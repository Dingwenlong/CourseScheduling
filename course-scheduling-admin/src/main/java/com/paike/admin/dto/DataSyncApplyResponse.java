package com.paike.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DataSyncApplyResponse {

    private String provider;
    private LocalDateTime syncTime;
    private Integer totalCreated;
    private Integer totalUpdated;
    private Integer totalSkipped;
    private List<DataSyncDatasetApplyResult> datasets;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDateTime getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(LocalDateTime syncTime) {
        this.syncTime = syncTime;
    }

    public Integer getTotalCreated() {
        return totalCreated;
    }

    public void setTotalCreated(Integer totalCreated) {
        this.totalCreated = totalCreated;
    }

    public Integer getTotalUpdated() {
        return totalUpdated;
    }

    public void setTotalUpdated(Integer totalUpdated) {
        this.totalUpdated = totalUpdated;
    }

    public Integer getTotalSkipped() {
        return totalSkipped;
    }

    public void setTotalSkipped(Integer totalSkipped) {
        this.totalSkipped = totalSkipped;
    }

    public List<DataSyncDatasetApplyResult> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DataSyncDatasetApplyResult> datasets) {
        this.datasets = datasets;
    }
}
