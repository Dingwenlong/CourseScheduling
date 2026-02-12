package com.paike.admin.service.impl;

import com.paike.admin.entity.Classroom;
import com.paike.admin.entity.TeachingTask;
import com.paike.admin.mapper.ClassroomMapper;
import com.paike.admin.mapper.TeachingTaskMapper;
import com.paike.admin.service.ScheduleService;
import com.paike.algorithm.dto.ClassroomData;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.algorithm.dto.SchedulingResult;
import com.paike.algorithm.dto.TaskData;
import com.paike.algorithm.genetic.GeneticScheduler;
import com.paike.algorithm.greedy.GreedyScheduler;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleServiceImpl.class);

    @Autowired
    private TeachingTaskMapper teachingTaskMapper;

    @Autowired
    private ClassroomMapper classroomMapper;

    @Autowired
    private GreedyScheduler greedyScheduler;

    @Autowired
    private GeneticScheduler geneticScheduler;

    @Override
    public SchedulingResult schedule(SchedulingRequest request) {
        log.info("开始排课，学期: {}, 算法类型: {}", request.getSemester(), request.getAlgorithmType());

        List<TaskData> tasks = loadTasks(request);
        List<ClassroomData> classrooms = loadClassrooms();

        if (tasks.isEmpty()) {
            return SchedulingResult.fail("没有待排课的教学任务");
        }

        if (classrooms.isEmpty()) {
            return SchedulingResult.fail("没有可用的教室资源");
        }

        String algorithmType = request.getAlgorithmType();
        if ("GENETIC".equalsIgnoreCase(algorithmType)) {
            return geneticScheduler.schedule(request, tasks, classrooms);
        } else {
            return greedyScheduler.schedule(request, tasks, classrooms);
        }
    }

    @Override
    @Async
    public CompletableFuture<SchedulingResult> scheduleAsync(SchedulingRequest request) {
        return CompletableFuture.completedFuture(schedule(request));
    }

    private List<TaskData> loadTasks(SchedulingRequest request) {
        LambdaQueryWrapper<TeachingTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachingTask::getSemester, request.getSemester());
        
        if (request.getTaskIds() != null && !request.getTaskIds().isEmpty()) {
            wrapper.in(TeachingTask::getId, request.getTaskIds());
        }

        List<TeachingTask> taskList = teachingTaskMapper.selectList(wrapper);
        
        return taskList.stream().map(this::convertToTaskData).collect(Collectors.toList());
    }

    private TaskData convertToTaskData(TeachingTask task) {
        TaskData data = new TaskData();
        data.setTaskId(task.getId());
        data.setCourseId(task.getCourseId());
        data.setTeacherId(task.getTeacherId());
        data.setClassId(task.getClassId());
        data.setStudentCount(task.getStudentCount());
        data.setWeeklyHours(task.getWeeklyHours());
        data.setPriority(task.getPriorityLevel());
        data.setTimePreference(task.getTimePreference());
        return data;
    }

    private List<ClassroomData> loadClassrooms() {
        List<Classroom> roomList = classroomMapper.selectList(
                new LambdaQueryWrapper<Classroom>().eq(Classroom::getStatus, 1)
        );
        
        return roomList.stream().map(this::convertToClassroomData).collect(Collectors.toList());
    }

    private ClassroomData convertToClassroomData(Classroom classroom) {
        ClassroomData data = new ClassroomData();
        data.setClassroomId(classroom.getId());
        data.setRoomNo(classroom.getRoomNo());
        data.setRoomName(classroom.getRoomName());
        data.setCampusId(classroom.getCampusId());
        data.setCapacity(classroom.getCapacity());
        data.setRoomType(classroom.getRoomType());
        data.setHasProjector(classroom.getHasProjector());
        data.setHasMicrophone(classroom.getHasMicrophone());
        data.setHasAirConditioner(classroom.getHasAirConditioner());
        return data;
    }
}
