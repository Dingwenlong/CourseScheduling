package com.paike.algorithm.model;

import java.io.Serializable;

public class ScheduledTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long taskId;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private Long classId;
    private String className;
    private Long classroomId;
    private String classroomName;
    private TimeSlot timeSlot;
    private String weeks;
    private Integer studentCount;
    private Integer priority;

    public ScheduledTask() {
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getWeeks() {
        return weeks;
    }

    public void setWeeks(String weeks) {
        this.weeks = weeks;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public static ScheduledTask of(Long taskId, Long courseId, Long teacherId, Long classId, Long classroomId, TimeSlot timeSlot) {
        ScheduledTask task = new ScheduledTask();
        task.setTaskId(taskId);
        task.setCourseId(courseId);
        task.setTeacherId(teacherId);
        task.setClassId(classId);
        task.setClassroomId(classroomId);
        task.setTimeSlot(timeSlot);
        return task;
    }
}
