package com.paike.admin.service;

import com.paike.admin.dto.AdjustmentRequest;
import com.paike.admin.dto.AdjustmentResult;
import com.paike.admin.entity.TimetableDetail;

public interface AdjustmentService {

    AdjustmentResult checkAdjustment(AdjustmentRequest request);

    AdjustmentResult executeAdjustment(AdjustmentRequest request);

    AdjustmentResult swapTwoCourses(Long timetableId, Long detailId1, Long detailId2);
}
