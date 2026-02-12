package com.paike.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("edu_class")
public class Clazz extends BaseEntity {

    private String classCode;
    private String className;
    private Long deptId;
    private String grade;
    private Integer studentCount;
    private String counselorName;
    private String counselorPhone;
    private Integer status;

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public String getCounselorName() {
        return counselorName;
    }

    public void setCounselorName(String counselorName) {
        this.counselorName = counselorName;
    }

    public String getCounselorPhone() {
        return counselorPhone;
    }

    public void setCounselorPhone(String counselorPhone) {
        this.counselorPhone = counselorPhone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
