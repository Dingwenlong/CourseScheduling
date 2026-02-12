package com.paike.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("edu_classroom")
public class Classroom extends BaseEntity {

    private String roomNo;
    private String roomName;
    private Long campusId;
    private String building;
    private Integer floor;
    private Integer capacity;

    @TableField("`room_type`")
    private String roomType;

    private Integer hasProjector;
    private Integer hasMicrophone;
    private Integer hasAirConditioner;
    private String equipmentDesc;
    private Integer status;

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

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
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

    public String getEquipmentDesc() {
        return equipmentDesc;
    }

    public void setEquipmentDesc(String equipmentDesc) {
        this.equipmentDesc = equipmentDesc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
