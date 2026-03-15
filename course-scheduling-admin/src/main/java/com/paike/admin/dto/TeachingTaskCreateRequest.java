package com.paike.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class TeachingTaskCreateRequest {

    @NotBlank(message = "学期不能为空")
    private String semester;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @NotNull(message = "班级ID不能为空")
    private Long classId;

    @PositiveOrZero(message = "学生人数不能为负数")
    private Integer studentCount;

    @Positive(message = "周课时必须大于0")
    private Integer weeklyHours;

    @Positive(message = "总周数必须大于0")
    private Integer totalWeeks;

    private String weeks;

    private String courseNature;

    @PositiveOrZero(message = "优先级不能为负数")
    private Integer priorityLevel;

    private Integer fixedDay;

    private Integer fixedSlot;

    private Long fixedClassroom;

    private String timePreference;

    private String status;

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(Integer weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public Integer getTotalWeeks() {
        return totalWeeks;
    }

    public void setTotalWeeks(Integer totalWeeks) {
        this.totalWeeks = totalWeeks;
    }

    public String getWeeks() {
        return weeks;
    }

    public void setWeeks(String weeks) {
        this.weeks = weeks;
    }

    public String getCourseNature() {
        return courseNature;
    }

    public void setCourseNature(String courseNature) {
        this.courseNature = courseNature;
    }

    public Integer getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public Integer getFixedDay() {
        return fixedDay;
    }

    public void setFixedDay(Integer fixedDay) {
        this.fixedDay = fixedDay;
    }

    public Integer getFixedSlot() {
        return fixedSlot;
    }

    public void setFixedSlot(Integer fixedSlot) {
        this.fixedSlot = fixedSlot;
    }

    public Long getFixedClassroom() {
        return fixedClassroom;
    }

    public void setFixedClassroom(Long fixedClassroom) {
        this.fixedClassroom = fixedClassroom;
    }

    public String getTimePreference() {
        return timePreference;
    }

    public void setTimePreference(String timePreference) {
        this.timePreference = timePreference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
