package com.paike.algorithm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SchedulingContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private String semester;
    private List<Long> taskIds;
    private List<Long> classroomIds;
    private List<Long> teacherIds;
    private List<Long> classIds;
    private List<TimeSlot> availableSlots;
    private Integer daysPerWeek;
    private Integer slotsPerDay;

    public SchedulingContext() {
        this.taskIds = new ArrayList<>();
        this.classroomIds = new ArrayList<>();
        this.teacherIds = new ArrayList<>();
        this.classIds = new ArrayList<>();
        this.availableSlots = new ArrayList<>();
        this.daysPerWeek = 7;
        this.slotsPerDay = 12;
    }

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

    public List<Long> getClassroomIds() {
        return classroomIds;
    }

    public void setClassroomIds(List<Long> classroomIds) {
        this.classroomIds = classroomIds;
    }

    public List<Long> getTeacherIds() {
        return teacherIds;
    }

    public void setTeacherIds(List<Long> teacherIds) {
        this.teacherIds = teacherIds;
    }

    public List<Long> getClassIds() {
        return classIds;
    }

    public void setClassIds(List<Long> classIds) {
        this.classIds = classIds;
    }

    public List<TimeSlot> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<TimeSlot> availableSlots) {
        this.availableSlots = availableSlots;
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

    public void initAvailableSlots() {
        availableSlots.clear();
        for (int day = 1; day <= daysPerWeek; day++) {
            for (int slot = 1; slot <= slotsPerDay; slot++) {
                availableSlots.add(new TimeSlot(day, slot));
            }
        }
    }
}
