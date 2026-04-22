package com.paike.admin.dto;

import java.util.List;
import java.util.Map;

public class DataSyncDatasetPreview {

    private String dataset;
    private String label;
    private Integer totalCount;
    private Integer readyCount;
    private Integer createCount;
    private Integer updateCount;
    private Integer skippedCount;
    private List<String> warnings;
    private List<Map<String, String>> sampleRows;

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getReadyCount() {
        return readyCount;
    }

    public void setReadyCount(Integer readyCount) {
        this.readyCount = readyCount;
    }

    public Integer getCreateCount() {
        return createCount;
    }

    public void setCreateCount(Integer createCount) {
        this.createCount = createCount;
    }

    public Integer getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(Integer updateCount) {
        this.updateCount = updateCount;
    }

    public Integer getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(Integer skippedCount) {
        this.skippedCount = skippedCount;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<Map<String, String>> getSampleRows() {
        return sampleRows;
    }

    public void setSampleRows(List<Map<String, String>> sampleRows) {
        this.sampleRows = sampleRows;
    }
}
