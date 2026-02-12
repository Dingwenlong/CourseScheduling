package com.paike.admin.service;

import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.algorithm.dto.SchedulingResult;
import java.util.concurrent.CompletableFuture;

public interface ScheduleService {

    SchedulingResult schedule(SchedulingRequest request);

    CompletableFuture<SchedulingResult> scheduleAsync(SchedulingRequest request);
}
