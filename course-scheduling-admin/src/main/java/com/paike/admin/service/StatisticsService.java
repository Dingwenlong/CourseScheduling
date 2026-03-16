package com.paike.admin.service;

import com.paike.admin.dto.ClassroomUtilization;
import com.paike.admin.dto.ConflictReport;
import com.paike.admin.dto.StatisticsOverview;
import com.paike.admin.dto.TeacherWorkload;

import java.util.List;

public interface StatisticsService {

    StatisticsOverview getOverview(Long timetableId);

    List<ClassroomUtilization> getClassroomUtilization(Long timetableId);

    List<TeacherWorkload> getTeacherWorkload(Long timetableId);

    ConflictReport getConflictReport(Long timetableId);

    Integer getTotalScheduledHours(Long timetableId);

    Integer getCourseCount(Long timetableId);
}
