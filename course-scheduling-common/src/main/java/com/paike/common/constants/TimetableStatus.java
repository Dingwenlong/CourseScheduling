package com.paike.common.constants;

public enum TimetableStatus {

    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String desc;

    TimetableStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static TimetableStatus fromCode(String code) {
        for (TimetableStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return DRAFT;
    }
}
