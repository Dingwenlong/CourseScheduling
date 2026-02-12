package com.paike.adapter.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserDataParser implements DataParser<Map<String, Object>> {

    @Override
    public List<Map<String, Object>> parse(String data) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> parse(byte[] data) {
        return new ArrayList<>();
    }

    @Override
    public String export(List<Map<String, Object>> data) {
        return "";
    }

    @Override
    public boolean supports(String format) {
        return "user".equalsIgnoreCase(format);
    }
}
