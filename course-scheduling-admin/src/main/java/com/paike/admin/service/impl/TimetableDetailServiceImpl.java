package com.paike.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paike.admin.entity.TimetableDetail;
import com.paike.admin.mapper.TimetableDetailMapper;
import com.paike.admin.service.TimetableDetailService;
import com.paike.algorithm.dto.SchedulingResult;
import com.paike.algorithm.model.ScheduledTask;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TimetableDetailServiceImpl extends ServiceImpl<TimetableDetailMapper, TimetableDetail> implements TimetableDetailService {

    @Override
    public List<TimetableDetail> listByTimetableId(Long timetableId) {
        return list(new LambdaQueryWrapper<TimetableDetail>()
                .eq(TimetableDetail::getTimetableId, timetableId)
                .orderByAsc(TimetableDetail::getDayOfWeek)
                .orderByAsc(TimetableDetail::getSlotNo));
    }

    @Override
    public List<TimetableDetail> listByClassId(Long timetableId, Long classId) {
        return list(new LambdaQueryWrapper<TimetableDetail>()
                .eq(TimetableDetail::getTimetableId, timetableId)
                .eq(TimetableDetail::getClassId, classId)
                .orderByAsc(TimetableDetail::getDayOfWeek)
                .orderByAsc(TimetableDetail::getSlotNo));
    }

    @Override
    public List<TimetableDetail> listByTeacherId(Long timetableId, Long teacherId) {
        return list(new LambdaQueryWrapper<TimetableDetail>()
                .eq(TimetableDetail::getTimetableId, timetableId)
                .eq(TimetableDetail::getTeacherId, teacherId)
                .orderByAsc(TimetableDetail::getDayOfWeek)
                .orderByAsc(TimetableDetail::getSlotNo));
    }

    @Override
    public List<TimetableDetail> listByClassroomId(Long timetableId, Long classroomId) {
        return list(new LambdaQueryWrapper<TimetableDetail>()
                .eq(TimetableDetail::getTimetableId, timetableId)
                .eq(TimetableDetail::getClassroomId, classroomId)
                .orderByAsc(TimetableDetail::getDayOfWeek)
                .orderByAsc(TimetableDetail::getSlotNo));
    }

    @Override
    @Transactional
    public void saveScheduleResult(Long timetableId, SchedulingResult result) {
        if (result.getScheduledTasks() == null || result.getScheduledTasks().isEmpty()) {
            return;
        }

        List<TimetableDetail> details = new ArrayList<>();
        for (ScheduledTask task : result.getScheduledTasks()) {
            TimetableDetail detail = convertToDetail(timetableId, task);
            details.add(detail);
        }

        saveBatch(details);
    }

    private TimetableDetail convertToDetail(Long timetableId, ScheduledTask task) {
        TimetableDetail detail = new TimetableDetail();
        detail.setTimetableId(timetableId);
        detail.setTaskId(task.getTaskId());
        detail.setCourseId(task.getCourseId());
        detail.setCourseName(task.getCourseName());
        detail.setTeacherId(task.getTeacherId());
        detail.setTeacherName(task.getTeacherName());
        detail.setClassId(task.getClassId());
        detail.setClassName(task.getClassName());
        detail.setClassroomId(task.getClassroomId());
        detail.setClassroomName(task.getClassroomName());
        detail.setDayOfWeek(task.getTimeSlot().getDayOfWeek());
        detail.setSlotNo(task.getTimeSlot().getSlotNo());
        detail.setWeeks(task.getWeeks());
        detail.setIsConflict(0);
        detail.setStatus(1);
        return detail;
    }

    @Override
    public int countConflicts(Long timetableId) {
        return Math.toIntExact(count(new LambdaQueryWrapper<TimetableDetail>()
                .eq(TimetableDetail::getTimetableId, timetableId)
                .eq(TimetableDetail::getIsConflict, 1)));
    }

    @Override
    @Transactional
    public void markConflicts(Long timetableId) {
        applyConflictState(listByTimetableId(timetableId));
    }

    @Override
    @Transactional
    public void markConflictsForSlots(Long timetableId, Collection<String> slotKeys) {
        if (slotKeys == null || slotKeys.isEmpty()) {
            return;
        }

        List<TimetableDetail> affectedDetails = new ArrayList<>();
        for (TimetableDetail detail : listByTimetableId(timetableId)) {
            if (slotKeys.contains(buildSlotKey(detail.getDayOfWeek(), detail.getSlotNo()))) {
                affectedDetails.add(detail);
            }
        }

        applyConflictState(affectedDetails);
    }

    private void applyConflictState(List<TimetableDetail> details) {
        if (details == null || details.isEmpty()) {
            return;
        }

        Map<String, List<TimetableDetail>> timeSlotMap = new HashMap<>();
        for (TimetableDetail detail : details) {
            timeSlotMap.computeIfAbsent(buildSlotKey(detail.getDayOfWeek(), detail.getSlotNo()), key -> new ArrayList<>())
                    .add(detail);
        }

        List<TimetableDetail> updates = new ArrayList<>(details.size());
        for (TimetableDetail detail : details) {
            List<String> conflicts = buildConflicts(detail,
                    timeSlotMap.getOrDefault(buildSlotKey(detail.getDayOfWeek(), detail.getSlotNo()), Collections.emptyList()));
            detail.setIsConflict(conflicts.isEmpty() ? 0 : 1);
            detail.setConflictInfo(conflicts.isEmpty() ? null : String.join(";", conflicts));
            updates.add(detail);
        }

        updateBatchById(updates);
    }

    private List<String> buildConflicts(TimetableDetail current, List<TimetableDetail> slotDetails) {
        List<String> conflicts = new ArrayList<>();
        for (TimetableDetail other : slotDetails) {
            if (Objects.equals(current.getId(), other.getId())) {
                continue;
            }

            if (current.getClassroomId() != null && current.getClassroomId().equals(other.getClassroomId())) {
                conflicts.add("教室冲突:" + other.getCourseName());
            }
            if (current.getTeacherId() != null && current.getTeacherId().equals(other.getTeacherId())) {
                conflicts.add("教师冲突:" + other.getCourseName());
            }
            if (current.getClassId() != null && current.getClassId().equals(other.getClassId())) {
                conflicts.add("班级冲突:" + other.getCourseName());
            }
        }
        return conflicts;
    }

    private String buildSlotKey(Integer dayOfWeek, Integer slotNo) {
        return dayOfWeek + "_" + slotNo;
    }
}
