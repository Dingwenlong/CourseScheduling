package com.paike.admin.service;

import com.paike.admin.dto.TimetableGenerationJobStatus;
import com.paike.algorithm.dto.SchedulingRequest;

public interface TimetableGenerationJobService {

    TimetableGenerationJobStatus submit(SchedulingRequest request);

    TimetableGenerationJobStatus getJob(String jobId);
}
