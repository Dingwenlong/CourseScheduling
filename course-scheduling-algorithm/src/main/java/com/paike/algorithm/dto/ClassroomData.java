package com.paike.algorithm.dto;

import java.io.Serializable;

public class ClassroomData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long classroomId;
    private String roomNo;
    private String roomName;
    private Long campusId;
    private String campusName;
    private Integer capacity;
    private String roomType;
    private Integer hasProjector;
    private Integer hasMicrophone;
    private Integer hasAirConditioner;

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }

    public String getCampusName() {
        return campusName;
    }

    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getHasProjector() {
        return hasProjector;
    }

    public void setHasProjector(Integer hasProjector) {
        this.hasProjector = hasProjector;
    }

    public Integer getHasMicrophone() {
        return hasMicrophone;
    }

    public void setHasMicrophone(Integer hasMicrophone) {
        this.hasMicrophone = hasMicrophone;
    }

    public Integer getHasAirConditioner() {
        return hasAirConditioner;
    }

    public void setHasAirConditioner(Integer hasAirConditioner) {
        this.hasAirConditioner = hasAirConditioner;
    }

    public boolean matches(TaskData task) {
        if (capacity != null && task.getStudentCount() != null) {
            if (capacity < task.getStudentCount()) {
                return false;
            }
        }
        if ("LAB".equals(roomType) && task.getNeedLab() != null && task.getNeedLab() == 1) {
            return true;
        }
        if ("MULTIMEDIA".equals(roomType) && task.getNeedMultimedia() != null && task.getNeedMultimedia() == 1) {
            return true;
        }
        if ("GENERAL".equals(roomType)) {
            if (task.getNeedLab() == null || task.getNeedLab() != 1) {
                return true;
            }
        }
        return true;
    }
}
