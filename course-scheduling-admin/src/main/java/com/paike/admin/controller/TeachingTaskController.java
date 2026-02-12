package com.paike.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paike.admin.entity.TeachingTask;
import com.paike.admin.mapper.TeachingTaskMapper;
import com.paike.common.result.PageResult;
import com.paike.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教学任务管理", description = "教学任务相关接口")
@RestController
@RequestMapping("/task")
public class TeachingTaskController {

    @Autowired
    private TeachingTaskMapper teachingTaskMapper;

    @Operation(summary = "分页查询教学任务")
    @GetMapping("/page")
    public Result<PageResult<TeachingTask>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String semester) {
        
        Page<TeachingTask> page = new Page<>(current, size);
        LambdaQueryWrapper<TeachingTask> wrapper = new LambdaQueryWrapper<>();
        
        if (semester != null && !semester.isEmpty()) {
            wrapper.eq(TeachingTask::getSemester, semester);
        }
        
        wrapper.orderByDesc(TeachingTask::getCreateTime);
        Page<TeachingTask> result = teachingTaskMapper.selectPage(page, wrapper);
        
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent()));
    }

    @Operation(summary = "根据ID查询教学任务")
    @GetMapping("/{id}")
    public Result<TeachingTask> getById(@PathVariable Long id) {
        TeachingTask task = teachingTaskMapper.selectById(id);
        return Result.success(task);
    }

    @Operation(summary = "新增教学任务")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> add(@RequestBody TeachingTask task) {
        teachingTaskMapper.insert(task);
        return Result.success();
    }

    @Operation(summary = "更新教学任务")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public Result<Void> update(@RequestBody TeachingTask task) {
        teachingTaskMapper.updateById(task);
        return Result.success();
    }

    @Operation(summary = "删除教学任务")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        teachingTaskMapper.deleteById(id);
        return Result.success();
    }
}
