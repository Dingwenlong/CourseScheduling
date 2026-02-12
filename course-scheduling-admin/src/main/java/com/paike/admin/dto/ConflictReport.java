package com.paike.admin.dto;

import java.util.List;

public class ConflictReport {

    private Integer totalConflicts;
    private Integer teacherConflicts;
    private Integer classroomConflicts;
    private Integer classConflicts;
    private List<ConflictDetail> conflictDetails;

    public Integer getTotalConflicts() {
        return totalConflicts;
    }

    public void setTotalConflicts(Integer totalConflicts) {
        this.totalConflicts = totalConflicts;
    }

    public Integer getTeacherConflicts() {
        return teacherConflicts;
    }

    public void setTeacherConflicts(Integer teacherConflicts) {
        this.teacherConflicts = teacherConflicts;
    }

    public Integer getClassroomConflicts() {
        return classroomConflicts;
    }

    public void setClassroomConflicts(Integer classroomConflicts) {
        this.classroomConflicts = classroomConflicts;
    }

    public Integer getClassConflicts() {
        return classConflicts;
    }

    public void setClassConflicts(Integer classConflicts) {
        this.classConflicts = classConflicts;
    }

    public List<ConflictDetail> getConflictDetails() {
        return conflictDetails;
    }

    public void setConflictDetails(List<ConflictDetail> conflictDetails) {
        this.conflictDetails = conflictDetails;
    }
}
