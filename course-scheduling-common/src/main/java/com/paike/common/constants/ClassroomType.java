package com.paike.common.constants;

public enum ClassroomType {

    GENERAL("GENERAL", "普通教室"),
    MULTIMEDIA("MULTIMEDIA", "多媒体教室"),
    LAB("LAB", "实验室"),
    COMPUTER("COMPUTER", "计算机房"),
    LECTURE_HALL("LECTURE_HALL", "阶梯教室");

    private final String code;
    private final String desc;

    ClassroomType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ClassroomType fromCode(String code) {
        for (ClassroomType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return GENERAL;
    }
}
