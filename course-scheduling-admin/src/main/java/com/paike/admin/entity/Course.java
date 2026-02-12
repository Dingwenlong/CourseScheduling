package com.paike.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("edu_course")
public class Course extends BaseEntity {

    private String courseCode;
    private String courseName;

    @TableField("`course_type`")
    private String courseType;

    private BigDecimal credit;
    private Integer theoryHours;
    private Integer practiceHours;
    private Integer totalHours;
    private Long deptId;
    private Integer priority;
    private Integer needMultimedia;
    private Integer needLab;
    private Integer minCapacity;
    private Integer maxCapacity;
    private String courseDesc;
    private Integer status;

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public Integer getTheoryHours() {
        return theoryHours;
    }

    public void setTheoryHours(Integer theoryHours) {
        this.theoryHours = theoryHours;
    }

    public Integer getPracticeHours() {
        return practiceHours;
    }

    public void setPracticeHours(Integer practiceHours) {
        this.practiceHours = practiceHours;
    }

    public Integer getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Integer totalHours) {
        this.totalHours = totalHours;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getNeedMultimedia() {
        return needMultimedia;
    }

    public void setNeedMultimedia(Integer needMultimedia) {
        this.needMultimedia = needMultimedia;
    }

    public Integer getNeedLab() {
        return needLab;
    }

    public void setNeedLab(Integer needLab) {
        this.needLab = needLab;
    }

    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getCourseDesc() {
        return courseDesc;
    }

    public void setCourseDesc(String courseDesc) {
        this.courseDesc = courseDesc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
