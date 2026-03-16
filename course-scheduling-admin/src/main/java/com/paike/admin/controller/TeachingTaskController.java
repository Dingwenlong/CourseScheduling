package com.paike.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paike.admin.dto.TeachingTaskCreateRequest;
import com.paike.admin.dto.TeachingTaskQueryRequest;
import com.paike.admin.dto.TeachingTaskUpdateRequest;
import com.paike.admin.entity.Course;
import com.paike.admin.entity.TeachingTask;
import com.paike.admin.mapper.CourseMapper;
import com.paike.admin.service.TeachingTaskService;
import com.paike.common.constants.TaskStatus;
import com.paike.common.result.PageResult;
import com.paike.common.result.Result;
import com.paike.common.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "教学任务管理", description = "教学任务相关接口")
@RestController
@RequestMapping("/task")
public class TeachingTaskController {

    @Autowired
    private TeachingTaskService teachingTaskService;

    @Autowired
    private CourseMapper courseMapper;

    @Operation(summary = "分页查询教学任务")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<PageResult<TeachingTask>> page(@Valid TeachingTaskQueryRequest request) {
        Page<TeachingTask> page = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<TeachingTask> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getSemester())) {
            wrapper.eq(TeachingTask::getSemester, request.getSemester());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(TeachingTask::getStatus, request.getStatus());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            List<Long> matchedCourseIds = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                            .like(Course::getCourseName, request.getKeyword())
                            .or()
                            .like(Course::getCourseCode, request.getKeyword()))
                    .stream()
                    .map(Course::getId)
                    .toList();
            if (matchedCourseIds.isEmpty()) {
                return Result.success(PageResult.of(List.of(), 0L, request.getSize().longValue(), request.getCurrent().longValue()));
            }
            wrapper.in(TeachingTask::getCourseId, matchedCourseIds);
        }

        wrapper.orderByDesc(TeachingTask::getCreateTime);
        Page<TeachingTask> result = teachingTaskService.page(page, wrapper);

        populateCourseNames(result.getRecords());
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), (long) result.getSize(), (long) result.getCurrent()));
    }

    @Operation(summary = "根据ID查询教学任务")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<TeachingTask> getById(@PathVariable Long id) {
        TeachingTask task = teachingTaskService.getById(id);
        if (task == null) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "教学任务不存在");
        }
        populateCourseNames(List.of(task));
        return Result.success(task);
    }

    private void populateCourseNames(List<TeachingTask> tasks) {
        Set<Long> courseIds = tasks.stream()
                .map(TeachingTask::getCourseId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (courseIds.isEmpty()) {
            return;
        }

        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        Map<Long, String> courseNameMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, Course::getCourseName));

        for (TeachingTask task : tasks) {
            if (task.getCourseId() != null) {
                task.setCourseName(courseNameMap.get(task.getCourseId()));
            }
        }
    }

    @Operation(summary = "新增教学任务")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody TeachingTaskCreateRequest request) {
        TeachingTask task = new TeachingTask();
        BeanUtils.copyProperties(request, task);
        if (!StringUtils.hasText(task.getStatus())) {
            task.setStatus(TaskStatus.PENDING.getCode());
        }
        teachingTaskService.save(task);
        return Result.success();
    }

    @Operation(summary = "更新教学任务")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody TeachingTaskUpdateRequest request) {
        TeachingTask existingTask = teachingTaskService.getById(request.getId());
        if (existingTask == null) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "教学任务不存在");
        }

        TeachingTask task = new TeachingTask();
        BeanUtils.copyProperties(request, task);
        if (!StringUtils.hasText(task.getStatus())) {
            task.setStatus(StringUtils.hasText(existingTask.getStatus()) ? existingTask.getStatus() : TaskStatus.PENDING.getCode());
        }
        teachingTaskService.updateById(task);
        return Result.success();
    }

    @Operation(summary = "删除教学任务")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        TeachingTask task = teachingTaskService.getById(id);
        if (task == null) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "教学任务不存在");
        }
        teachingTaskService.removeById(id);
        return Result.success();
    }
}
