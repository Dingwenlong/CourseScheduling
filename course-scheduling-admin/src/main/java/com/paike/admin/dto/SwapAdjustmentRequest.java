package com.paike.admin.dto;

public class SwapAdjustmentRequest {

    private Long applicationId;
    private Long timetableId;
    private Long detailId1;
    private Long detailId2;
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

    public Long getDetailId1() {
        return detailId1;
    }

    public void setDetailId1(Long detailId1) {
        this.detailId1 = detailId1;
    }

    public Long getDetailId2() {
        return detailId2;
    }

    public void setDetailId2(Long detailId2) {
        this.detailId2 = detailId2;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
