package com.paike.admin.service;

import com.paike.admin.dto.AdjustmentRequest;
import com.paike.admin.dto.AdjustmentRecommendationResponse;
import com.paike.admin.dto.AdjustmentResult;
import com.paike.admin.dto.AdjustmentHistoryResponse;
import com.paike.admin.dto.SwapAdjustmentRequest;
import com.paike.admin.dto.SwapAdjustmentHistoryResponse;
import com.paike.admin.entity.AdjustmentApplication;
import com.paike.admin.entity.SwapAdjustmentApplication;

import java.util.List;

public interface AdjustmentService {

    AdjustmentResult checkAdjustment(AdjustmentRequest request);

    AdjustmentApplication applyAdjustment(AdjustmentRequest request);

    List<AdjustmentRecommendationResponse> listAdjustmentRecommendations(Long timetableId, Long detailId, int limit);

    AdjustmentApplication getLatestApplication(Long timetableId, Long detailId);

    AdjustmentApplication getPendingApplication(Long timetableId, Long detailId);

    void cancelApplication(Long applicationId);

    AdjustmentResult executeAdjustment(AdjustmentRequest request);

    AdjustmentResult checkSwap(SwapAdjustmentRequest request);

    SwapAdjustmentApplication applySwap(SwapAdjustmentRequest request);

    SwapAdjustmentApplication getLatestSwapApplication(Long timetableId, Long detailId1, Long detailId2);

    SwapAdjustmentApplication getPendingSwapApplication(Long timetableId, Long detailId1, Long detailId2);

    void cancelSwapApplication(Long applicationId);

    AdjustmentResult executeSwap(SwapAdjustmentRequest request);

    AdjustmentResult swapTwoCourses(Long timetableId, Long detailId1, Long detailId2);

    List<AdjustmentHistoryResponse> listAdjustmentHistory(int limit);

    List<SwapAdjustmentHistoryResponse> listSwapAdjustmentHistory(int limit);
}
