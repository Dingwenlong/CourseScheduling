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
        List<TimetableDetail> details = listByTimetableId(timetableId);

        Map<String, List<TimetableDetail>> timeSlotMap = new HashMap<>();
        for (TimetableDetail detail : details) {
            String key = detail.getDayOfWeek() + "_" + detail.getSlotNo();
            timeSlotMap.computeIfAbsent(key, k -> new ArrayList<>()).add(detail);
        }

        for (Map.Entry<String, List<TimetableDetail>> entry : timeSlotMap.entrySet()) {
            List<TimetableDetail> slotDetails = entry.getValue();

            for (int i = 0; i < slotDetails.size(); i++) {
                TimetableDetail d1 = slotDetails.get(i);
                StringBuilder conflictInfo = new StringBuilder();

                for (int j = 0; j < slotDetails.size(); j++) {
                    if (i == j) continue;
                    TimetableDetail d2 = slotDetails.get(j);

                    if (d1.getClassroomId() != null && d1.getClassroomId().equals(d2.getClassroomId())) {
                        conflictInfo.append("教室冲突:").append(d2.getCourseName()).append(";");
                    }
                    if (d1.getTeacherId() != null && d1.getTeacherId().equals(d2.getTeacherId())) {
                        conflictInfo.append("教师冲突:").append(d2.getCourseName()).append(";");
                    }
                    if (d1.getClassId() != null && d1.getClassId().equals(d2.getClassId())) {
                        conflictInfo.append("班级冲突:").append(d2.getCourseName()).append(";");
                    }
                }

                if (conflictInfo.length() > 0) {
                    d1.setIsConflict(1);
                    d1.setConflictInfo(conflictInfo.toString());
                    updateById(d1);
                }
            }
        }
    }
}
