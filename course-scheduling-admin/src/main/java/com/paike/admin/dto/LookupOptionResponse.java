package com.paike.admin.dto;

public class LookupOptionResponse {

    private Long value;
    private String label;
    private String code;

    public static LookupOptionResponse of(Long value, String label, String code) {
        LookupOptionResponse response = new LookupOptionResponse();
        response.setValue(value);
        response.setLabel(label);
        response.setCode(code);
        return response;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
