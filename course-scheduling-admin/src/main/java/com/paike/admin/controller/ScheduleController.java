package com.paike.admin.controller;

import com.paike.admin.dto.AlgorithmInfo;
import com.paike.admin.service.ScheduleService;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.algorithm.dto.SchedulingResult;
import com.paike.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "排课管理", description = "排课相关接口")
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Operation(summary = "执行排课")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/execute")
    public Result<SchedulingResult> executeSchedule(@Valid @RequestBody SchedulingRequest request) {
        SchedulingResult result = scheduleService.schedule(request);
        return Result.success(result);
    }

    @Operation(summary = "异步排课")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/async")
    public Result<String> asyncSchedule(@Valid @RequestBody SchedulingRequest request) {
        scheduleService.scheduleAsync(request);
        return Result.success("排课任务已提交，请稍后查询结果");
    }

    @Operation(summary = "获取算法列表")
    @GetMapping("/algorithms")
    public Result<List<AlgorithmInfo>> getAlgorithms() {
        List<AlgorithmInfo> algorithms = Arrays.asList(
                new AlgorithmInfo("GREEDY", "贪心算法", "基于贪心策略的排课算法，按优先级排序任务，依次分配最优时间槽和教室"),
                new AlgorithmInfo("GENETIC", "遗传算法", "基于遗传算法的排课优化，通过选择、交叉、变异操作迭代寻找最优解")
        );
        return Result.success(algorithms);
    }
}
