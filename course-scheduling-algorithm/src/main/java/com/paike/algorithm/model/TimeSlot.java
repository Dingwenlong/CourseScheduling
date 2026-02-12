package com.paike.algorithm.model;

import java.io.Serializable;

public class TimeSlot implements Serializable, Comparable<TimeSlot> {

    private static final long serialVersionUID = 1L;

    private Integer dayOfWeek;
    private Integer slotNo;

    public TimeSlot() {
    }

    public TimeSlot(Integer dayOfWeek, Integer slotNo) {
        this.dayOfWeek = dayOfWeek;
        this.slotNo = slotNo;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getSlotNo() {
        return slotNo;
    }

    public void setSlotNo(Integer slotNo) {
        this.slotNo = slotNo;
    }

    public static TimeSlot of(Integer dayOfWeek, Integer slotNo) {
        return new TimeSlot(dayOfWeek, slotNo);
    }

    @Override
    public int compareTo(TimeSlot other) {
        int dayCompare = this.dayOfWeek.compareTo(other.dayOfWeek);
        if (dayCompare != 0) {
            return dayCompare;
        }
        return this.slotNo.compareTo(other.slotNo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) obj;
        return dayOfWeek.equals(timeSlot.dayOfWeek) && slotNo.equals(timeSlot.slotNo);
    }

    @Override
    public int hashCode() {
        return 31 * dayOfWeek + slotNo;
    }

    @Override
    public String toString() {
        return "周" + dayOfWeek + "第" + slotNo + "节";
    }
}
