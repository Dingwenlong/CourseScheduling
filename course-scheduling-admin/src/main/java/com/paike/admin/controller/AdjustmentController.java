package com.paike.admin.controller;

import com.paike.admin.dto.AdjustmentRequest;
import com.paike.admin.dto.AdjustmentRecommendationResponse;
import com.paike.admin.dto.AdjustmentResult;
import com.paike.admin.dto.AdjustmentHistoryResponse;
import com.paike.admin.dto.SwapAdjustmentRequest;
import com.paike.admin.dto.SwapAdjustmentHistoryResponse;
import com.paike.admin.entity.AdjustmentApplication;
import com.paike.admin.entity.SwapAdjustmentApplication;
import com.paike.admin.service.AdjustmentService;
import com.paike.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "调课管理", description = "调课相关接口")
@RestController
@RequestMapping("/adjustment")
public class AdjustmentController {

    @Autowired
    private AdjustmentService adjustmentService;

    @Operation(summary = "检测调课冲突")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/check")
    public Result<AdjustmentResult> checkAdjustment(@RequestBody AdjustmentRequest request) {
        AdjustmentResult result = adjustmentService.checkAdjustment(request);
        return Result.success(result);
    }

    @Operation(summary = "提交调课申请")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/apply")
    public Result<AdjustmentApplication> applyAdjustment(@RequestBody AdjustmentRequest request) {
        AdjustmentApplication application = adjustmentService.applyAdjustment(request);
        return Result.success(application);
    }

    @Operation(summary = "推荐可用调课方案")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/recommendations")
    public Result<List<AdjustmentRecommendationResponse>> getAdjustmentRecommendations(@RequestParam Long timetableId,
                                                                                      @RequestParam Long detailId,
                                                                                      @RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(adjustmentService.listAdjustmentRecommendations(timetableId, detailId, limit));
    }

    @Operation(summary = "查询当前待处理调课申请")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/pending")
    public Result<AdjustmentApplication> getPendingApplication(@RequestParam Long timetableId, @RequestParam Long detailId) {
        AdjustmentApplication application = adjustmentService.getPendingApplication(timetableId, detailId);
        return Result.success(application);
    }

    @Operation(summary = "查询当前课程最新调课申请状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/latest")
    public Result<AdjustmentApplication> getLatestApplication(@RequestParam Long timetableId, @RequestParam Long detailId) {
        return Result.success(adjustmentService.getLatestApplication(timetableId, detailId));
    }

    @Operation(summary = "取消调课申请")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/cancel/{applicationId}")
    public Result<Void> cancelApplication(@PathVariable Long applicationId) {
        adjustmentService.cancelApplication(applicationId);
        return Result.success();
    }

    @Operation(summary = "查询调课申请历史")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/history")
    public Result<List<AdjustmentHistoryResponse>> getAdjustmentHistory(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(adjustmentService.listAdjustmentHistory(limit));
    }

    @Operation(summary = "检测课程交换冲突")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/swap/check")
    public Result<AdjustmentResult> checkSwap(@RequestBody SwapAdjustmentRequest request) {
        AdjustmentResult result = adjustmentService.checkSwap(request);
        return Result.success(result);
    }

    @Operation(summary = "提交课程交换申请")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/swap/apply")
    public Result<SwapAdjustmentApplication> applySwap(@RequestBody SwapAdjustmentRequest request) {
        SwapAdjustmentApplication application = adjustmentService.applySwap(request);
        return Result.success(application);
    }

    @Operation(summary = "查询当前待处理课程交换申请")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/swap/pending")
    public Result<SwapAdjustmentApplication> getPendingSwapApplication(@RequestParam Long timetableId,
                                                                       @RequestParam Long detailId1,
                                                                       @RequestParam Long detailId2) {
        SwapAdjustmentApplication application = adjustmentService.getPendingSwapApplication(timetableId, detailId1, detailId2);
        return Result.success(application);
    }

    @Operation(summary = "查询当前课程交换最新申请状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/swap/latest")
    public Result<SwapAdjustmentApplication> getLatestSwapApplication(@RequestParam Long timetableId,
                                                                      @RequestParam Long detailId1,
                                                                      @RequestParam Long detailId2) {
        return Result.success(adjustmentService.getLatestSwapApplication(timetableId, detailId1, detailId2));
    }

    @Operation(summary = "取消课程交换申请")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/swap/cancel/{applicationId}")
    public Result<Void> cancelSwapApplication(@PathVariable Long applicationId) {
        adjustmentService.cancelSwapApplication(applicationId);
        return Result.success();
    }

    @Operation(summary = "查询课程交换申请历史")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/swap/history")
    public Result<List<SwapAdjustmentHistoryResponse>> getSwapHistory(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(adjustmentService.listSwapAdjustmentHistory(limit));
    }

    @Operation(summary = "执行课程交换")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/swap/execute")
    public Result<AdjustmentResult> executeSwap(@RequestBody SwapAdjustmentRequest request) {
        AdjustmentResult result = adjustmentService.executeSwap(request);
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
