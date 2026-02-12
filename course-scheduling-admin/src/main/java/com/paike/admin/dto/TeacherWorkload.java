package com.paike.admin.dto;

import java.math.BigDecimal;

public class TeacherWorkload {

    private Long teacherId;
    private String teacherName;
    private Integer totalHours;
    private Integer courseCount;
    private BigDecimal averageHoursPerCourse;

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

    public Integer getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Integer totalHours) {
        this.totalHours = totalHours;
    }

    public Integer getCourseCount() {
        return courseCount;
    }

    public void setCourseCount(Integer courseCount) {
        this.courseCount = courseCount;
    }

    public BigDecimal getAverageHoursPerCourse() {
        return averageHoursPerCourse;
    }

    public void setAverageHoursPerCourse(BigDecimal averageHoursPerCourse) {
        this.averageHoursPerCourse = averageHoursPerCourse;
    }
}
