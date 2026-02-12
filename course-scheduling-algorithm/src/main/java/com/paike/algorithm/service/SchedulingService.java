package com.paike.algorithm.service;

import com.paike.algorithm.dto.ClassroomData;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.algorithm.dto.SchedulingResult;
import com.paike.algorithm.dto.TaskData;
import java.util.List;

public interface SchedulingService {

    SchedulingResult schedule(SchedulingRequest request, List<TaskData> tasks, List<ClassroomData> classrooms);

    String getAlgorithmName();

    String getAlgorithmDescription();
}
