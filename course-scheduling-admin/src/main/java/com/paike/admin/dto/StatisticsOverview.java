package com.paike.admin.dto;

import java.util.List;

public class StatisticsOverview {

    private Integer totalHours;
    private Integer courseCount;
    private List<ClassroomUtilization> classroomUtilization;
    private List<TeacherWorkload> teacherWorkload;
    private ConflictReport conflictReport;

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

    public List<ClassroomUtilization> getClassroomUtilization() {
        return classroomUtilization;
    }

    public void setClassroomUtilization(List<ClassroomUtilization> classroomUtilization) {
        this.classroomUtilization = classroomUtilization;
    }

    public List<TeacherWorkload> getTeacherWorkload() {
        return teacherWorkload;
    }

    public void setTeacherWorkload(List<TeacherWorkload> teacherWorkload) {
        this.teacherWorkload = teacherWorkload;
    }

    public ConflictReport getConflictReport() {
        return conflictReport;
    }

    public void setConflictReport(ConflictReport conflictReport) {
        this.conflictReport = conflictReport;
    }
}
