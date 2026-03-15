package com.paike.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paike.admin.dto.ClassroomUtilization;
import com.paike.admin.dto.ConflictDetail;
import com.paike.admin.dto.ConflictReport;
import com.paike.admin.dto.TeacherWorkload;
import com.paike.admin.entity.Classroom;
import com.paike.admin.entity.Timetable;
import com.paike.admin.entity.TimetableDetail;
import com.paike.admin.mapper.ClassroomMapper;
import com.paike.admin.mapper.TimetableDetailMapper;
import com.paike.admin.mapper.TimetableMapper;
import com.paike.admin.service.StatisticsService;
import com.paike.common.exception.BusinessException;
import com.paike.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private TimetableDetailMapper timetableDetailMapper;

    @Autowired
    private ClassroomMapper classroomMapper;

    @Autowired
    private TimetableMapper timetableMapper;

    private void validateTimetableExists(Long timetableId) {
        Timetable timetable = timetableMapper.selectById(timetableId);
        if (timetable == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "课表不存在");
        }
    }

    @Override
    public List<ClassroomUtilization> getClassroomUtilization(Long timetableId) {
        validateTimetableExists(timetableId);
        List<TimetableDetail> details = timetableDetailMapper.selectList(
                new LambdaQueryWrapper<TimetableDetail>()
                        .eq(TimetableDetail::getTimetableId, timetableId));

        Map<Long, Integer> classroomUsage = new HashMap<>();
        for (TimetableDetail detail : details) {
            classroomUsage.merge(detail.getClassroomId(), 1, Integer::sum);
        }

        List<Classroom> classrooms = classroomMapper.selectList(
                new LambdaQueryWrapper<Classroom>().eq(Classroom::getStatus, 1));

        int totalSlots = 5 * 10;

        List<ClassroomUtilization> result = new ArrayList<>();
        for (Classroom classroom : classrooms) {
            ClassroomUtilization utilization = new ClassroomUtilization();
            utilization.setClassroomId(classroom.getId());
            utilization.setClassroomName(classroom.getRoomName());
            utilization.setTotalSlots(totalSlots);
            
            Integer usedSlots = classroomUsage.getOrDefault(classroom.getId(), 0);
            utilization.setUsedSlots(usedSlots);
            
            BigDecimal rate = BigDecimal.ZERO;
            if (totalSlots > 0) {
                rate = BigDecimal.valueOf(usedSlots)
                        .divide(BigDecimal.valueOf(totalSlots), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
            utilization.setUtilizationRate(rate);
            
            result.add(utilization);
        }

        result.sort((a, b) -> b.getUtilizationRate().compareTo(a.getUtilizationRate()));
        return result;
    }

    @Override
    public List<TeacherWorkload> getTeacherWorkload(Long timetableId) {
        validateTimetableExists(timetableId);
        List<TimetableDetail> details = timetableDetailMapper.selectList(
                new LambdaQueryWrapper<TimetableDetail>()
                        .eq(TimetableDetail::getTimetableId, timetableId));

        Map<Long, TeacherWorkload> workloadMap = new HashMap<>();
        Map<Long, java.util.Set<Long>> teacherCoursesMap = new HashMap<>();
        
        for (TimetableDetail detail : details) {
            if (detail.getTeacherId() == null) continue;
            
            TeacherWorkload workload = workloadMap.computeIfAbsent(
                    detail.getTeacherId(), 
                    k -> {
                        TeacherWorkload w = new TeacherWorkload();
                        w.setTeacherId(k);
                        w.setTeacherName(detail.getTeacherName());
                        w.setTotalHours(0);
                        w.setCourseCount(0);
                        return w;
                    });
            
            workload.setTotalHours(workload.getTotalHours() + 2);
            
            java.util.Set<Long> courses = teacherCoursesMap.computeIfAbsent(
                    detail.getTeacherId(), 
                    k -> new java.util.HashSet<>());
            if (detail.getCourseId() != null) {
                courses.add(detail.getCourseId());
            }
        }

        for (TeacherWorkload workload : workloadMap.values()) {
            java.util.Set<Long> courses = teacherCoursesMap.get(workload.getTeacherId());
            int courseCount = courses != null ? courses.size() : 0;
            workload.setCourseCount(courseCount);
            
            if (courseCount > 0) {
                BigDecimal avg = BigDecimal.valueOf(workload.getTotalHours())
                        .divide(BigDecimal.valueOf(courseCount), 2, RoundingMode.HALF_UP);
                workload.setAverageHoursPerCourse(avg);
            }
        }

        return new ArrayList<>(workloadMap.values());
    }

    @Override
    public ConflictReport getConflictReport(Long timetableId) {
        validateTimetableExists(timetableId);
        List<TimetableDetail> conflicts = timetableDetailMapper.selectList(
                new LambdaQueryWrapper<TimetableDetail>()
                        .eq(TimetableDetail::getTimetableId, timetableId)
                        .eq(TimetableDetail::getIsConflict, 1));

        ConflictReport report = new ConflictReport();
        report.setTotalConflicts(conflicts.size());
        
        int teacherConflicts = 0;
        int classroomConflicts = 0;
        int classConflicts = 0;
        List<ConflictDetail> conflictDetails = new ArrayList<>();

        for (TimetableDetail detail : conflicts) {
            String conflictInfo = detail.getConflictInfo();
            if (conflictInfo != null) {
                if (conflictInfo.contains("教师冲突")) teacherConflicts++;
                if (conflictInfo.contains("教室冲突")) classroomConflicts++;
                if (conflictInfo.contains("班级冲突")) classConflicts++;
            }

            ConflictDetail conflictDetail = new ConflictDetail();
            conflictDetail.setDetailId(detail.getId());
            conflictDetail.setCourseName(detail.getCourseName());
            conflictDetail.setTeacherName(detail.getTeacherName());
            conflictDetail.setClassName(detail.getClassName());
            conflictDetail.setClassroomName(detail.getClassroomName());
            conflictDetail.setDayOfWeek(detail.getDayOfWeek());
            conflictDetail.setSlotNo(detail.getSlotNo());
            conflictDetail.setConflictDescription(conflictInfo);
            conflictDetails.add(conflictDetail);
        }

        report.setTeacherConflicts(teacherConflicts);
        report.setClassroomConflicts(classroomConflicts);
        report.setClassConflicts(classConflicts);
        report.setConflictDetails(conflictDetails);

        return report;
    }

    @Override
    public Integer getTotalScheduledHours(Long timetableId) {
        validateTimetableExists(timetableId);
        Long count = timetableDetailMapper.selectCount(
                new LambdaQueryWrapper<TimetableDetail>()
                        .eq(TimetableDetail::getTimetableId, timetableId));
        return count.intValue() * 2;
    }

    @Override
    public Integer getCourseCount(Long timetableId) {
        validateTimetableExists(timetableId);
        List<TimetableDetail> details = timetableDetailMapper.selectList(
                new LambdaQueryWrapper<TimetableDetail>()
                        .eq(TimetableDetail::getTimetableId, timetableId));
        
        return (int) details.stream()
                .map(TimetableDetail::getCourseId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();
    }
}
