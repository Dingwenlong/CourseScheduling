package com.paike.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.paike.admin.entity.TimetableDetail;

import java.util.List;

public interface TimetableDetailService extends IService<TimetableDetail> {

    List<TimetableDetail> listByTimetableId(Long timetableId);

    List<TimetableDetail> listByClassId(Long timetableId, Long classId);

    List<TimetableDetail> listByTeacherId(Long timetableId, Long teacherId);

    List<TimetableDetail> listByClassroomId(Long timetableId, Long classroomId);

    void saveScheduleResult(Long timetableId, com.paike.algorithm.dto.SchedulingResult result);

    int countConflicts(Long timetableId);

    void markConflicts(Long timetableId);
}
