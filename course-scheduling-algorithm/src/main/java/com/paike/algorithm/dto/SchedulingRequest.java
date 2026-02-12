package com.paike.algorithm.dto;

import java.util.List;

public class SchedulingRequest {

    private String semester;
    private List<Long> taskIds;
    private Integer daysPerWeek;
    private Integer slotsPerDay;
    private String algorithmType;
    private Integer maxGenerations;
    private Double targetFitness;

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public List<Long> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<Long> taskIds) {
        this.taskIds = taskIds;
    }

    public Integer getDaysPerWeek() {
        return daysPerWeek;
    }

    public void setDaysPerWeek(Integer daysPerWeek) {
        this.daysPerWeek = daysPerWeek;
    }

    public Integer getSlotsPerDay() {
        return slotsPerDay;
    }

    public void setSlotsPerDay(Integer slotsPerDay) {
        this.slotsPerDay = slotsPerDay;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    public Integer getMaxGenerations() {
        return maxGenerations;
    }

    public void setMaxGenerations(Integer maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    public Double getTargetFitness() {
        return targetFitness;
    }

    public void setTargetFitness(Double targetFitness) {
        this.targetFitness = targetFitness;
    }
}
