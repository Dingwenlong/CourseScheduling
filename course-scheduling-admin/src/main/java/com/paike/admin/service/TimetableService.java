package com.paike.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paike.admin.entity.Timetable;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.algorithm.dto.SchedulingResult;

import java.util.List;

public interface TimetableService extends IService<Timetable> {

    Timetable generateTimetable(SchedulingRequest request);

    Timetable publishTimetable(Long timetableId);

    Timetable archiveTimetable(Long timetableId);

    Timetable getLatestTimetable(String semester);

    List<Timetable> listBySemester(String semester);

    void deleteTimetable(Long timetableId);
}
