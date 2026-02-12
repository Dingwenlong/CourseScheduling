package com.paike.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paike.admin.entity.Timetable;
import com.paike.admin.entity.TimetableDetail;
import com.paike.admin.mapper.TimetableMapper;
import com.paike.admin.service.TimetableDetailService;
import com.paike.admin.service.TimetableService;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.common.result.PageResult;
import com.paike.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "课表管理", description = "课表相关接口")
@RestController
@RequestMapping("/timetable")
public class TimetableController {

    @Autowired
    private TimetableService timetableService;

    @Autowired
    private TimetableDetailService timetableDetailService;

    @Autowired
    private TimetableMapper timetableMapper;

    @Operation(summary = "生成课表")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/generate")
    public Result<Timetable> generate(@RequestBody SchedulingRequest request) {
        Timetable timetable = timetableService.generateTimetable(request);
        return Result.success(timetable);
    }

    @Operation(summary = "发布课表")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/publish")
    public Result<Timetable> publish(@PathVariable Long id) {
        Timetable timetable = timetableService.publishTimetable(id);
        return Result.success(timetable);
    }

    @Operation(summary = "归档课表")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/archive")
    public Result<Timetable> archive(@PathVariable Long id) {
        Timetable timetable = timetableService.archiveTimetable(id);
        return Result.success(timetable);
    }

    @Operation(summary = "删除课表")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        timetableService.deleteTimetable(id);
        return Result.success();
    }

    @Operation(summary = "分页查询课表")
    @GetMapping("/page")
    public Result<PageResult<Timetable>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String semester) {
        
        Page<Timetable> page = new Page<>(current, size);
        LambdaQueryWrapper<Timetable> wrapper = new LambdaQueryWrapper<>();
        
        if (semester != null && !semester.isEmpty()) {
            wrapper.eq(Timetable::getSemester, semester);
        }
        
        wrapper.orderByDesc(Timetable::getCreateTime);
        Page<Timetable> result = timetableMapper.selectPage(page, wrapper);
        
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent()));
    }

    @Operation(summary = "根据ID查询课表")
    @GetMapping("/{id}")
    public Result<Timetable> getById(@PathVariable Long id) {
        Timetable timetable = timetableService.getById(id);
        return Result.success(timetable);
    }

    @Operation(summary = "查询课表明细")
    @GetMapping("/{id}/details")
    public Result<List<TimetableDetail>> getDetails(@PathVariable Long id) {
        List<TimetableDetail> details = timetableDetailService.listByTimetableId(id);
        return Result.success(details);
    }

    @Operation(summary = "查询班级课表")
    @GetMapping("/{id}/class/{classId}")
    public Result<List<TimetableDetail>> getClassTimetable(
            @PathVariable Long id,
            @PathVariable Long classId) {
        List<TimetableDetail> details = timetableDetailService.listByClassId(id, classId);
        return Result.success(details);
    }

    @Operation(summary = "查询教师课表")
    @GetMapping("/{id}/teacher/{teacherId}")
    public Result<List<TimetableDetail>> getTeacherTimetable(
            @PathVariable Long id,
            @PathVariable Long teacherId) {
        List<TimetableDetail> details = timetableDetailService.listByTeacherId(id, teacherId);
        return Result.success(details);
    }

    @Operation(summary = "查询教室课表")
    @GetMapping("/{id}/classroom/{classroomId}")
    public Result<List<TimetableDetail>> getClassroomTimetable(
            @PathVariable Long id,
            @PathVariable Long classroomId) {
        List<TimetableDetail> details = timetableDetailService.listByClassroomId(id, classroomId);
        return Result.success(details);
    }

    @Operation(summary = "获取最新课表")
    @GetMapping("/latest")
    public Result<Timetable> getLatest(@RequestParam String semester) {
        Timetable timetable = timetableService.getLatestTimetable(semester);
        return Result.success(timetable);
    }

    @Operation(summary = "查询冲突记录")
    @GetMapping("/{id}/conflicts")
    public Result<List<TimetableDetail>> getConflicts(@PathVariable Long id) {
        List<TimetableDetail> conflicts = timetableDetailService.list(new LambdaQueryWrapper<TimetableDetail>()
                .eq(TimetableDetail::getTimetableId, id)
                .eq(TimetableDetail::getIsConflict, 1));
        return Result.success(conflicts);
    }
}
