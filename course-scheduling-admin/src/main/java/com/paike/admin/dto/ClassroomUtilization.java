package com.paike.admin.dto;

import java.math.BigDecimal;

public class ClassroomUtilization {

    private Long classroomId;
    private String classroomName;
    private Integer totalSlots;
    private Integer usedSlots;
    private BigDecimal utilizationRate;

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

    public Integer getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(Integer totalSlots) {
        this.totalSlots = totalSlots;
    }

    public Integer getUsedSlots() {
        return usedSlots;
    }

    public void setUsedSlots(Integer usedSlots) {
        this.usedSlots = usedSlots;
    }

    public BigDecimal getUtilizationRate() {
        return utilizationRate;
    }

    public void setUtilizationRate(BigDecimal utilizationRate) {
        this.utilizationRate = utilizationRate;
    }
}
