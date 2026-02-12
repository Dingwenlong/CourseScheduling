package com.paike.common.constants;

public enum TaskStatus {

    PENDING("PENDING", "待排课"),
    SCHEDULED("SCHEDULED", "已排课"),
    ADJUSTING("ADJUSTING", "调整中"),
    COMPLETED("COMPLETED", "已完成");

    private final String code;
    private final String desc;

    TaskStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TaskStatus fromCode(String code) {
        for (TaskStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
