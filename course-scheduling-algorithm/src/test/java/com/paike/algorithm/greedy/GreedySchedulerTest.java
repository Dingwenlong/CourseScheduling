package com.paike.algorithm.greedy;

import com.paike.algorithm.dto.ClassroomData;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.algorithm.dto.SchedulingResult;
import com.paike.algorithm.dto.TaskData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GreedySchedulerTest {

    @Test
    void shouldScheduleHigherPriorityTaskFirstWhenResourcesConflict() {
        GreedyScheduler scheduler = new GreedyScheduler();

        SchedulingRequest request = new SchedulingRequest();
        request.setDaysPerWeek(1);
        request.setSlotsPerDay(2);

        TaskData highPriority = createTask(1L, 10, 100L, 200L, 2);
        TaskData lowPriority = createTask(2L, 1, 100L, 201L, 2);
        ClassroomData classroom = createClassroom(10L, 60);

        SchedulingResult result = scheduler.schedule(request, List.of(lowPriority, highPriority), List.of(classroom));

        assertEquals(1, result.getScheduledTasks().size());
        assertEquals(1L, result.getScheduledTasks().get(0).getTaskId());
        assertEquals(1, result.getConflictCount());
    }

    @Test
    void shouldTreatAdjacentStartSlotsAsOverlappingForTwoSlotSessions() {
        GreedyScheduler scheduler = new GreedyScheduler();

        SchedulingRequest request = new SchedulingRequest();
        request.setDaysPerWeek(1);
        request.setSlotsPerDay(3);

        TaskData firstTask = createTask(1L, 10, 100L, 200L, 2);
        TaskData secondTask = createTask(2L, 9, 101L, 201L, 2);
        ClassroomData classroom = createClassroom(10L, 60);

        SchedulingResult result = scheduler.schedule(request, List.of(firstTask, secondTask), List.of(classroom));

        assertEquals(1, result.getScheduledTasks().size());
        assertEquals(1, result.getConflictCount());
        assertNotNull(result.getScheduledTasks().get(0).getTimeSlot());
        assertEquals(1, result.getScheduledTasks().get(0).getTimeSlot().getSlotNo());
    }

    private TaskData createTask(Long taskId, int priority, Long teacherId, Long classId, int weeklyHours) {
        TaskData task = new TaskData();
        task.setTaskId(taskId);
        task.setCourseId(taskId);
        task.setCourseName("课程-" + taskId);
        task.setTeacherId(teacherId);
        task.setTeacherName("教师-" + teacherId);
        task.setClassId(classId);
        task.setClassName("班级-" + classId);
        task.setStudentCount(40);
        task.setWeeklyHours(weeklyHours);
        task.setPriority(priority);
        task.setCourseType("THEORY");
        task.setNeedLab(0);
        task.setNeedMultimedia(0);
        return task;
    }

    private ClassroomData createClassroom(Long classroomId, int capacity) {
        ClassroomData classroom = new ClassroomData();
        classroom.setClassroomId(classroomId);
        classroom.setRoomName("教室-" + classroomId);
        classroom.setCapacity(capacity);
        classroom.setRoomType("LECTURE_HALL");
        classroom.setHasProjector(1);
        return classroom;
    }
}
