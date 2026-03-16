package com.paike.admin.dto;

public class AdjustmentRequest {

    private Long applicationId;
    private Long timetableId;
    private Long detailId;
    private Integer newDayOfWeek;
    private Integer newSlotNo;
    private Long newClassroomId;
    private String reason;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(Long timetableId) {
        this.timetableId = timetableId;
    }

    public Long getDetailId() {
        return detailId;
    }

    public void setDetailId(Long detailId) {
        this.detailId = detailId;
    }

    public Integer getNewDayOfWeek() {
        return newDayOfWeek;
    }

    public void setNewDayOfWeek(Integer newDayOfWeek) {
        this.newDayOfWeek = newDayOfWeek;
    }

    public Integer getNewSlotNo() {
        return newSlotNo;
    }

    public void setNewSlotNo(Integer newSlotNo) {
        this.newSlotNo = newSlotNo;
    }

    public Long getNewClassroomId() {
        return newClassroomId;
    }

    public void setNewClassroomId(Long newClassroomId) {
        this.newClassroomId = newClassroomId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
