package com.paike.admin.dto;

import java.time.LocalDateTime;

public class SwapAdjustmentHistoryResponse {

    private Long id;
    private String applicationNo;
    private Long timetableId;
    private Long detailId1;
    private Long detailId2;
    private String courseName1;
    private String courseName2;
    private String sourceSummary1;
    private String sourceSummary2;
    private String reason;
    private String status;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private String auditRemark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationNo() {
        return applicationNo;
    }

    public void setApplicationNo(String applicationNo) {
        this.applicationNo = applicationNo;
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

    public String getCourseName1() {
        return courseName1;
    }

    public void setCourseName1(String courseName1) {
        this.courseName1 = courseName1;
    }

    public String getCourseName2() {
        return courseName2;
    }

    public void setCourseName2(String courseName2) {
        this.courseName2 = courseName2;
    }

    public String getSourceSummary1() {
        return sourceSummary1;
    }

    public void setSourceSummary1(String sourceSummary1) {
        this.sourceSummary1 = sourceSummary1;
    }

    public String getSourceSummary2() {
        return sourceSummary2;
    }

    public void setSourceSummary2(String sourceSummary2) {
        this.sourceSummary2 = sourceSummary2;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }
}
