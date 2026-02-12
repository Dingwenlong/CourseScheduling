package com.paike.algorithm.greedy;

import com.paike.algorithm.config.GreedyAlgorithmConfig;
import com.paike.algorithm.dto.ClassroomData;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.algorithm.dto.SchedulingResult;
import com.paike.algorithm.dto.TaskData;
import com.paike.algorithm.model.ScheduledTask;
import com.paike.algorithm.model.TimeSlot;
import com.paike.algorithm.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GreedyScheduler implements SchedulingService {

    private static final Logger log = LoggerFactory.getLogger(GreedyScheduler.class);

    @Autowired
    private GreedyAlgorithmConfig config;

    private int daysPerWeek = 5;
    private int slotsPerDay = 10;

    @Override
    public SchedulingResult schedule(SchedulingRequest request, List<TaskData> tasks, List<ClassroomData> classrooms) {
        long startTime = System.currentTimeMillis();
        log.info("开始贪心算法排课，任务数量: {}, 教室数量: {}", tasks.size(), classrooms.size());

        if (request.getDaysPerWeek() != null) {
            this.daysPerWeek = request.getDaysPerWeek();
        }
        if (request.getSlotsPerDay() != null) {
            this.slotsPerDay = request.getSlotsPerDay();
        }

        List<TaskData> sortedTasks = sortTasksByPriority(tasks);
        
        Map<Long, Set<TimeSlot>> teacherSchedule = new HashMap<>();
        Map<Long, Set<TimeSlot>> classroomSchedule = new HashMap<>();
        Map<Long, Set<TimeSlot>> classSchedule = new HashMap<>();
        
        List<ScheduledTask> scheduledTasks = new ArrayList<>();
        int conflictCount = 0;
        
        for (TaskData task : sortedTasks) {
            ScheduledTask result = scheduleTask(task, classrooms, teacherSchedule, classroomSchedule, classSchedule);
            if (result != null) {
                scheduledTasks.add(result);
                updateSchedules(result, teacherSchedule, classroomSchedule, classSchedule);
            } else {
                conflictCount++;
                log.warn("任务 {} 排课失败，无法找到合适的时间槽", task.getTaskId());
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        log.info("贪心算法排课完成，成功: {}, 冲突: {}, 耗时: {}ms", 
                scheduledTasks.size(), conflictCount, executionTime);

        SchedulingResult result = SchedulingResult.success(scheduledTasks, tasks.size(), scheduledTasks.size(), conflictCount);
        result.setExecutionTime(executionTime);
        return result;
    }

    private List<TaskData> sortTasksByPriority(List<TaskData> tasks) {
        return tasks.stream()
                .sorted((t1, t2) -> {
                    int p1 = t1.getPriority() != null ? t1.getPriority() : 5;
                    int p2 = t2.getPriority() != null ? t2.getPriority() : 5;
                    if (p1 != p2) {
                        return p1 - p2;
                    }
                    int hours1 = t1.getWeeklyHours() != null ? t1.getWeeklyHours() : 0;
                    int hours2 = t2.getWeeklyHours() != null ? t2.getWeeklyHours() : 0;
                    return hours2 - hours1;
                })
                .collect(Collectors.toList());
    }

    private ScheduledTask scheduleTask(TaskData task, List<ClassroomData> classrooms,
                                       Map<Long, Set<TimeSlot>> teacherSchedule,
                                       Map<Long, Set<TimeSlot>> classroomSchedule,
                                       Map<Long, Set<TimeSlot>> classSchedule) {
        
        List<ClassroomData> suitableClassrooms = findSuitableClassrooms(task, classrooms);
        if (suitableClassrooms.isEmpty()) {
            log.warn("任务 {} 没有找到合适的教室", task.getTaskId());
            return null;
        }

        int weeklyHours = task.getWeeklyHours() != null ? task.getWeeklyHours() : 2;
        int sessions = (weeklyHours + 1) / 2;

        for (int session = 0; session < sessions; session++) {
            TimeSlot bestSlot = null;
            ClassroomData bestClassroom = null;
            int bestScore = Integer.MIN_VALUE;

            for (int day = 1; day <= daysPerWeek; day++) {
                for (int slot = 1; slot <= slotsPerDay - 1; slot++) {
                    TimeSlot timeSlot = new TimeSlot(day, slot);
                    
                    if (isSlotConflict(task, timeSlot, suitableClassrooms.get(0),
                            teacherSchedule, classroomSchedule, classSchedule)) {
                        continue;
                    }

                    for (ClassroomData classroom : suitableClassrooms) {
                        if (isClassroomConflict(classroom, timeSlot, classroomSchedule)) {
                            continue;
                        }

                        int score = calculateSlotScore(task, timeSlot, classroom);
                        if (score > bestScore) {
                            bestScore = score;
                            bestSlot = timeSlot;
                            bestClassroom = classroom;
                        }
                    }
                }
            }

            if (bestSlot == null || bestClassroom == null) {
                return null;
            }

            ScheduledTask scheduledTask = createScheduledTask(task, bestSlot, bestClassroom);
            return scheduledTask;
        }

        return null;
    }

    private List<ClassroomData> findSuitableClassrooms(TaskData task, List<ClassroomData> classrooms) {
        return classrooms.stream()
                .filter(c -> c.matches(task))
                .sorted((c1, c2) -> {
                    int cap1 = c1.getCapacity() != null ? c1.getCapacity() : 0;
                    int cap2 = c2.getCapacity() != null ? c2.getCapacity() : 0;
                    int students = task.getStudentCount() != null ? task.getStudentCount() : 0;
                    int diff1 = Math.abs(cap1 - students);
                    int diff2 = Math.abs(cap2 - students);
                    return diff1 - diff2;
                })
                .collect(Collectors.toList());
    }

    private boolean isSlotConflict(TaskData task, TimeSlot timeSlot, ClassroomData classroom,
                                   Map<Long, Set<TimeSlot>> teacherSchedule,
                                   Map<Long, Set<TimeSlot>> classroomSchedule,
                                   Map<Long, Set<TimeSlot>> classSchedule) {
        
        if (task.getTeacherId() != null) {
            Set<TimeSlot> teacherSlots = teacherSchedule.get(task.getTeacherId());
            if (teacherSlots != null && teacherSlots.contains(timeSlot)) {
                return true;
            }
        }

        if (task.getClassId() != null) {
            Set<TimeSlot> classSlots = classSchedule.get(task.getClassId());
            if (classSlots != null && classSlots.contains(timeSlot)) {
                return true;
            }
        }

        return false;
    }

    private boolean isClassroomConflict(ClassroomData classroom, TimeSlot timeSlot,
                                        Map<Long, Set<TimeSlot>> classroomSchedule) {
        Set<TimeSlot> classroomSlots = classroomSchedule.get(classroom.getClassroomId());
        return classroomSlots != null && classroomSlots.contains(timeSlot);
    }

    private int calculateSlotScore(TaskData task, TimeSlot timeSlot, ClassroomData classroom) {
        int score = 100;

        if (timeSlot.getSlotNo() >= 1 && timeSlot.getSlotNo() <= 4) {
            score += 20;
        }

        if (timeSlot.getDayOfWeek() >= 1 && timeSlot.getDayOfWeek() <= 4) {
            score += 10;
        }

        if (task.getPreferredCampusId() != null && 
            task.getPreferredCampusId().equals(classroom.getCampusId())) {
            score += 30;
        }

        if (classroom.getCapacity() != null && task.getStudentCount() != null) {
            int diff = classroom.getCapacity() - task.getStudentCount();
            if (diff >= 0 && diff <= 10) {
                score += 15;
            }
        }

        return score;
    }

    private ScheduledTask createScheduledTask(TaskData task, TimeSlot timeSlot, ClassroomData classroom) {
        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.setTaskId(task.getTaskId());
        scheduledTask.setCourseId(task.getCourseId());
        scheduledTask.setCourseName(task.getCourseName());
        scheduledTask.setTeacherId(task.getTeacherId());
        scheduledTask.setTeacherName(task.getTeacherName());
        scheduledTask.setClassId(task.getClassId());
        scheduledTask.setClassName(task.getClassName());
        scheduledTask.setClassroomId(classroom.getClassroomId());
        scheduledTask.setClassroomName(classroom.getRoomName());
        scheduledTask.setTimeSlot(timeSlot);
        scheduledTask.setStudentCount(task.getStudentCount());
        scheduledTask.setPriority(task.getPriority());
        return scheduledTask;
    }

    private void updateSchedules(ScheduledTask task,
                                 Map<Long, Set<TimeSlot>> teacherSchedule,
                                 Map<Long, Set<TimeSlot>> classroomSchedule,
                                 Map<Long, Set<TimeSlot>> classSchedule) {
        
        if (task.getTeacherId() != null) {
            teacherSchedule.computeIfAbsent(task.getTeacherId(), k -> new HashSet<>())
                    .add(task.getTimeSlot());
        }

        if (task.getClassroomId() != null) {
            classroomSchedule.computeIfAbsent(task.getClassroomId(), k -> new HashSet<>())
                    .add(task.getTimeSlot());
        }

        if (task.getClassId() != null) {
            classSchedule.computeIfAbsent(task.getClassId(), k -> new HashSet<>())
                    .add(task.getTimeSlot());
        }
    }

    @Override
    public String getAlgorithmName() {
        return "贪心算法";
    }

    @Override
    public String getAlgorithmDescription() {
        return "基于贪心策略的排课算法，按优先级排序任务，依次分配最优时间槽和教室";
    }
}
