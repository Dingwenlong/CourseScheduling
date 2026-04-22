package com.paike.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DataSyncPreviewResponse {

    private String provider;
    private Long defaultDeptId;
    private Long defaultCampusId;
    private LocalDateTime previewTime;
    private List<DataSyncDatasetPreview> datasets;

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

    public LocalDateTime getPreviewTime() {
        return previewTime;
    }

    public void setPreviewTime(LocalDateTime previewTime) {
        this.previewTime = previewTime;
    }

    public List<DataSyncDatasetPreview> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DataSyncDatasetPreview> datasets) {
        this.datasets = datasets;
    }
}
