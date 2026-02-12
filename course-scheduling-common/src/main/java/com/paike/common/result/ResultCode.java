package com.paike.common.result;

public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "方法不允许"),
    SYSTEM_ERROR(500, "系统错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_ALREADY_EXISTS(1004, "用户已存在"),
    TOKEN_EXPIRED(1005, "Token已过期"),
    TOKEN_INVALID(1006, "Token无效"),

    DATA_NOT_FOUND(2001, "数据不存在"),
    DATA_ALREADY_EXISTS(2002, "数据已存在"),
    DATA_INTEGRITY_VIOLATION(2003, "数据完整性冲突"),

    SCHEDULE_CONFLICT(3001, "排课冲突"),
    SCHEDULE_TIME_CONFLICT(3002, "时间冲突"),
    SCHEDULE_CLASSROOM_CONFLICT(3003, "教室冲突"),
    SCHEDULE_TEACHER_CONFLICT(3004, "教师冲突"),
    SCHEDULE_CLASS_CONFLICT(3005, "班级冲突");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
