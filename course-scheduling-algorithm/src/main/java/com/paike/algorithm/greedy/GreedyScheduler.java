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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GreedyScheduler implements SchedulingService {

    private static final Logger log = LoggerFactory.getLogger(GreedyScheduler.class);
    private static final int SESSION_SLOT_SPAN = 2;

    @Autowired
    private GreedyAlgorithmConfig config;

    @Override
    public SchedulingResult schedule(SchedulingRequest request, List<TaskData> tasks, List<ClassroomData> classrooms) {
        long startTime = System.currentTimeMillis();
        log.info("开始贪心算法排课，任务数量: {}, 教室数量: {}", tasks.size(), classrooms.size());

        int daysPerWeek = request.getDaysPerWeek() != null ? request.getDaysPerWeek() : 5;
        int slotsPerDay = request.getSlotsPerDay() != null ? request.getSlotsPerDay() : 10;

        List<TaskData> sortedTasks = sortTasksByPriority(tasks);
        
        Map<Long, Set<TimeSlot>> teacherSchedule = new HashMap<>();
        Map<Long, Set<TimeSlot>> classroomSchedule = new HashMap<>();
        Map<Long, Set<TimeSlot>> classSchedule = new HashMap<>();
        
        List<ScheduledTask> scheduledTasks = new ArrayList<>();
        int conflictCount = 0;
        int scheduledTaskCount = 0;
        
        for (TaskData task : sortedTasks) {
            List<ScheduledTask> result = scheduleTask(task, classrooms, teacherSchedule, classroomSchedule, classSchedule, daysPerWeek, slotsPerDay);
            if (!result.isEmpty()) {
                scheduledTasks.addAll(result);
                result.forEach(item -> updateSchedules(item, teacherSchedule, classroomSchedule, classSchedule));
                scheduledTaskCount++;
            } else {
                conflictCount++;
                log.warn("任务 {} 排课失败，无法找到合适的时间槽", task.getTaskId());
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        log.info("贪心算法排课完成，成功: {}, 冲突: {}, 耗时: {}ms", 
                scheduledTaskCount, conflictCount, executionTime);

        SchedulingResult result = SchedulingResult.success(scheduledTasks, tasks.size(), scheduledTaskCount, conflictCount);
        result.setExecutionTime(executionTime);
        return result;
    }

    private List<TaskData> sortTasksByPriority(List<TaskData> tasks) {
        return tasks.stream()
                .sorted((t1, t2) -> {
                    int p1 = t1.getPriority() != null ? t1.getPriority() : 5;
                    int p2 = t2.getPriority() != null ? t2.getPriority() : 5;
                    if (p1 != p2) {
                        return Integer.compare(p2, p1);
                    }
                    int hours1 = t1.getWeeklyHours() != null ? t1.getWeeklyHours() : 0;
                    int hours2 = t2.getWeeklyHours() != null ? t2.getWeeklyHours() : 0;
                    return hours2 - hours1;
                })
                .collect(Collectors.toList());
    }

    private List<ScheduledTask> scheduleTask(TaskData task,
                                             List<ClassroomData> classrooms,
                                             Map<Long, Set<TimeSlot>> teacherSchedule,
                                             Map<Long, Set<TimeSlot>> classroomSchedule,
                                             Map<Long, Set<TimeSlot>> classSchedule,
                                             int daysPerWeek,
                                             int slotsPerDay) {
        
        List<ClassroomData> suitableClassrooms = findSuitableClassrooms(task, classrooms);
        if (suitableClassrooms.isEmpty()) {
            log.warn("任务 {} 没有找到合适的教室", task.getTaskId());
            return List.of();
        }

        int weeklyHours = task.getWeeklyHours() != null ? task.getWeeklyHours() : 2;
        int sessions = Math.max(1, (weeklyHours + 1) / 2);
        int maxStartSlot = Math.max(1, slotsPerDay - SESSION_SLOT_SPAN + 1);
        List<ScheduledTask> selectedSessions = new ArrayList<>();
        Set<TimeSlot> selectedOccupiedSlots = new HashSet<>();

        for (int session = 0; session < sessions; session++) {
            TimeSlot bestSlot = null;
            ClassroomData bestClassroom = null;
            int bestScore = Integer.MIN_VALUE;

            for (int day = 1; day <= daysPerWeek; day++) {
                for (int slot = 1; slot <= maxStartSlot; slot++) {
                    TimeSlot timeSlot = new TimeSlot(day, slot);
                    
                    if (isSlotConflict(task, timeSlot, teacherSchedule, classSchedule, selectedOccupiedSlots)) {
                        continue;
                    }

                    for (ClassroomData classroom : suitableClassrooms) {
                        if (isClassroomConflict(classroom, timeSlot, classroomSchedule, selectedSessions)) {
                            continue;
                        }

                        int score = calculateSlotScore(task, timeSlot, classroom) + calculateDistributionScore(selectedSessions, timeSlot);
                        if (score > bestScore) {
                            bestScore = score;
                            bestSlot = timeSlot;
                            bestClassroom = classroom;
                        }
                    }
                }
            }

            if (bestSlot == null || bestClassroom == null) {
                return List.of();
            }

            selectedSessions.add(createScheduledTask(task, bestSlot, bestClassroom));
            selectedOccupiedSlots.addAll(getOccupiedSlots(bestSlot));
        }

        return selectedSessions;
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

    private boolean isSlotConflict(TaskData task,
                                   TimeSlot timeSlot,
                                   Map<Long, Set<TimeSlot>> teacherSchedule,
                                   Map<Long, Set<TimeSlot>> classSchedule,
                                   Set<TimeSlot> selectedOccupiedSlots) {
        List<TimeSlot> occupiedSlots = getOccupiedSlots(timeSlot);
        for (TimeSlot occupiedSlot : occupiedSlots) {
            if (selectedOccupiedSlots.contains(occupiedSlot)) {
                return true;
            }

            if (task.getTeacherId() != null) {
                Set<TimeSlot> teacherSlots = teacherSchedule.get(task.getTeacherId());
                if (teacherSlots != null && teacherSlots.contains(occupiedSlot)) {
                    return true;
                }
            }

            if (task.getClassId() != null) {
                Set<TimeSlot> classSlots = classSchedule.get(task.getClassId());
                if (classSlots != null && classSlots.contains(occupiedSlot)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isClassroomConflict(ClassroomData classroom, TimeSlot timeSlot,
                                        Map<Long, Set<TimeSlot>> classroomSchedule,
                                        List<ScheduledTask> selectedSessions) {
        List<TimeSlot> occupiedSlots = getOccupiedSlots(timeSlot);
        Set<TimeSlot> classroomSlots = classroomSchedule.get(classroom.getClassroomId());
        if (classroomSlots != null) {
            for (TimeSlot occupiedSlot : occupiedSlots) {
                if (classroomSlots.contains(occupiedSlot)) {
                    return true;
                }
            }
        }
        return selectedSessions.stream()
                .anyMatch(task -> classroom.getClassroomId().equals(task.getClassroomId())
                        && isOverlapping(timeSlot, task.getTimeSlot()));
    }

    private int calculateDistributionScore(List<ScheduledTask> selectedSessions, TimeSlot candidate) {
        if (selectedSessions.isEmpty()) {
            return 0;
        }
        boolean sameDayAlreadyUsed = selectedSessions.stream()
                .anyMatch(task -> task.getTimeSlot().getDayOfWeek().equals(candidate.getDayOfWeek()));
        return sameDayAlreadyUsed ? -5 : 5;
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
        scheduledTask.setWeeks(task.getWeeks());
        scheduledTask.setStudentCount(task.getStudentCount());
        scheduledTask.setPriority(task.getPriority());
        return scheduledTask;
    }

    private List<TimeSlot> getOccupiedSlots(TimeSlot startSlot) {
        List<TimeSlot> occupiedSlots = new ArrayList<>(SESSION_SLOT_SPAN);
        for (int offset = 0; offset < SESSION_SLOT_SPAN; offset++) {
            occupiedSlots.add(new TimeSlot(startSlot.getDayOfWeek(), startSlot.getSlotNo() + offset));
        }
        return occupiedSlots;
    }

    private boolean isOverlapping(TimeSlot left, TimeSlot right) {
        if (!Objects.equals(left.getDayOfWeek(), right.getDayOfWeek())) {
            return false;
        }
        int leftEnd = left.getSlotNo() + SESSION_SLOT_SPAN - 1;
        int rightEnd = right.getSlotNo() + SESSION_SLOT_SPAN - 1;
        return left.getSlotNo() <= rightEnd && right.getSlotNo() <= leftEnd;
    }

    private void updateSchedules(ScheduledTask task,
                                 Map<Long, Set<TimeSlot>> teacherSchedule,
                                 Map<Long, Set<TimeSlot>> classroomSchedule,
                                 Map<Long, Set<TimeSlot>> classSchedule) {
        List<TimeSlot> occupiedSlots = getOccupiedSlots(task.getTimeSlot());
        
        if (task.getTeacherId() != null) {
            teacherSchedule.computeIfAbsent(task.getTeacherId(), k -> new HashSet<>())
                    .addAll(occupiedSlots);
        }

        if (task.getClassroomId() != null) {
            classroomSchedule.computeIfAbsent(task.getClassroomId(), k -> new HashSet<>())
                    .addAll(occupiedSlots);
        }

        if (task.getClassId() != null) {
            classSchedule.computeIfAbsent(task.getClassId(), k -> new HashSet<>())
                    .addAll(occupiedSlots);
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
