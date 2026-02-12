package com.paike.common.constants;

public enum CourseType {

    REQUIRED("REQUIRED", "必修课"),
    ELECTIVE("ELECTIVE", "选修课"),
    PRACTICE("PRACTICE", "实践课"),
    EXPERIMENT("EXPERIMENT", "实验课");

    private final String code;
    private final String desc;

    CourseType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CourseType fromCode(String code) {
        for (CourseType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return REQUIRED;
    }
}
