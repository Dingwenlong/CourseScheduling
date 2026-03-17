package com.paike.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paike.admin.dto.ClassroomUtilization;
import com.paike.admin.dto.ConflictDetail;
import com.paike.admin.dto.ConflictReport;
import com.paike.admin.dto.StatisticsOverview;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private TimetableDetailMapper timetableDetailMapper;

    @Autowired
    private ClassroomMapper classroomMapper;

    @Autowired
    private TimetableMapper timetableMapper;

    @Override
    public StatisticsOverview getOverview(Long timetableId) {
        StatisticsSnapshot snapshot = loadSnapshot(timetableId);
        StatisticsOverview overview = new StatisticsOverview();
        // 使用timetable表中的scheduledCount，与首页保持一致
        Timetable timetable = snapshot.getTimetable();
        overview.setTotalHours(timetable.getScheduledCount() != null ? timetable.getScheduledCount() * 2 : 0);
        overview.setCourseCount(calculateCourseCount(snapshot.getDetails()));
        overview.setClassroomUtilization(buildClassroomUtilization(snapshot.getDetails(), snapshot.getClassrooms()));
        overview.setTeacherWorkload(buildTeacherWorkload(snapshot.getDetails()));
        overview.setConflictReport(buildConflictReport(snapshot.getDetails()));
        return overview;
    }

    @Override
    public List<ClassroomUtilization> getClassroomUtilization(Long timetableId) {
        StatisticsSnapshot snapshot = loadSnapshot(timetableId);
        return buildClassroomUtilization(snapshot.getDetails(), snapshot.getClassrooms());
    }

    @Override
    public List<TeacherWorkload> getTeacherWorkload(Long timetableId) {
        return buildTeacherWorkload(loadSnapshot(timetableId).getDetails());
    }

    @Override
    public ConflictReport getConflictReport(Long timetableId) {
        return buildConflictReport(loadSnapshot(timetableId).getDetails());
    }

    @Override
    public Integer getTotalScheduledHours(Long timetableId) {
        Timetable timetable = timetableMapper.selectById(timetableId);
        if (timetable == null) {
            return 0;
        }
        return timetable.getScheduledCount() != null ? timetable.getScheduledCount() * 2 : 0;
    }

    @Override
    public Integer getCourseCount(Long timetableId) {
        return calculateCourseCount(loadSnapshot(timetableId).getDetails());
    }

    private StatisticsSnapshot loadSnapshot(Long timetableId) {
        Timetable timetable = timetableMapper.selectById(timetableId);
        if (timetable == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "课表不存在");
        }

        List<TimetableDetail> details = timetableDetailMapper.selectList(
                new LambdaQueryWrapper<TimetableDetail>()
                        .eq(TimetableDetail::getTimetableId, timetableId));
        List<Classroom> classrooms = classroomMapper.selectList(
                new LambdaQueryWrapper<Classroom>().eq(Classroom::getStatus, 1));
        return new StatisticsSnapshot(timetable, details, classrooms);
    }

    private List<ClassroomUtilization> buildClassroomUtilization(List<TimetableDetail> details, List<Classroom> classrooms) {
        Map<Long, Integer> classroomUsage = new HashMap<>();
        for (TimetableDetail detail : details) {
            if (detail.getClassroomId() != null) {
                classroomUsage.merge(detail.getClassroomId(), 1, Integer::sum);
            }
        }

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

        result.sort((left, right) -> right.getUtilizationRate().compareTo(left.getUtilizationRate()));
        return result;
    }

    private List<TeacherWorkload> buildTeacherWorkload(List<TimetableDetail> details) {
        Map<Long, TeacherWorkload> workloadMap = new HashMap<>();
        Map<Long, Set<Long>> teacherCoursesMap = new HashMap<>();

        for (TimetableDetail detail : details) {
            if (detail.getTeacherId() == null) {
                continue;
            }

            TeacherWorkload workload = workloadMap.computeIfAbsent(detail.getTeacherId(), key -> {
                TeacherWorkload item = new TeacherWorkload();
                item.setTeacherId(key);
                item.setTeacherName(detail.getTeacherName());
                item.setTotalHours(0);
                item.setCourseCount(0);
                return item;
            });
            workload.setTotalHours(workload.getTotalHours() + 2);

            if (detail.getCourseId() != null) {
                teacherCoursesMap.computeIfAbsent(detail.getTeacherId(), key -> new HashSet<>())
                        .add(detail.getCourseId());
            }
        }

        List<TeacherWorkload> workloads = new ArrayList<>(workloadMap.values());
        for (TeacherWorkload workload : workloads) {
            Set<Long> courses = teacherCoursesMap.get(workload.getTeacherId());
            int courseCount = courses == null ? 0 : courses.size();
            workload.setCourseCount(courseCount);
            if (courseCount > 0) {
                workload.setAverageHoursPerCourse(BigDecimal.valueOf(workload.getTotalHours())
                        .divide(BigDecimal.valueOf(courseCount), 2, RoundingMode.HALF_UP));
            } else {
                workload.setAverageHoursPerCourse(BigDecimal.ZERO);
            }
        }

        workloads.sort(Comparator.comparing(TeacherWorkload::getTotalHours,
                Comparator.nullsFirst(Integer::compareTo)).reversed());
        return workloads;
    }

    private ConflictReport buildConflictReport(List<TimetableDetail> details) {
        ConflictReport report = new ConflictReport();
        List<ConflictDetail> conflictDetails = new ArrayList<>();
        int teacherConflicts = 0;
        int classroomConflicts = 0;
        int classConflicts = 0;

        for (TimetableDetail detail : details) {
            if (!Objects.equals(detail.getIsConflict(), 1)) {
                continue;
            }

            String conflictInfo = detail.getConflictInfo();
            if (conflictInfo != null) {
                if (conflictInfo.contains("教师冲突")) {
                    teacherConflicts++;
                }
                if (conflictInfo.contains("教室冲突")) {
                    classroomConflicts++;
                }
                if (conflictInfo.contains("班级冲突")) {
                    classConflicts++;
                }
            }

            ConflictDetail conflictDetail = new ConflictDetail();
            conflictDetail.setDetailId(detail.getId());
            conflictDetail.setCourseName(detail.getCourseName());
            conflictDetail.setTeacherName(detail.getTeacherName());
            conflictDetail.setClassName(detail.getClassName());
            conflictDetail.setClassroomName(detail.getClassroomName());
            conflictDetail.setDayOfWeek(detail.getDayOfWeek());
            conflictDetail.setSlotNo(detail.getSlotNo());
            conflictDetail.setConflictType(resolveConflictType(conflictInfo));
            conflictDetail.setConflictDescription(conflictInfo);
            conflictDetails.add(conflictDetail);
        }

        conflictDetails.sort(Comparator.comparing(ConflictDetail::getDayOfWeek,
                        Comparator.nullsFirst(Integer::compareTo))
                .thenComparing(ConflictDetail::getSlotNo, Comparator.nullsFirst(Integer::compareTo))
                .thenComparing(ConflictDetail::getCourseName, Comparator.nullsFirst(String::compareTo)));

        report.setTotalConflicts(conflictDetails.size());
        report.setTeacherConflicts(teacherConflicts);
        report.setClassroomConflicts(classroomConflicts);
        report.setClassConflicts(classConflicts);
        report.setConflictDetails(conflictDetails);
        return report;
    }

    private Integer calculateCourseCount(List<TimetableDetail> details) {
        Set<Long> courseIds = new HashSet<>();
        for (TimetableDetail detail : details) {
            if (detail.getCourseId() != null) {
                courseIds.add(detail.getCourseId());
            }
        }
        return courseIds.size();
    }

    private String resolveConflictType(String conflictInfo) {
        if (conflictInfo == null || conflictInfo.trim().isEmpty()) {
            return "冲突";
        }

        boolean teacherConflict = conflictInfo.contains("教师冲突");
        boolean classroomConflict = conflictInfo.contains("教室冲突");
        boolean classConflict = conflictInfo.contains("班级冲突");
        int conflictTypeCount = (teacherConflict ? 1 : 0) + (classroomConflict ? 1 : 0) + (classConflict ? 1 : 0);

        if (conflictTypeCount > 1) {
            return "复合冲突";
        }
        if (teacherConflict) {
            return "教师冲突";
        }
        if (classroomConflict) {
            return "教室冲突";
        }
        if (classConflict) {
            return "班级冲突";
        }
        return "冲突";
    }

    private static class StatisticsSnapshot {
        private final Timetable timetable;
        private final List<TimetableDetail> details;
        private final List<Classroom> classrooms;

        private StatisticsSnapshot(Timetable timetable, List<TimetableDetail> details, List<Classroom> classrooms) {
            this.timetable = timetable;
            this.details = details;
            this.classrooms = classrooms;
        }

        public Timetable getTimetable() {
            return timetable;
        }

        public List<TimetableDetail> getDetails() {
            return details;
        }

        public List<Classroom> getClassrooms() {
            return classrooms;
        }
    }
}
