package com.paike.adapter.sync;

import java.time.LocalDateTime;

public class SyncResult {

    private Boolean success;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
    private String systemName;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public static SyncResult success(int total, int success, int fail) {
        SyncResult result = new SyncResult();
        result.setSuccess(true);
        result.setTotalCount(total);
        result.setSuccessCount(success);
        result.setFailCount(fail);
        result.setEndTime(LocalDateTime.now());
        return result;
    }

    public static SyncResult fail(String errorMessage) {
        SyncResult result = new SyncResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.setEndTime(LocalDateTime.now());
        return result;
    }
}
