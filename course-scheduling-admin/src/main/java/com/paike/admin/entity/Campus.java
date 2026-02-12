package com.paike.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_campus")
public class Campus extends BaseEntity {

    private String campusCode;
    private String campusName;
    private String address;
    private Integer commuteTime;
    private Integer sortOrder;
    private Integer status;

    public String getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(String campusCode) {
        this.campusCode = campusCode;
    }

    public String getCampusName() {
        return campusName;
    }

    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getCommuteTime() {
        return commuteTime;
    }

    public void setCommuteTime(Integer commuteTime) {
        this.commuteTime = commuteTime;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
