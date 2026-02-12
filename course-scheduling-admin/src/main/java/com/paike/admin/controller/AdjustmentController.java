package com.paike.admin.controller;

import com.paike.admin.dto.AdjustmentRequest;
import com.paike.admin.dto.AdjustmentResult;
import com.paike.admin.service.AdjustmentService;
import com.paike.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "调课管理", description = "调课相关接口")
@RestController
@RequestMapping("/adjustment")
public class AdjustmentController {

    @Autowired
    private AdjustmentService adjustmentService;

    @Operation(summary = "检测调课冲突")
    @PostMapping("/check")
    public Result<AdjustmentResult> checkAdjustment(@RequestBody AdjustmentRequest request) {
        AdjustmentResult result = adjustmentService.checkAdjustment(request);
        return Result.success(result);
    }

    @Operation(summary = "执行调课")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/execute")
    public Result<AdjustmentResult> executeAdjustment(@RequestBody AdjustmentRequest request) {
        AdjustmentResult result = adjustmentService.executeAdjustment(request);
        return Result.success(result);
    }

    @Operation(summary = "交换两门课程")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/swap")
    public Result<AdjustmentResult> swapCourses(
            @RequestParam Long timetableId,
            @RequestParam Long detailId1,
            @RequestParam Long detailId2) {
        AdjustmentResult result = adjustmentService.swapTwoCourses(timetableId, detailId1, detailId2);
        return Result.success(result);
    }
}
