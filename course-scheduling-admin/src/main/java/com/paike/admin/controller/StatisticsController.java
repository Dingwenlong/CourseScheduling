package com.paike.admin.controller;

import com.paike.admin.dto.ClassroomUtilization;
import com.paike.admin.dto.ConflictReport;
import com.paike.admin.dto.StatisticsOverview;
import com.paike.admin.dto.TeacherWorkload;
import com.paike.admin.service.StatisticsService;
import com.paike.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "统计分析", description = "统计相关接口")
@RestController
@RequestMapping("/statistics")
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Operation(summary = "统计概览")
    @GetMapping("/overview/{timetableId}")
    public Result<StatisticsOverview> getOverview(@PathVariable Long timetableId) {
        StatisticsOverview overview = statisticsService.getOverview(timetableId);
        return Result.success(overview);
    }

    @Operation(summary = "教室利用率统计")
    @GetMapping("/classroom-utilization/{timetableId}")
    public Result<List<ClassroomUtilization>> getClassroomUtilization(@PathVariable Long timetableId) {
        List<ClassroomUtilization> result = statisticsService.getClassroomUtilization(timetableId);
        return Result.success(result);
    }

    @Operation(summary = "教师工作量统计")
    @GetMapping("/teacher-workload/{timetableId}")
    public Result<List<TeacherWorkload>> getTeacherWorkload(@PathVariable Long timetableId) {
        List<TeacherWorkload> result = statisticsService.getTeacherWorkload(timetableId);
        return Result.success(result);
    }

    @Operation(summary = "冲突报告")
    @GetMapping("/conflict-report/{timetableId}")
    public Result<ConflictReport> getConflictReport(@PathVariable Long timetableId) {
        ConflictReport report = statisticsService.getConflictReport(timetableId);
        return Result.success(report);
    }

    @Operation(summary = "总排课学时")
    @GetMapping("/total-hours/{timetableId}")
    public Result<Integer> getTotalHours(@PathVariable Long timetableId) {
        Integer hours = statisticsService.getTotalScheduledHours(timetableId);
        return Result.success(hours);
    }

    @Operation(summary = "课程数量统计")
    @GetMapping("/course-count/{timetableId}")
    public Result<Integer> getCourseCount(@PathVariable Long timetableId) {
        Integer count = statisticsService.getCourseCount(timetableId);
        return Result.success(count);
    }
}
