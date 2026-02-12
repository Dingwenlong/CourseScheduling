package com.paike.common.exception;

import com.paike.common.result.ResultCode;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    public static BusinessException of(ResultCode resultCode) {
        return new BusinessException(resultCode);
    }

    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }
}
