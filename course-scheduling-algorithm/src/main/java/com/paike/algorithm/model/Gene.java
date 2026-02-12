package com.paike.algorithm.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Gene implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long taskId;
    private TimeSlot timeSlot;
    private Long classroomId;
    private Map<String, Object> properties;

    public Gene() {
        this.properties = new HashMap<>();
    }

    public Gene(Long taskId, TimeSlot timeSlot, Long classroomId) {
        this.taskId = taskId;
        this.timeSlot = timeSlot;
        this.classroomId = classroomId;
        this.properties = new HashMap<>();
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public static Gene of(Long taskId, TimeSlot timeSlot, Long classroomId) {
        return new Gene(taskId, timeSlot, classroomId);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Gene clone() {
        Gene gene = new Gene(this.taskId, this.timeSlot, this.classroomId);
        gene.setProperties(new HashMap<>(this.properties));
        return gene;
    }
}
