package com.paike.adapter.config;

public class SyncConfig {

    private Long id;
    private String systemName;
    private String systemCode;
    private String systemType;
    private String apiUrl;
    private String authType;
    private String authConfig;
    private String fieldMapping;
    private Integer syncInterval;
    private String lastSyncTime;
    private Integer lastSyncStatus;
    private String lastSyncMsg;
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(String authConfig) {
        this.authConfig = authConfig;
    }

    public String getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(String fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public Integer getSyncInterval() {
        return syncInterval;
    }

    public void setSyncInterval(Integer syncInterval) {
        this.syncInterval = syncInterval;
    }

    public String getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(String lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public Integer getLastSyncStatus() {
        return lastSyncStatus;
    }

    public void setLastSyncStatus(Integer lastSyncStatus) {
        this.lastSyncStatus = lastSyncStatus;
    }

    public String getLastSyncMsg() {
        return lastSyncMsg;
    }

    public void setLastSyncMsg(String lastSyncMsg) {
        this.lastSyncMsg = lastSyncMsg;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
