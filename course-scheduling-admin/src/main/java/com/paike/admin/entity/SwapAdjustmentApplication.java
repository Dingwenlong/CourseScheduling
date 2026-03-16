package com.paike.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("adj_swap_application")
public class SwapAdjustmentApplication extends BaseEntity {

    private String applicationNo;
    private String semester;
    private Long timetableId;
    private Long detailId1;
    private Long detailId2;
    private Long teacherId;
    private Integer oldDay1;
    private Integer oldSlot1;
    private Long oldClassroom1;
    private Integer oldDay2;
    private Integer oldSlot2;
    private Long oldClassroom2;
    private String reason;

    @TableField("`status`")
    private String status;

    private LocalDateTime applyTime;
    private Long auditorId;
    private LocalDateTime auditTime;
    private String auditRemark;

    public String getApplicationNo() {
        return applicationNo;
    }

    public void setApplicationNo(String applicationNo) {
        this.applicationNo = applicationNo;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
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

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getOldDay1() {
        return oldDay1;
    }

    public void setOldDay1(Integer oldDay1) {
        this.oldDay1 = oldDay1;
    }

    public Integer getOldSlot1() {
        return oldSlot1;
    }

    public void setOldSlot1(Integer oldSlot1) {
        this.oldSlot1 = oldSlot1;
    }

    public Long getOldClassroom1() {
        return oldClassroom1;
    }

    public void setOldClassroom1(Long oldClassroom1) {
        this.oldClassroom1 = oldClassroom1;
    }

    public Integer getOldDay2() {
        return oldDay2;
    }

    public void setOldDay2(Integer oldDay2) {
        this.oldDay2 = oldDay2;
    }

    public Integer getOldSlot2() {
        return oldSlot2;
    }

    public void setOldSlot2(Integer oldSlot2) {
        this.oldSlot2 = oldSlot2;
    }

    public Long getOldClassroom2() {
        return oldClassroom2;
    }

    public void setOldClassroom2(Long oldClassroom2) {
        this.oldClassroom2 = oldClassroom2;
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

    public Long getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(Long auditorId) {
        this.auditorId = auditorId;
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
