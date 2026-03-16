package com.paike.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("adj_application")
public class AdjustmentApplication extends BaseEntity {

    private String applicationNo;
    private String semester;
    private Long detailId;
    private Long teacherId;
    private Integer oldDay;
    private Integer oldSlot;
    private Long oldClassroom;
    private Integer newDay;
    private Integer newSlot;
    private Long newClassroom;
    private String reason;
    private String attachmentUrl;

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

    public Long getDetailId() {
        return detailId;
    }

    public void setDetailId(Long detailId) {
        this.detailId = detailId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getOldDay() {
        return oldDay;
    }

    public void setOldDay(Integer oldDay) {
        this.oldDay = oldDay;
    }

    public Integer getOldSlot() {
        return oldSlot;
    }

    public void setOldSlot(Integer oldSlot) {
        this.oldSlot = oldSlot;
    }

    public Long getOldClassroom() {
        return oldClassroom;
    }

    public void setOldClassroom(Long oldClassroom) {
        this.oldClassroom = oldClassroom;
    }

    public Integer getNewDay() {
        return newDay;
    }

    public void setNewDay(Integer newDay) {
        this.newDay = newDay;
    }

    public Integer getNewSlot() {
        return newSlot;
    }

    public void setNewSlot(Integer newSlot) {
        this.newSlot = newSlot;
    }

    public Long getNewClassroom() {
        return newClassroom;
    }

    public void setNewClassroom(Long newClassroom) {
        this.newClassroom = newClassroom;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
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
