package com.paike.admin.dto;

import java.util.List;
import java.util.Map;

public class DataSyncRequest {

    private String provider;
    private Long defaultDeptId;
    private Long defaultCampusId;
    private Map<String, Map<String, String>> mappings;
    private Map<String, List<Map<String, Object>>> payload;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Long getDefaultDeptId() {
        return defaultDeptId;
    }

    public void setDefaultDeptId(Long defaultDeptId) {
        this.defaultDeptId = defaultDeptId;
    }

    public Long getDefaultCampusId() {
        return defaultCampusId;
    }

    public void setDefaultCampusId(Long defaultCampusId) {
        this.defaultCampusId = defaultCampusId;
    }

    public Map<String, Map<String, String>> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, Map<String, String>> mappings) {
        this.mappings = mappings;
    }

    public Map<String, List<Map<String, Object>>> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, List<Map<String, Object>>> payload) {
        this.payload = payload;
    }
}
