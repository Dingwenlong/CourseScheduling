package com.paike.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paike.admin.dto.AdjustmentRequest;
import com.paike.admin.dto.AdjustmentResult;
import com.paike.admin.entity.TimetableDetail;
import com.paike.admin.mapper.TimetableDetailMapper;
import com.paike.admin.service.AdjustmentService;
import com.paike.common.exception.BusinessException;
import com.paike.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdjustmentServiceImpl implements AdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(AdjustmentServiceImpl.class);

    @Autowired
    private TimetableDetailMapper timetableDetailMapper;

    @Override
    public AdjustmentResult checkAdjustment(AdjustmentRequest request) {
        TimetableDetail detail = timetableDetailMapper.selectById(request.getDetailId());
        if (detail == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        List<String> conflicts = new ArrayList<>();

        LambdaQueryWrapper<TimetableDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimetableDetail::getTimetableId, request.getTimetableId())
                .eq(TimetableDetail::getDayOfWeek, request.getNewDayOfWeek())
                .eq(TimetableDetail::getSlotNo, request.getNewSlotNo())
                .ne(TimetableDetail::getId, request.getDetailId());

        List<TimetableDetail> existingDetails = timetableDetailMapper.selectList(wrapper);

        Long classroomIdToCheck = request.getNewClassroomId() != null ? 
                request.getNewClassroomId() : detail.getClassroomId();

        for (TimetableDetail existing : existingDetails) {
            if (classroomIdToCheck != null && 
                    classroomIdToCheck.equals(existing.getClassroomId())) {
                conflicts.add("教室冲突: " + existing.getCourseName() + " 已占用该教室");
            }

            if (detail.getTeacherId() != null && 
                    detail.getTeacherId().equals(existing.getTeacherId())) {
                conflicts.add("教师冲突: " + existing.getCourseName() + " 该教师已有课");
            }

            if (detail.getClassId() != null && 
                    detail.getClassId().equals(existing.getClassId())) {
                conflicts.add("班级冲突: " + existing.getCourseName() + " 该班级已有课");
            }
        }

        if (conflicts.isEmpty()) {
            return AdjustmentResult.success("调课检测通过，无冲突");
        } else {
            return AdjustmentResult.fail("存在冲突，请确认是否继续", conflicts);
        }
    }

    @Override
    @Transactional
    public AdjustmentResult executeAdjustment(AdjustmentRequest request) {
        AdjustmentResult checkResult = checkAdjustment(request);
        
        if (!checkResult.getSuccess()) {
            throw new BusinessException("调课存在冲突，无法执行: " + String.join(", ", checkResult.getConflicts()));
        }
        
        TimetableDetail detail = timetableDetailMapper.selectById(request.getDetailId());
        if (detail == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        detail.setDayOfWeek(request.getNewDayOfWeek());
        detail.setSlotNo(request.getNewSlotNo());
        
        if (request.getNewClassroomId() != null) {
            detail.setClassroomId(request.getNewClassroomId());
        }

        detail.setIsConflict(0);
        detail.setConflictInfo(null);

        timetableDetailMapper.updateById(detail);

        log.info("调课执行成功，明细ID: {}, 新时间: 周{}第{}节", 
                request.getDetailId(), request.getNewDayOfWeek(), request.getNewSlotNo());

        return AdjustmentResult.success("调课成功");
    }

    private List<String> checkDetailConflicts(TimetableDetail detail) {
        List<String> conflicts = new ArrayList<>();

        LambdaQueryWrapper<TimetableDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimetableDetail::getTimetableId, detail.getTimetableId())
                .eq(TimetableDetail::getDayOfWeek, detail.getDayOfWeek())
                .eq(TimetableDetail::getSlotNo, detail.getSlotNo())
                .ne(TimetableDetail::getId, detail.getId());

        List<TimetableDetail> existingDetails = timetableDetailMapper.selectList(wrapper);

        for (TimetableDetail existing : existingDetails) {
            if (detail.getClassroomId() != null && 
                    detail.getClassroomId().equals(existing.getClassroomId())) {
                conflicts.add("教室冲突: " + existing.getCourseName() + " 已占用该教室");
            }

            if (detail.getTeacherId() != null && 
                    detail.getTeacherId().equals(existing.getTeacherId())) {
                conflicts.add("教师冲突: " + existing.getCourseName() + " 该教师已有课");
            }

            if (detail.getClassId() != null && 
                    detail.getClassId().equals(existing.getClassId())) {
                conflicts.add("班级冲突: " + existing.getCourseName() + " 该班级已有课");
            }
        }

        return conflicts;
    }

    @Override
    @Transactional
    public AdjustmentResult swapTwoCourses(Long timetableId, Long detailId1, Long detailId2) {
        TimetableDetail detail1 = timetableDetailMapper.selectById(detailId1);
        TimetableDetail detail2 = timetableDetailMapper.selectById(detailId2);

        if (detail1 == null || detail2 == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        if (!detail1.getTimetableId().equals(timetableId) || 
            !detail2.getTimetableId().equals(timetableId)) {
            throw new BusinessException("课程不属于同一课表");
        }

        Integer tempDay = detail1.getDayOfWeek();
        Integer tempSlot = detail1.getSlotNo();
        Long tempClassroom = detail1.getClassroomId();

        TimetableDetail tempDetail1 = new TimetableDetail();
        tempDetail1.setId(detail1.getId());
        tempDetail1.setTimetableId(detail1.getTimetableId());
        tempDetail1.setDayOfWeek(detail2.getDayOfWeek());
        tempDetail1.setSlotNo(detail2.getSlotNo());
        tempDetail1.setClassroomId(detail2.getClassroomId());
        tempDetail1.setTeacherId(detail1.getTeacherId());
        tempDetail1.setClassId(detail1.getClassId());
        tempDetail1.setCourseName(detail1.getCourseName());

        TimetableDetail tempDetail2 = new TimetableDetail();
        tempDetail2.setId(detail2.getId());
        tempDetail2.setTimetableId(detail2.getTimetableId());
        tempDetail2.setDayOfWeek(tempDay);
        tempDetail2.setSlotNo(tempSlot);
        tempDetail2.setClassroomId(tempClassroom);
        tempDetail2.setTeacherId(detail2.getTeacherId());
        tempDetail2.setClassId(detail2.getClassId());
        tempDetail2.setCourseName(detail2.getCourseName());

        List<String> conflicts1 = checkDetailConflicts(tempDetail1);
        List<String> conflicts2 = checkDetailConflicts(tempDetail2);

        List<String> allConflicts = new ArrayList<>();
        if (!conflicts1.isEmpty()) {
            allConflicts.add(detail1.getCourseName() + " 冲突: " + String.join(", ", conflicts1));
        }
        if (!conflicts2.isEmpty()) {
            allConflicts.add(detail2.getCourseName() + " 冲突: " + String.join(", ", conflicts2));
        }

        if (!allConflicts.isEmpty()) {
            throw new BusinessException("课程交换存在冲突: " + String.join("; ", allConflicts));
        }

        detail1.setDayOfWeek(detail2.getDayOfWeek());
        detail1.setSlotNo(detail2.getSlotNo());
        detail1.setClassroomId(detail2.getClassroomId());
        detail1.setIsConflict(0);
        detail1.setConflictInfo(null);

        detail2.setDayOfWeek(tempDay);
        detail2.setSlotNo(tempSlot);
        detail2.setClassroomId(tempClassroom);
        detail2.setIsConflict(0);
        detail2.setConflictInfo(null);

        timetableDetailMapper.updateById(detail1);
        timetableDetailMapper.updateById(detail2);

        log.info("课程交换成功，ID1: {}, ID2: {}", detailId1, detailId2);

        return AdjustmentResult.success("课程交换成功");
    }
}
