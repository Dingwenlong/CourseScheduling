package com.paike.admin.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paike.admin.entity.TeachingTask;
import com.paike.admin.entity.Timetable;
import com.paike.admin.entity.TimetableDetail;
import com.paike.admin.mapper.TeachingTaskMapper;
import com.paike.admin.mapper.TimetableMapper;
import com.paike.admin.service.ScheduleService;
import com.paike.admin.service.TimetableDetailService;
import com.paike.admin.service.TimetableService;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.algorithm.dto.SchedulingResult;
import com.paike.common.constants.TaskStatus;
import com.paike.common.exception.BusinessException;
import com.paike.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TimetableServiceImpl extends ServiceImpl<TimetableMapper, Timetable> implements TimetableService {

    private static final Logger log = LoggerFactory.getLogger(TimetableServiceImpl.class);

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private TimetableDetailService timetableDetailService;

    @Autowired
    private TeachingTaskMapper teachingTaskMapper;

    @Override
    @Transactional
    public Timetable generateTimetable(SchedulingRequest request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (!StringUtils.hasText(request.getSemester())) {
            throw new BusinessException("学期不能为空");
        }
        log.info("开始生成课表，学期: {}", request.getSemester());

        Timetable latestVersion = getLatestTimetable(request.getSemester());
        int version = 1;
        if (latestVersion != null) {
            version = latestVersion.getVersion() + 1;
        }

        SchedulingResult result = scheduleService.schedule(request);

        if (!result.getSuccess()) {
            throw new BusinessException("排课失败：" + result.getMessage());
        }

        String algorithmType = StringUtils.hasText(request.getAlgorithmType()) ? request.getAlgorithmType().toUpperCase() : "GREEDY";
        Map<String, Object> algorithmConfig = new LinkedHashMap<>();
        algorithmConfig.put("algorithmType", algorithmType);
        algorithmConfig.put("daysPerWeek", request.getDaysPerWeek());
        algorithmConfig.put("slotsPerDay", request.getSlotsPerDay());
        algorithmConfig.put("maxGenerations", request.getMaxGenerations());
        algorithmConfig.put("targetFitness", request.getTargetFitness());

        Timetable timetable = new Timetable();
        timetable.setSemester(request.getSemester());
        timetable.setVersion(version);
        timetable.setName(request.getSemester() + " 第" + version + "版课表");
        timetable.setStatus("DRAFT");
        timetable.setGenerateType("AUTO");
        timetable.setAlgorithmConfig(JSON.toJSONString(algorithmConfig));
        timetable.setTaskCount(result.getTotalTasks());
        timetable.setScheduledCount(result.getScheduledCount());
        timetable.setConflictCount(result.getConflictCount());
        timetable.setGenerateTime(LocalDateTime.now());

        if (result.getSatisfactionScore() != null) {
            timetable.setSatisfactionScore(result.getSatisfactionScore());
        }

        if (result.getScheduledCount() > 0 && result.getTotalTasks() > 0) {
            BigDecimal rate = BigDecimal.valueOf(result.getScheduledCount())
                    .divide(BigDecimal.valueOf(result.getTotalTasks()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            timetable.setUtilizationRate(rate);
        }

        save(timetable);

        timetableDetailService.saveScheduleResult(timetable.getId(), result);

        timetableDetailService.markConflicts(timetable.getId());
        int actualConflictCount = timetableDetailService.countConflicts(timetable.getId());
        if (actualConflictCount != timetable.getConflictCount()) {
            timetable.setConflictCount(actualConflictCount);
            updateById(timetable);
        }

        log.info("课表生成完成，ID: {}, 排课成功: {}, 冲突: {}", 
                timetable.getId(), result.getScheduledCount(), timetable.getConflictCount());

        return timetable;
    }

    @Override
    @Transactional
    public Timetable publishTimetable(Long timetableId) {
        Timetable timetable = getById(timetableId);
        if (timetable == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        if ("PUBLISHED".equals(timetable.getStatus())) {
            throw new BusinessException("课表已发布，无需重复操作");
        }

        if ("ARCHIVED".equals(timetable.getStatus())) {
            throw new BusinessException("已归档课表不能发布");
        }

        timetable.setStatus("PUBLISHED");
        timetable.setPublishTime(LocalDateTime.now());
        updateById(timetable);
        markPublishedTasksScheduled(timetableId);

        log.info("课表发布成功，ID: {}", timetableId);
        return timetable;
    }

    @Override
    @Transactional
    public Timetable archiveTimetable(Long timetableId) {
        Timetable timetable = getById(timetableId);
        if (timetable == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        timetable.setStatus("ARCHIVED");
        updateById(timetable);

        log.info("课表归档成功，ID: {}", timetableId);
        return timetable;
    }

    @Override
    public Timetable getLatestTimetable(String semester) {
        return getOne(new LambdaQueryWrapper<Timetable>()
                .eq(Timetable::getSemester, semester)
                .orderByDesc(Timetable::getVersion)
                .last("LIMIT 1"));
    }

    @Override
    public List<Timetable> listBySemester(String semester) {
        return list(new LambdaQueryWrapper<Timetable>()
                .eq(Timetable::getSemester, semester)
                .orderByDesc(Timetable::getVersion));
    }

    @Override
    @Transactional
    public void deleteTimetable(Long timetableId) {
        Timetable timetable = getById(timetableId);
        if (timetable == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        if ("PUBLISHED".equals(timetable.getStatus())) {
            throw new BusinessException("已发布的课表不能删除，请先归档");
        }

        timetableDetailService.remove(new LambdaQueryWrapper<TimetableDetail>()
                .eq(TimetableDetail::getTimetableId, timetableId));

        removeById(timetableId);

        log.info("课表删除成功，ID: {}", timetableId);
    }

    private void markPublishedTasksScheduled(Long timetableId) {
        List<TimetableDetail> details = timetableDetailService.listByTimetableId(timetableId);
        Set<Long> taskIds = new LinkedHashSet<>();
        for (TimetableDetail detail : details) {
            if (detail.getTaskId() != null) {
                taskIds.add(detail.getTaskId());
            }
        }
        if (taskIds.isEmpty()) {
            return;
        }

        teachingTaskMapper.update(null, new LambdaUpdateWrapper<TeachingTask>()
                .in(TeachingTask::getId, taskIds)
                .ne(TeachingTask::getStatus, TaskStatus.COMPLETED.getCode())
                .set(TeachingTask::getStatus, TaskStatus.SCHEDULED.getCode()));
    }
}
