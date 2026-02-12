package com.paike.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("sch_timetable")
public class Timetable extends BaseEntity {

    private String semester;
    private Integer version;
    private String name;

    @TableField("`status`")
    private String status;

    private String generateType;
    private String algorithmConfig;
    private Integer taskCount;
    private Integer scheduledCount;
    private Integer conflictCount;
    private BigDecimal utilizationRate;
    private BigDecimal satisfactionScore;
    private LocalDateTime generateTime;
    private LocalDateTime publishTime;
    private Long publisherId;
    private String remark;

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGenerateType() {
        return generateType;
    }

    public void setGenerateType(String generateType) {
        this.generateType = generateType;
    }

    public String getAlgorithmConfig() {
        return algorithmConfig;
    }

    public void setAlgorithmConfig(String algorithmConfig) {
        this.algorithmConfig = algorithmConfig;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }

    public Integer getScheduledCount() {
        return scheduledCount;
    }

    public void setScheduledCount(Integer scheduledCount) {
        this.scheduledCount = scheduledCount;
    }

    public Integer getConflictCount() {
        return conflictCount;
    }

    public void setConflictCount(Integer conflictCount) {
        this.conflictCount = conflictCount;
    }

    public BigDecimal getUtilizationRate() {
        return utilizationRate;
    }

    public void setUtilizationRate(BigDecimal utilizationRate) {
        this.utilizationRate = utilizationRate;
    }

    public BigDecimal getSatisfactionScore() {
        return satisfactionScore;
    }

    public void setSatisfactionScore(BigDecimal satisfactionScore) {
        this.satisfactionScore = satisfactionScore;
    }

    public LocalDateTime getGenerateTime() {
        return generateTime;
    }

    public void setGenerateTime(LocalDateTime generateTime) {
        this.generateTime = generateTime;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
