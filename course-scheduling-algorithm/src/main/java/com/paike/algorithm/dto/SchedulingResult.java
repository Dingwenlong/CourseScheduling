package com.paike.algorithm.dto;

import com.paike.algorithm.model.ScheduledTask;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SchedulingResult {

    private Boolean success;
    private String message;
    private List<ScheduledTask> scheduledTasks;
    private Integer totalTasks;
    private Integer scheduledCount;
    private Integer conflictCount;
    private BigDecimal utilizationRate;
    private BigDecimal satisfactionScore;
    private Integer generations;
    private Long executionTime;
    private LocalDateTime createTime;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ScheduledTask> getScheduledTasks() {
        return scheduledTasks;
    }

    public void setScheduledTasks(List<ScheduledTask> scheduledTasks) {
        this.scheduledTasks = scheduledTasks;
    }

    public Integer getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Integer totalTasks) {
        this.totalTasks = totalTasks;
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

    public Integer getGenerations() {
        return generations;
    }

    public void setGenerations(Integer generations) {
        this.generations = generations;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public static SchedulingResult success(List<ScheduledTask> tasks, int total, int scheduled, int conflicts) {
        SchedulingResult result = new SchedulingResult();
        result.setSuccess(true);
        result.setMessage("排课成功");
        result.setScheduledTasks(tasks);
        result.setTotalTasks(total);
        result.setScheduledCount(scheduled);
        result.setConflictCount(conflicts);
        result.setCreateTime(LocalDateTime.now());
        return result;
    }

    public static SchedulingResult fail(String message) {
        SchedulingResult result = new SchedulingResult();
        result.setSuccess(false);
        result.setMessage(message);
        result.setCreateTime(LocalDateTime.now());
        return result;
    }
}
