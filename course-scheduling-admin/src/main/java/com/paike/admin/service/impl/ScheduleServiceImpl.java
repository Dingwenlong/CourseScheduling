package com.paike.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paike.admin.entity.Classroom;
import com.paike.admin.entity.Clazz;
import com.paike.admin.entity.Course;
import com.paike.admin.entity.TeachingTask;
import com.paike.admin.entity.Teacher;
import com.paike.admin.entity.User;
import com.paike.admin.mapper.ClassMapper;
import com.paike.admin.mapper.ClassroomMapper;
import com.paike.admin.mapper.CourseMapper;
import com.paike.admin.mapper.TeachingTaskMapper;
import com.paike.admin.mapper.TeacherMapper;
import com.paike.admin.mapper.UserMapper;
import com.paike.admin.service.ScheduleService;
import com.paike.algorithm.dto.ClassroomData;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.algorithm.dto.SchedulingResult;
import com.paike.algorithm.dto.TaskData;
import com.paike.algorithm.genetic.GeneticScheduler;
import com.paike.algorithm.greedy.GreedyScheduler;
import com.paike.common.constants.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private CourseMapper courseMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private UserMapper userMapper;

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
        SchedulingResult result = schedule(request);
        return CompletableFuture.completedFuture(result);
    }

    private List<TaskData> loadTasks(SchedulingRequest request) {
        LambdaQueryWrapper<TeachingTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachingTask::getSemester, request.getSemester());
        wrapper.and(w -> w.eq(TeachingTask::getStatus, TaskStatus.PENDING.getCode())
                .or()
                .isNull(TeachingTask::getStatus));
        
        if (request.getTaskIds() != null && !request.getTaskIds().isEmpty()) {
            wrapper.in(TeachingTask::getId, request.getTaskIds());
        }

        List<TeachingTask> taskList = teachingTaskMapper.selectList(wrapper);
        if (taskList.isEmpty()) {
            return List.of();
        }

        Set<Long> courseIds = taskList.stream()
                .map(TeachingTask::getCourseId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> teacherIds = taskList.stream()
                .map(TeachingTask::getTeacherId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> classIds = taskList.stream()
                .map(TeachingTask::getClassId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, course -> course));
        Map<Long, Teacher> teacherMap = teacherMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(Teacher::getId, teacher -> teacher));
        Map<Long, Clazz> classMap = classMapper.selectBatchIds(classIds).stream()
                .collect(Collectors.toMap(Clazz::getId, clazz -> clazz));

        Set<Long> userIds = teacherMap.values().stream()
                .map(Teacher::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return taskList.stream()
                .map(task -> convertToTaskData(task, courseMap, teacherMap, classMap, userMap))
                .collect(Collectors.toList());
    }

    private TaskData convertToTaskData(TeachingTask task,
                                       Map<Long, Course> courseMap,
                                       Map<Long, Teacher> teacherMap,
                                       Map<Long, Clazz> classMap,
                                       Map<Long, User> userMap) {
        Course course = courseMap.get(task.getCourseId());
        Teacher teacher = teacherMap.get(task.getTeacherId());
        Clazz clazz = classMap.get(task.getClassId());
        User teacherUser = teacher != null ? userMap.get(teacher.getUserId()) : null;

        TaskData data = new TaskData();
        data.setTaskId(task.getId());
        data.setCourseId(task.getCourseId());
        data.setCourseName(course != null ? course.getCourseName() : "课程-" + task.getCourseId());
        data.setTeacherId(task.getTeacherId());
        data.setTeacherName(resolveTeacherName(task.getTeacherId(), teacher, teacherUser));
        data.setClassId(task.getClassId());
        data.setClassName(clazz != null ? clazz.getClassName() : "班级-" + task.getClassId());
        data.setStudentCount(task.getStudentCount() != null ? task.getStudentCount() : (clazz != null ? clazz.getStudentCount() : null));
        data.setWeeklyHours(task.getWeeklyHours());
        data.setWeeks(task.getWeeks());
        data.setPriority(task.getPriorityLevel());
        data.setCourseType(course != null ? course.getCourseType() : task.getCourseNature());
        data.setNeedMultimedia(course != null ? course.getNeedMultimedia() : null);
        data.setNeedLab(course != null ? course.getNeedLab() : null);
        data.setPreferredCampusId(teacher != null ? teacher.getCampusId() : null);
        data.setTimePreference(task.getTimePreference());
        return data;
    }

    private String resolveTeacherName(Long teacherId, Teacher teacher, User teacherUser) {
        if (teacherUser != null && teacherUser.getRealName() != null && !teacherUser.getRealName().isBlank()) {
            return teacherUser.getRealName();
        }
        if (teacher != null && teacher.getTeacherNo() != null && !teacher.getTeacherNo().isBlank()) {
            return teacher.getTeacherNo();
        }
        return teacherId != null ? "教师-" + teacherId : null;
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
