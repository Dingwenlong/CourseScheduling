package com.paike.algorithm.dto;

import java.io.Serializable;

public class TaskData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long taskId;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private Long classId;
    private String className;
    private Integer studentCount;
    private Integer weeklyHours;
    private Integer priority;
    private String courseType;
    private Integer needMultimedia;
    private Integer needLab;
    private Long preferredCampusId;
    private String timePreference;

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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public Integer getNeedMultimedia() {
        return needMultimedia;
    }

    public void setNeedMultimedia(Integer needMultimedia) {
        this.needMultimedia = needMultimedia;
    }

    public Integer getNeedLab() {
        return needLab;
    }

    public void setNeedLab(Integer needLab) {
        this.needLab = needLab;
    }

    public Long getPreferredCampusId() {
        return preferredCampusId;
    }

    public void setPreferredCampusId(Long preferredCampusId) {
        this.preferredCampusId = preferredCampusId;
    }

    public String getTimePreference() {
        return timePreference;
    }

    public void setTimePreference(String timePreference) {
        this.timePreference = timePreference;
    }
}
