package com.paike.admin.dto;

import java.util.List;

public class AdjustmentResult {

    private Boolean success;
    private String message;
    private List<String> conflicts;

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

    public List<String> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<String> conflicts) {
        this.conflicts = conflicts;
    }

    public static AdjustmentResult success(String message) {
        AdjustmentResult result = new AdjustmentResult();
        result.setSuccess(true);
        result.setMessage(message);
        return result;
    }

    public static AdjustmentResult fail(String message, List<String> conflicts) {
        AdjustmentResult result = new AdjustmentResult();
        result.setSuccess(false);
        result.setMessage(message);
        result.setConflicts(conflicts);
        return result;
    }
}
