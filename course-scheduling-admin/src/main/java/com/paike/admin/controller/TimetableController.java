package com.paike.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paike.admin.dto.TimetableGenerationJobStatus;
import com.paike.admin.entity.Timetable;
import com.paike.admin.entity.TimetableDetail;
import com.paike.admin.mapper.TimetableMapper;
import com.paike.admin.service.TimetableDetailService;
import com.paike.admin.service.TimetableGenerationJobService;
import com.paike.admin.service.TimetableService;
import com.paike.admin.utils.SecurityUtils;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.common.exception.BusinessException;
import com.paike.common.result.PageResult;
import com.paike.common.result.Result;
import com.paike.common.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
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

    @Autowired
    private TimetableGenerationJobService timetableGenerationJobService;

    @Autowired
    private SecurityUtils securityUtils;

    @Operation(summary = "生成课表")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/generate")
    public Result<Timetable> generate(@RequestBody SchedulingRequest request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (!StringUtils.hasText(request.getSemester())) {
            throw new BusinessException("学期不能为空");
        }
        Timetable timetable = timetableService.generateTimetable(request);
        return Result.success(timetable);
    }

    @Operation(summary = "异步生成课表")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/generate-async")
    public Result<TimetableGenerationJobStatus> generateAsync(@RequestBody SchedulingRequest request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (!StringUtils.hasText(request.getSemester())) {
            throw new BusinessException("学期不能为空");
        }
        return Result.success(timetableGenerationJobService.submit(request));
    }

    @Operation(summary = "查询异步生成课表任务")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/generate-jobs/{jobId}")
    public Result<TimetableGenerationJobStatus> getGenerateJob(@PathVariable String jobId) {
        if (!StringUtils.hasText(jobId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return Result.success(timetableGenerationJobService.getJob(jobId));
    }

    @Operation(summary = "发布课表")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/publish")
    public Result<Timetable> publish(@PathVariable Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        Timetable timetable = timetableService.publishTimetable(id);
        return Result.success(timetable);
    }

    @Operation(summary = "归档课表")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/archive")
    public Result<Timetable> archive(@PathVariable Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        Timetable timetable = timetableService.archiveTimetable(id);
        return Result.success(timetable);
    }

    @Operation(summary = "删除课表")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        timetableService.deleteTimetable(id);
        return Result.success();
    }

    @Operation(summary = "分页查询课表")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public Result<Timetable> getById(@PathVariable Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        Timetable timetable = timetableService.getById(id);
        if (timetable == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return Result.success(timetable);
    }

    @Operation(summary = "查询课表明细")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/details")
    public Result<List<TimetableDetail>> getDetails(@PathVariable Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        List<TimetableDetail> details = timetableDetailService.listByTimetableId(id);
        return Result.success(securityUtils.filterTimetableDetails(details));
    }

    @Operation(summary = "查询班级课表")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/class/{classId}")
    public Result<List<TimetableDetail>> getClassTimetable(
            @PathVariable Long id,
            @PathVariable Long classId) {
        if (id == null || classId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        securityUtils.checkClassAccess(classId);
        List<TimetableDetail> details = timetableDetailService.listByClassId(id, classId);
        return Result.success(details);
    }

    @Operation(summary = "查询教师课表")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/teacher/{teacherId}")
    public Result<List<TimetableDetail>> getTeacherTimetable(
            @PathVariable Long id,
            @PathVariable Long teacherId) {
        if (id == null || teacherId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        securityUtils.checkTeacherAccess(teacherId);
        List<TimetableDetail> details = timetableDetailService.listByTeacherId(id, teacherId);
        return Result.success(details);
    }

    @Operation(summary = "查询教室课表")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/classroom/{classroomId}")
    public Result<List<TimetableDetail>> getClassroomTimetable(
            @PathVariable Long id,
            @PathVariable Long classroomId) {
        if (id == null || classroomId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        securityUtils.checkClassroomAccess();
        List<TimetableDetail> details = timetableDetailService.listByClassroomId(id, classroomId);
        return Result.success(details);
    }

    @Operation(summary = "获取最新课表")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/latest")
    public Result<Timetable> getLatest(@RequestParam String semester) {
        if (!StringUtils.hasText(semester)) {
            throw new BusinessException("学期不能为空");
        }
        Timetable timetable = timetableService.getLatestTimetable(semester);
        return Result.success(timetable);
    }

    @Operation(summary = "查询冲突记录")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/conflicts")
    public Result<List<TimetableDetail>> getConflicts(@PathVariable Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        List<TimetableDetail> conflicts = timetableDetailService.list(new LambdaQueryWrapper<TimetableDetail>()
                .eq(TimetableDetail::getTimetableId, id)
                .eq(TimetableDetail::getIsConflict, 1));
        return Result.success(securityUtils.filterTimetableDetails(conflicts));
    }
}
