package com.paike.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("edu_teaching_task")
public class TeachingTask extends BaseEntity {

    private String semester;
    private Long courseId;
    private Long teacherId;
    private Long classId;
    private Integer studentCount;
    private Integer weeklyHours;
    private Integer totalWeeks;
    private String weeks;

    @TableField("`course_nature`")
    private String courseNature;

    private Integer priorityLevel;
    private Integer fixedDay;
    private Integer fixedSlot;
    private Long fixedClassroom;
    private String timePreference;

    @TableField("`status`")
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
