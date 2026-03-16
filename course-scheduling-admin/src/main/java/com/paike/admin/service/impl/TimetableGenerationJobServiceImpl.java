package com.paike.admin.service.impl;

import com.paike.admin.dto.TimetableGenerationJobStatus;
import com.paike.admin.entity.Timetable;
import com.paike.admin.service.TimetableGenerationJobService;
import com.paike.admin.service.TimetableService;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.common.exception.BusinessException;
import com.paike.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Service
public class TimetableGenerationJobServiceImpl implements TimetableGenerationJobService {

    private final Map<String, TimetableGenerationJobStatus> jobs = new ConcurrentHashMap<>();

    @Autowired
    private TimetableService timetableService;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    public TimetableGenerationJobStatus submit(SchedulingRequest request) {
        if (request == null || !StringUtils.hasText(request.getSemester())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "学期不能为空");
        }

        SchedulingRequest requestSnapshot = copyRequest(request);
        String jobId = UUID.randomUUID().toString().replace("-", "");

        TimetableGenerationJobStatus status = new TimetableGenerationJobStatus();
        status.setJobId(jobId);
        status.setStatus("SUBMITTED");
        status.setMessage("课表生成任务已提交");
        status.setSemester(requestSnapshot.getSemester());
        status.setAlgorithmType(StringUtils.hasText(requestSnapshot.getAlgorithmType()) ? requestSnapshot.getAlgorithmType().toUpperCase() : "GREEDY");
        status.setSubmittedAt(LocalDateTime.now());
        jobs.put(jobId, status);

        taskExecutor.execute(() -> executeJob(jobId, requestSnapshot));
        return status;
    }

    @Override
    public TimetableGenerationJobStatus getJob(String jobId) {
        TimetableGenerationJobStatus status = jobs.get(jobId);
        if (status == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "排课任务不存在");
        }
        return status;
    }

    private void executeJob(String jobId, SchedulingRequest request) {
        TimetableGenerationJobStatus status = jobs.get(jobId);
        if (status == null) {
            return;
        }

        status.setStatus("RUNNING");
        status.setMessage("正在生成课表");
        status.setStartedAt(LocalDateTime.now());

        try {
            Timetable timetable = timetableService.generateTimetable(request);
            status.setStatus("SUCCESS");
            status.setMessage("课表生成成功");
            status.setTimetableId(timetable.getId());
            status.setTimetableName(timetable.getName());
        } catch (Exception ex) {
            status.setStatus("FAILED");
            status.setMessage(resolveErrorMessage(ex));
        } finally {
            status.setFinishedAt(LocalDateTime.now());
        }
    }

    private SchedulingRequest copyRequest(SchedulingRequest request) {
        SchedulingRequest copy = new SchedulingRequest();
        copy.setSemester(request.getSemester());
        copy.setTaskIds(request.getTaskIds() != null ? List.copyOf(request.getTaskIds()) : null);
        copy.setDaysPerWeek(request.getDaysPerWeek());
        copy.setSlotsPerDay(request.getSlotsPerDay());
        copy.setAlgorithmType(request.getAlgorithmType());
        copy.setMaxGenerations(request.getMaxGenerations());
        copy.setTargetFitness(request.getTargetFitness());
        return copy;
    }

    private String resolveErrorMessage(Exception ex) {
        Throwable current = ex;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return StringUtils.hasText(message) ? message : "课表生成失败";
    }
}
