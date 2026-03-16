package com.paike.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.paike.admin.dto.AdjustmentRequest;
import com.paike.admin.dto.AdjustmentResult;
import com.paike.admin.dto.SwapAdjustmentRequest;
import com.paike.admin.entity.AdjustmentApplication;
import com.paike.admin.entity.Classroom;
import com.paike.admin.entity.SwapAdjustmentApplication;
import com.paike.admin.entity.TeachingTask;
import com.paike.admin.entity.Timetable;
import com.paike.admin.entity.TimetableDetail;
import com.paike.admin.entity.User;
import com.paike.admin.mapper.AdjustmentApplicationMapper;
import com.paike.admin.mapper.ClassroomMapper;
import com.paike.admin.mapper.SwapAdjustmentApplicationMapper;
import com.paike.admin.mapper.TeachingTaskMapper;
import com.paike.admin.mapper.TimetableDetailMapper;
import com.paike.admin.mapper.TimetableMapper;
import com.paike.admin.service.AdjustmentService;
import com.paike.admin.service.TimetableDetailService;
import com.paike.admin.utils.SecurityUtils;
import com.paike.common.constants.TaskStatus;
import com.paike.common.exception.BusinessException;
import com.paike.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AdjustmentServiceImpl implements AdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(AdjustmentServiceImpl.class);

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String TIMETABLE_PUBLISHED = "PUBLISHED";
    private static final int DETAIL_STATUS_NORMAL = 1;
    private static final DateTimeFormatter APPLICATION_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Autowired
    private TimetableDetailMapper timetableDetailMapper;

    @Autowired
    private TimetableMapper timetableMapper;

    @Autowired
    private ClassroomMapper classroomMapper;

    @Autowired
    private TeachingTaskMapper teachingTaskMapper;

    @Autowired
    private AdjustmentApplicationMapper adjustmentApplicationMapper;

    @Autowired
    private SwapAdjustmentApplicationMapper swapAdjustmentApplicationMapper;

    @Autowired
    private TimetableDetailService timetableDetailService;

    @Autowired
    private SecurityUtils securityUtils;

    @Override
    public AdjustmentResult checkAdjustment(AdjustmentRequest request) {
        AdjustmentContext context = buildAdjustmentContext(request);
        List<String> conflicts = checkDetailConflicts(context.getTargetDetail(),
                Collections.singleton(context.getSourceDetail().getId()));
        if (conflicts.isEmpty()) {
            return AdjustmentResult.success("调课检测通过，无冲突");
        }
        return AdjustmentResult.fail("存在冲突，请调整后重试", conflicts);
    }

    @Override
    @Transactional
    public AdjustmentApplication applyAdjustment(AdjustmentRequest request) {
        if (!StringUtils.hasText(request.getReason())) {
            throw new BusinessException("调课原因不能为空");
        }

        AdjustmentContext context = buildAdjustmentContext(request);
        AdjustmentResult checkResult = checkAdjustment(request);
        if (!Boolean.TRUE.equals(checkResult.getSuccess())) {
            throw new BusinessException("调课存在冲突，无法提交申请: " + String.join("，", checkResult.getConflicts()));
        }

        User currentUser = requireCurrentUser();
        AdjustmentApplication application = resolveTargetApplication(request, context.getSourceDetail().getId(), currentUser);
        boolean isNew = application == null;
        if (isNew) {
            application = new AdjustmentApplication();
            application.setApplicationNo(generateApplicationNo("ADJ"));
        }

        application.setSemester(context.getTimetable().getSemester());
        application.setDetailId(context.getSourceDetail().getId());
        application.setTeacherId(currentUser.getId());
        application.setOldDay(context.getSourceDetail().getDayOfWeek());
        application.setOldSlot(context.getSourceDetail().getSlotNo());
        application.setOldClassroom(context.getSourceDetail().getClassroomId());
        application.setNewDay(request.getNewDayOfWeek());
        application.setNewSlot(request.getNewSlotNo());
        application.setNewClassroom(request.getNewClassroomId());
        application.setReason(request.getReason().trim());
        application.setStatus(STATUS_PENDING);
        application.setApplyTime(LocalDateTime.now());
        application.setAuditorId(null);
        application.setAuditTime(null);
        application.setAuditRemark(null);

        if (isNew) {
            adjustmentApplicationMapper.insert(application);
        } else {
            adjustmentApplicationMapper.updateById(application);
        }

        markTasksAdjustingIfPublished(context.getTimetable(), context.getSourceDetail().getTaskId());
        log.info("调课申请已保存，applicationId={}, detailId={}", application.getId(), context.getSourceDetail().getId());
        return application;
    }

    @Override
    public AdjustmentApplication getPendingApplication(Long timetableId, Long detailId) {
        TimetableDetail detail = getDetailInTimetable(timetableId, detailId);
        User currentUser = requireCurrentUser();
        if (isAdmin(currentUser)) {
            return findLatestPendingApplication(detail.getId());
        }
        return adjustmentApplicationMapper.selectOne(new LambdaQueryWrapper<AdjustmentApplication>()
                .eq(AdjustmentApplication::getDetailId, detail.getId())
                .eq(AdjustmentApplication::getTeacherId, currentUser.getId())
                .eq(AdjustmentApplication::getStatus, STATUS_PENDING)
                .orderByDesc(AdjustmentApplication::getApplyTime)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional
    public void cancelApplication(Long applicationId) {
        AdjustmentApplication application = getOwnedApplication(applicationId);
        ensurePending(application.getStatus());

        LocalDateTime now = LocalDateTime.now();
        application.setStatus(STATUS_CANCELLED);
        application.setAuditorId(getCurrentUserId());
        application.setAuditTime(now);
        application.setAuditRemark("用户取消申请");
        adjustmentApplicationMapper.updateById(application);

        TimetableDetail detail = getDetailOrThrow(application.getDetailId());
        Timetable timetable = getTimetableOrThrow(detail.getTimetableId());
        restoreTaskStatusIfNoPendingApplications(timetable, detail.getTaskId());
        log.info("调课申请已取消，applicationId={}", applicationId);
    }

    @Override
    @Transactional
    public AdjustmentResult executeAdjustment(AdjustmentRequest request) {
        AdjustmentApplication application = null;
        AdjustmentRequest effectiveRequest = request;
        if (request.getApplicationId() != null) {
            application = getOwnedApplication(request.getApplicationId());
            ensurePending(application.getStatus());
            effectiveRequest = buildRequestFromApplication(application, request.getTimetableId());
        }

        AdjustmentContext context = buildAdjustmentContext(effectiveRequest);
        AdjustmentResult checkResult = buildCheckResult(context.getTargetDetail(),
                Collections.singleton(context.getSourceDetail().getId()),
                "调课检测通过，无冲突",
                "调课存在冲突，无法执行");
        if (!Boolean.TRUE.equals(checkResult.getSuccess())) {
            throw new BusinessException(checkResult.getMessage() + ": " + String.join("，", checkResult.getConflicts()));
        }

        TimetableDetail detail = context.getSourceDetail();
        String oldSlotKey = buildSlotKey(detail.getDayOfWeek(), detail.getSlotNo());
        String newSlotKey = buildSlotKey(context.getTargetDetail().getDayOfWeek(), context.getTargetDetail().getSlotNo());

        detail.setDayOfWeek(context.getTargetDetail().getDayOfWeek());
        detail.setSlotNo(context.getTargetDetail().getSlotNo());
        detail.setClassroomId(context.getTargetDetail().getClassroomId());
        detail.setClassroomName(context.getTargetDetail().getClassroomName());
        detail.setIsConflict(0);
        detail.setConflictInfo(null);
        timetableDetailMapper.updateById(detail);

        refreshConflicts(detail.getTimetableId(), oldSlotKey, newSlotKey);
        updateTimetableConflictCount(detail.getTimetableId());

        if (application != null) {
            approveApplication(application, "已执行调课");
        }
        closePendingApplicationsForDetails(Collections.singleton(detail.getId()),
                application != null ? application.getId() : null,
                null,
                "课程已完成调课");
        markTasksCompletedIfPublished(context.getTimetable(), detail.getTaskId());

        log.info("调课执行成功，detailId={}, timetableId={}", detail.getId(), detail.getTimetableId());
        return AdjustmentResult.success("调课成功");
    }

    @Override
    public AdjustmentResult checkSwap(SwapAdjustmentRequest request) {
        SwapContext context = getSwapContext(request.getTimetableId(), request.getDetailId1(), request.getDetailId2());
        return buildSwapCheckResult(context);
    }

    @Override
    @Transactional
    public SwapAdjustmentApplication applySwap(SwapAdjustmentRequest request) {
        if (!StringUtils.hasText(request.getReason())) {
            throw new BusinessException("交换原因不能为空");
        }

        SwapContext context = getSwapContext(request.getTimetableId(), request.getDetailId1(), request.getDetailId2());
        AdjustmentResult checkResult = buildSwapCheckResult(context);
        if (!Boolean.TRUE.equals(checkResult.getSuccess())) {
            throw new BusinessException("课程交换存在冲突，无法提交申请: " + String.join("，", checkResult.getConflicts()));
        }

        User currentUser = requireCurrentUser();
        SwapAdjustmentApplication application = resolveTargetSwapApplication(request, context, currentUser);
        boolean isNew = application == null;
        if (isNew) {
            application = new SwapAdjustmentApplication();
            application.setApplicationNo(generateApplicationNo("SWP"));
        }

        application.setSemester(context.getTimetable().getSemester());
        application.setTimetableId(context.getTimetable().getId());
        application.setDetailId1(context.getDetail1().getId());
        application.setDetailId2(context.getDetail2().getId());
        application.setTeacherId(currentUser.getId());
        application.setOldDay1(context.getDetail1().getDayOfWeek());
        application.setOldSlot1(context.getDetail1().getSlotNo());
        application.setOldClassroom1(context.getDetail1().getClassroomId());
        application.setOldDay2(context.getDetail2().getDayOfWeek());
        application.setOldSlot2(context.getDetail2().getSlotNo());
        application.setOldClassroom2(context.getDetail2().getClassroomId());
        application.setReason(request.getReason().trim());
        application.setStatus(STATUS_PENDING);
        application.setApplyTime(LocalDateTime.now());
        application.setAuditorId(null);
        application.setAuditTime(null);
        application.setAuditRemark(null);

        if (isNew) {
            swapAdjustmentApplicationMapper.insert(application);
        } else {
            swapAdjustmentApplicationMapper.updateById(application);
        }

        markTasksAdjustingIfPublished(context.getTimetable(), context.getDetail1().getTaskId(), context.getDetail2().getTaskId());
        log.info("课程交换申请已保存，applicationId={}, detailIds=({}, {})",
                application.getId(), context.getDetail1().getId(), context.getDetail2().getId());
        return application;
    }

    @Override
    public SwapAdjustmentApplication getPendingSwapApplication(Long timetableId, Long detailId1, Long detailId2) {
        SwapContext context = getSwapContext(timetableId, detailId1, detailId2);
        User currentUser = requireCurrentUser();
        if (isAdmin(currentUser)) {
            return findLatestPendingSwapApplication(context.getDetail1().getId(), context.getDetail2().getId());
        }
        return swapAdjustmentApplicationMapper.selectOne(new LambdaQueryWrapper<SwapAdjustmentApplication>()
                .eq(SwapAdjustmentApplication::getTimetableId, context.getTimetable().getId())
                .eq(SwapAdjustmentApplication::getDetailId1, context.getDetail1().getId())
                .eq(SwapAdjustmentApplication::getDetailId2, context.getDetail2().getId())
                .eq(SwapAdjustmentApplication::getTeacherId, currentUser.getId())
                .eq(SwapAdjustmentApplication::getStatus, STATUS_PENDING)
                .orderByDesc(SwapAdjustmentApplication::getApplyTime)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional
    public void cancelSwapApplication(Long applicationId) {
        SwapAdjustmentApplication application = getOwnedSwapApplication(applicationId);
        ensurePending(application.getStatus());

        LocalDateTime now = LocalDateTime.now();
        application.setStatus(STATUS_CANCELLED);
        application.setAuditorId(getCurrentUserId());
        application.setAuditTime(now);
        application.setAuditRemark("用户取消交换申请");
        swapAdjustmentApplicationMapper.updateById(application);

        SwapContext context = getSwapContext(application.getTimetableId(), application.getDetailId1(), application.getDetailId2());
        restoreTaskStatusIfNoPendingApplications(context.getTimetable(),
                context.getDetail1().getTaskId(),
                context.getDetail2().getTaskId());
        log.info("课程交换申请已取消，applicationId={}", applicationId);
    }

    @Override
    @Transactional
    public AdjustmentResult executeSwap(SwapAdjustmentRequest request) {
        SwapAdjustmentApplication application = null;
        Long timetableId = request.getTimetableId();
        Long detailId1 = request.getDetailId1();
        Long detailId2 = request.getDetailId2();
        if (request.getApplicationId() != null) {
            application = getOwnedSwapApplication(request.getApplicationId());
            ensurePending(application.getStatus());
            timetableId = application.getTimetableId();
            detailId1 = application.getDetailId1();
            detailId2 = application.getDetailId2();
        }

        SwapContext context = getSwapContext(timetableId, detailId1, detailId2);
        AdjustmentResult checkResult = buildSwapCheckResult(context);
        if (!Boolean.TRUE.equals(checkResult.getSuccess())) {
            throw new BusinessException(checkResult.getMessage() + ": " + String.join("，", checkResult.getConflicts()));
        }

        TimetableDetail detail1 = context.getDetail1();
        TimetableDetail detail2 = context.getDetail2();
        String slotKey1 = buildSlotKey(detail1.getDayOfWeek(), detail1.getSlotNo());
        String slotKey2 = buildSlotKey(detail2.getDayOfWeek(), detail2.getSlotNo());

        Integer tempDay = detail1.getDayOfWeek();
        Integer tempSlot = detail1.getSlotNo();
        Long tempClassroomId = detail1.getClassroomId();
        String tempClassroomName = detail1.getClassroomName();

        detail1.setDayOfWeek(detail2.getDayOfWeek());
        detail1.setSlotNo(detail2.getSlotNo());
        detail1.setClassroomId(detail2.getClassroomId());
        detail1.setClassroomName(detail2.getClassroomName());
        detail1.setIsConflict(0);
        detail1.setConflictInfo(null);

        detail2.setDayOfWeek(tempDay);
        detail2.setSlotNo(tempSlot);
        detail2.setClassroomId(tempClassroomId);
        detail2.setClassroomName(tempClassroomName);
        detail2.setIsConflict(0);
        detail2.setConflictInfo(null);

        timetableDetailMapper.updateById(detail1);
        timetableDetailMapper.updateById(detail2);

        refreshConflicts(context.getTimetable().getId(), slotKey1, slotKey2);
        updateTimetableConflictCount(context.getTimetable().getId());

        if (application != null) {
            approveSwapApplication(application, "已执行课程交换");
        }
        closePendingApplicationsForDetails(asSet(detail1.getId(), detail2.getId()),
                null,
                application != null ? application.getId() : null,
                "课程已完成交换");
        markTasksCompletedIfPublished(context.getTimetable(), detail1.getTaskId(), detail2.getTaskId());

        log.info("课程交换执行成功，detailIds=({}, {})", detail1.getId(), detail2.getId());
        return AdjustmentResult.success("课程交换成功");
    }

    @Override
    @Transactional
    public AdjustmentResult swapTwoCourses(Long timetableId, Long detailId1, Long detailId2) {
        SwapAdjustmentRequest request = new SwapAdjustmentRequest();
        request.setTimetableId(timetableId);
        request.setDetailId1(detailId1);
        request.setDetailId2(detailId2);
        return executeSwap(request);
    }

    private AdjustmentContext buildAdjustmentContext(AdjustmentRequest request) {
        if (request == null
                || request.getTimetableId() == null
                || request.getDetailId() == null
                || request.getNewDayOfWeek() == null
                || request.getNewSlotNo() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Timetable timetable = getTimetableOrThrow(request.getTimetableId());
        TimetableDetail sourceDetail = getDetailInTimetable(timetable.getId(), request.getDetailId());

        Long targetClassroomId = request.getNewClassroomId() != null
                ? request.getNewClassroomId()
                : sourceDetail.getClassroomId();
        String targetClassroomName = sourceDetail.getClassroomName();
        if (request.getNewClassroomId() != null) {
            Classroom classroom = classroomMapper.selectById(request.getNewClassroomId());
            if (classroom == null) {
                throw new BusinessException("目标教室不存在");
            }
            if (classroom.getStatus() != null && classroom.getStatus() == 0) {
                throw new BusinessException("目标教室已停用");
            }
            targetClassroomName = resolveClassroomName(classroom);
        }

        if (Objects.equals(sourceDetail.getDayOfWeek(), request.getNewDayOfWeek())
                && Objects.equals(sourceDetail.getSlotNo(), request.getNewSlotNo())
                && Objects.equals(sourceDetail.getClassroomId(), targetClassroomId)) {
            throw new BusinessException("调课内容未发生变化");
        }

        TimetableDetail targetDetail = copyDetail(sourceDetail);
        targetDetail.setDayOfWeek(request.getNewDayOfWeek());
        targetDetail.setSlotNo(request.getNewSlotNo());
        targetDetail.setClassroomId(targetClassroomId);
        targetDetail.setClassroomName(targetClassroomName);

        return new AdjustmentContext(timetable, sourceDetail, targetDetail);
    }

    private AdjustmentRequest buildRequestFromApplication(AdjustmentApplication application, Long preferredTimetableId) {
        AdjustmentRequest request = new AdjustmentRequest();
        request.setApplicationId(application.getId());
        request.setTimetableId(resolveTimetableIdByApplication(application, preferredTimetableId));
        request.setDetailId(application.getDetailId());
        request.setNewDayOfWeek(application.getNewDay());
        request.setNewSlotNo(application.getNewSlot());
        request.setNewClassroomId(application.getNewClassroom());
        request.setReason(application.getReason());
        return request;
    }

    private Long resolveTimetableIdByApplication(AdjustmentApplication application, Long preferredTimetableId) {
        if (preferredTimetableId != null) {
            return preferredTimetableId;
        }
        TimetableDetail detail = getDetailOrThrow(application.getDetailId());
        return detail.getTimetableId();
    }

    private AdjustmentApplication resolveTargetApplication(AdjustmentRequest request, Long detailId, User currentUser) {
        AdjustmentApplication application = null;
        if (request.getApplicationId() != null) {
            application = getOwnedApplication(request.getApplicationId());
            if (!Objects.equals(application.getDetailId(), detailId)) {
                throw new BusinessException("申请与当前课程明细不匹配");
            }
        } else {
            AdjustmentApplication existing = findLatestPendingApplication(detailId);
            if (existing != null) {
                if (!isAdmin(currentUser) && !Objects.equals(existing.getTeacherId(), currentUser.getId())) {
                    throw new BusinessException("当前课程已有待处理调课申请");
                }
                application = existing;
            }
        }
        return application;
    }

    private SwapAdjustmentApplication resolveTargetSwapApplication(SwapAdjustmentRequest request,
                                                                   SwapContext context,
                                                                   User currentUser) {
        SwapAdjustmentApplication application = null;
        if (request.getApplicationId() != null) {
            application = getOwnedSwapApplication(request.getApplicationId());
            if (!Objects.equals(application.getTimetableId(), context.getTimetable().getId())
                    || !Objects.equals(application.getDetailId1(), context.getDetail1().getId())
                    || !Objects.equals(application.getDetailId2(), context.getDetail2().getId())) {
                throw new BusinessException("交换申请与当前课程不匹配");
            }
        } else {
            SwapAdjustmentApplication existing = findLatestPendingSwapApplication(context.getDetail1().getId(), context.getDetail2().getId());
            if (existing != null) {
                if (!isAdmin(currentUser) && !Objects.equals(existing.getTeacherId(), currentUser.getId())) {
                    throw new BusinessException("当前课程交换已有待处理申请");
                }
                application = existing;
            }
        }
        return application;
    }

    private AdjustmentResult buildSwapCheckResult(SwapContext context) {
        TimetableDetail preview1 = copyDetail(context.getDetail1());
        preview1.setDayOfWeek(context.getDetail2().getDayOfWeek());
        preview1.setSlotNo(context.getDetail2().getSlotNo());
        preview1.setClassroomId(context.getDetail2().getClassroomId());
        preview1.setClassroomName(context.getDetail2().getClassroomName());

        TimetableDetail preview2 = copyDetail(context.getDetail2());
        preview2.setDayOfWeek(context.getDetail1().getDayOfWeek());
        preview2.setSlotNo(context.getDetail1().getSlotNo());
        preview2.setClassroomId(context.getDetail1().getClassroomId());
        preview2.setClassroomName(context.getDetail1().getClassroomName());

        Set<Long> ignoredIds = asSet(context.getDetail1().getId(), context.getDetail2().getId());
        List<String> conflicts = new ArrayList<>();

        List<String> conflicts1 = checkDetailConflicts(preview1, ignoredIds);
        if (!conflicts1.isEmpty()) {
            conflicts.add(context.getDetail1().getCourseName() + " -> " + String.join("；", conflicts1));
        }

        List<String> conflicts2 = checkDetailConflicts(preview2, ignoredIds);
        if (!conflicts2.isEmpty()) {
            conflicts.add(context.getDetail2().getCourseName() + " -> " + String.join("；", conflicts2));
        }

        if (conflicts.isEmpty()) {
            return AdjustmentResult.success("课程交换检测通过，无冲突");
        }
        return AdjustmentResult.fail("交换后存在冲突，无法执行", conflicts);
    }

    private AdjustmentResult buildCheckResult(TimetableDetail detail,
                                              Collection<Long> ignoredIds,
                                              String successMessage,
                                              String failMessage) {
        List<String> conflicts = checkDetailConflicts(detail, ignoredIds);
        if (conflicts.isEmpty()) {
            return AdjustmentResult.success(successMessage);
        }
        return AdjustmentResult.fail(failMessage, conflicts);
    }

    private List<String> checkDetailConflicts(TimetableDetail detail, Collection<Long> ignoredIds) {
        List<TimetableDetail> existingDetails = timetableDetailMapper.selectList(new LambdaQueryWrapper<TimetableDetail>()
                .eq(TimetableDetail::getTimetableId, detail.getTimetableId())
                .eq(TimetableDetail::getDayOfWeek, detail.getDayOfWeek())
                .eq(TimetableDetail::getSlotNo, detail.getSlotNo())
                .eq(TimetableDetail::getStatus, DETAIL_STATUS_NORMAL));

        Set<Long> ignored = ignoredIds == null ? Collections.emptySet() : new LinkedHashSet<>(ignoredIds);
        Set<String> conflicts = new LinkedHashSet<>();
        for (TimetableDetail existing : existingDetails) {
            if (ignored.contains(existing.getId())) {
                continue;
            }
            if (detail.getClassroomId() != null && detail.getClassroomId().equals(existing.getClassroomId())) {
                conflicts.add("教室冲突: " + existing.getCourseName() + " 已占用该教室");
            }
            if (detail.getTeacherId() != null && detail.getTeacherId().equals(existing.getTeacherId())) {
                conflicts.add("教师冲突: " + existing.getCourseName() + " 该教师已有课");
            }
            if (detail.getClassId() != null && detail.getClassId().equals(existing.getClassId())) {
                conflicts.add("班级冲突: " + existing.getCourseName() + " 该班级已有课");
            }
        }
        return new ArrayList<>(conflicts);
    }

    private TimetableDetail copyDetail(TimetableDetail source) {
        TimetableDetail target = new TimetableDetail();
        target.setId(source.getId());
        target.setTimetableId(source.getTimetableId());
        target.setTaskId(source.getTaskId());
        target.setCourseId(source.getCourseId());
        target.setCourseName(source.getCourseName());
        target.setTeacherId(source.getTeacherId());
        target.setTeacherName(source.getTeacherName());
        target.setClassId(source.getClassId());
        target.setClassName(source.getClassName());
        target.setClassroomId(source.getClassroomId());
        target.setClassroomName(source.getClassroomName());
        target.setDayOfWeek(source.getDayOfWeek());
        target.setSlotNo(source.getSlotNo());
        target.setWeeks(source.getWeeks());
        target.setStatus(source.getStatus());
        return target;
    }

    private Timetable getTimetableOrThrow(Long timetableId) {
        Timetable timetable = timetableMapper.selectById(timetableId);
        if (timetable == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return timetable;
    }

    private TimetableDetail getDetailOrThrow(Long detailId) {
        TimetableDetail detail = timetableDetailMapper.selectById(detailId);
        if (detail == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return detail;
    }

    private TimetableDetail getDetailInTimetable(Long timetableId, Long detailId) {
        TimetableDetail detail = getDetailOrThrow(detailId);
        if (!Objects.equals(detail.getTimetableId(), timetableId)) {
            throw new BusinessException("课程明细不属于当前课表");
        }
        return detail;
    }

    private SwapContext getSwapContext(Long timetableId, Long detailId1, Long detailId2) {
        if (timetableId == null || detailId1 == null || detailId2 == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (Objects.equals(detailId1, detailId2)) {
            throw new BusinessException("不能交换同一门课程");
        }

        Timetable timetable = getTimetableOrThrow(timetableId);
        TimetableDetail rawDetail1 = getDetailInTimetable(timetableId, detailId1);
        TimetableDetail rawDetail2 = getDetailInTimetable(timetableId, detailId2);

        TimetableDetail detail1 = rawDetail1;
        TimetableDetail detail2 = rawDetail2;
        if (detail1.getId() > detail2.getId()) {
            detail1 = rawDetail2;
            detail2 = rawDetail1;
        }

        return new SwapContext(timetable, detail1, detail2);
    }

    private AdjustmentApplication findLatestPendingApplication(Long detailId) {
        return adjustmentApplicationMapper.selectOne(new LambdaQueryWrapper<AdjustmentApplication>()
                .eq(AdjustmentApplication::getDetailId, detailId)
                .eq(AdjustmentApplication::getStatus, STATUS_PENDING)
                .orderByDesc(AdjustmentApplication::getApplyTime)
                .last("LIMIT 1"));
    }

    private SwapAdjustmentApplication findLatestPendingSwapApplication(Long detailId1, Long detailId2) {
        long firstId = Math.min(detailId1, detailId2);
        long secondId = Math.max(detailId1, detailId2);
        return swapAdjustmentApplicationMapper.selectOne(new LambdaQueryWrapper<SwapAdjustmentApplication>()
                .eq(SwapAdjustmentApplication::getDetailId1, firstId)
                .eq(SwapAdjustmentApplication::getDetailId2, secondId)
                .eq(SwapAdjustmentApplication::getStatus, STATUS_PENDING)
                .orderByDesc(SwapAdjustmentApplication::getApplyTime)
                .last("LIMIT 1"));
    }

    private AdjustmentApplication getOwnedApplication(Long applicationId) {
        AdjustmentApplication application = adjustmentApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (!canManage(application.getTeacherId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该调课申请");
        }
        return application;
    }

    private SwapAdjustmentApplication getOwnedSwapApplication(Long applicationId) {
        SwapAdjustmentApplication application = swapAdjustmentApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (!canManage(application.getTeacherId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该交换申请");
        }
        return application;
    }

    private boolean canManage(Long ownerId) {
        User currentUser = requireCurrentUser();
        return isAdmin(currentUser) || Objects.equals(currentUser.getId(), ownerId);
    }

    private User requireCurrentUser() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        return currentUser;
    }

    private Long getCurrentUserId() {
        return requireCurrentUser().getId();
    }

    private boolean isAdmin(User user) {
        return user != null && ROLE_ADMIN.equalsIgnoreCase(user.getRole());
    }

    private void ensurePending(String status) {
        if (!STATUS_PENDING.equals(status)) {
            throw new BusinessException("当前申请状态不可操作");
        }
    }

    private void approveApplication(AdjustmentApplication application, String remark) {
        application.setStatus(STATUS_APPROVED);
        application.setAuditorId(getCurrentUserId());
        application.setAuditTime(LocalDateTime.now());
        application.setAuditRemark(remark);
        adjustmentApplicationMapper.updateById(application);
    }

    private void approveSwapApplication(SwapAdjustmentApplication application, String remark) {
        application.setStatus(STATUS_APPROVED);
        application.setAuditorId(getCurrentUserId());
        application.setAuditTime(LocalDateTime.now());
        application.setAuditRemark(remark);
        swapAdjustmentApplicationMapper.updateById(application);
    }

    private void closePendingApplicationsForDetails(Set<Long> detailIds,
                                                    Long keepSingleApplicationId,
                                                    Long keepSwapApplicationId,
                                                    String remark) {
        if (detailIds == null || detailIds.isEmpty()) {
            return;
        }

        Long auditorId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        List<AdjustmentApplication> applications = adjustmentApplicationMapper.selectList(new LambdaQueryWrapper<AdjustmentApplication>()
                .eq(AdjustmentApplication::getStatus, STATUS_PENDING)
                .in(AdjustmentApplication::getDetailId, detailIds));
        for (AdjustmentApplication application : applications) {
            if (Objects.equals(application.getId(), keepSingleApplicationId)) {
                continue;
            }
            application.setStatus(STATUS_CANCELLED);
            application.setAuditorId(auditorId);
            application.setAuditTime(now);
            application.setAuditRemark(remark);
            adjustmentApplicationMapper.updateById(application);
        }

        List<SwapAdjustmentApplication> swapApplications = swapAdjustmentApplicationMapper.selectList(
                new LambdaQueryWrapper<SwapAdjustmentApplication>()
                        .eq(SwapAdjustmentApplication::getStatus, STATUS_PENDING)
                        .and(wrapper -> wrapper.in(SwapAdjustmentApplication::getDetailId1, detailIds)
                                .or()
                                .in(SwapAdjustmentApplication::getDetailId2, detailIds)));
        for (SwapAdjustmentApplication application : swapApplications) {
            if (Objects.equals(application.getId(), keepSwapApplicationId)) {
                continue;
            }
            application.setStatus(STATUS_CANCELLED);
            application.setAuditorId(auditorId);
            application.setAuditTime(now);
            application.setAuditRemark(remark);
            swapAdjustmentApplicationMapper.updateById(application);
        }
    }

    private void refreshConflicts(Long timetableId, String... slotKeys) {
        Set<String> affectedSlots = new LinkedHashSet<>();
        if (slotKeys != null) {
            for (String slotKey : slotKeys) {
                if (StringUtils.hasText(slotKey)) {
                    affectedSlots.add(slotKey);
                }
            }
        }
        if (!affectedSlots.isEmpty()) {
            timetableDetailService.markConflictsForSlots(timetableId, affectedSlots);
        }
    }

    private void updateTimetableConflictCount(Long timetableId) {
        int conflictCount = timetableDetailService.countConflicts(timetableId);
        timetableMapper.update(null, new LambdaUpdateWrapper<Timetable>()
                .eq(Timetable::getId, timetableId)
                .set(Timetable::getConflictCount, conflictCount));
    }

    private void markTasksAdjustingIfPublished(Timetable timetable, Long... taskIds) {
        if (!isPublishedTimetable(timetable)) {
            return;
        }
        updateTaskStatus(TaskStatus.ADJUSTING.getCode(), taskIds);
    }

    private void markTasksCompletedIfPublished(Timetable timetable, Long... taskIds) {
        if (!isPublishedTimetable(timetable)) {
            return;
        }
        updateTaskStatus(TaskStatus.COMPLETED.getCode(), taskIds);
    }

    private void restoreTaskStatusIfNoPendingApplications(Timetable timetable, Long... taskIds) {
        if (!isPublishedTimetable(timetable)) {
            return;
        }
        Set<Long> uniqueTaskIds = uniqueIds(taskIds);
        for (Long taskId : uniqueTaskIds) {
            if (hasAnyPendingApplicationsForTask(taskId)) {
                continue;
            }
            teachingTaskMapper.update(null, new LambdaUpdateWrapper<TeachingTask>()
                    .eq(TeachingTask::getId, taskId)
                    .set(TeachingTask::getStatus, TaskStatus.SCHEDULED.getCode()));
        }
    }

    private void updateTaskStatus(String status, Long... taskIds) {
        Set<Long> uniqueTaskIds = uniqueIds(taskIds);
        if (uniqueTaskIds.isEmpty()) {
            return;
        }
        teachingTaskMapper.update(null, new LambdaUpdateWrapper<TeachingTask>()
                .in(TeachingTask::getId, uniqueTaskIds)
                .set(TeachingTask::getStatus, status));
    }

    private boolean hasAnyPendingApplicationsForTask(Long taskId) {
        if (taskId == null) {
            return false;
        }

        List<TimetableDetail> details = timetableDetailMapper.selectList(new LambdaQueryWrapper<TimetableDetail>()
                .select(TimetableDetail::getId)
                .eq(TimetableDetail::getTaskId, taskId)
                .eq(TimetableDetail::getStatus, DETAIL_STATUS_NORMAL));
        if (details.isEmpty()) {
            return false;
        }

        Set<Long> detailIds = new LinkedHashSet<>();
        for (TimetableDetail detail : details) {
            detailIds.add(detail.getId());
        }

        Long singleCount = adjustmentApplicationMapper.selectCount(new LambdaQueryWrapper<AdjustmentApplication>()
                .eq(AdjustmentApplication::getStatus, STATUS_PENDING)
                .in(AdjustmentApplication::getDetailId, detailIds));
        if (singleCount != null && singleCount > 0) {
            return true;
        }

        Long swapCount = swapAdjustmentApplicationMapper.selectCount(new LambdaQueryWrapper<SwapAdjustmentApplication>()
                .eq(SwapAdjustmentApplication::getStatus, STATUS_PENDING)
                .and(wrapper -> wrapper.in(SwapAdjustmentApplication::getDetailId1, detailIds)
                        .or()
                        .in(SwapAdjustmentApplication::getDetailId2, detailIds)));
        return swapCount != null && swapCount > 0;
    }

    private boolean isPublishedTimetable(Timetable timetable) {
        return timetable != null && TIMETABLE_PUBLISHED.equals(timetable.getStatus());
    }

    private String resolveClassroomName(Classroom classroom) {
        if (classroom == null) {
            return null;
        }
        if (StringUtils.hasText(classroom.getRoomName())) {
            return classroom.getRoomName();
        }
        return classroom.getRoomNo();
    }

    private String generateApplicationNo(String prefix) {
        return prefix
                + APPLICATION_NO_FORMATTER.format(LocalDateTime.now())
                + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private Set<Long> uniqueIds(Long... ids) {
        Set<Long> uniqueIds = new LinkedHashSet<>();
        if (ids == null) {
            return uniqueIds;
        }
        for (Long id : ids) {
            if (id != null) {
                uniqueIds.add(id);
            }
        }
        return uniqueIds;
    }

    private Set<Long> asSet(Long... ids) {
        return uniqueIds(ids);
    }

    private String buildSlotKey(Integer dayOfWeek, Integer slotNo) {
        return dayOfWeek + "_" + slotNo;
    }

    private static class AdjustmentContext {
        private final Timetable timetable;
        private final TimetableDetail sourceDetail;
        private final TimetableDetail targetDetail;

        private AdjustmentContext(Timetable timetable, TimetableDetail sourceDetail, TimetableDetail targetDetail) {
            this.timetable = timetable;
            this.sourceDetail = sourceDetail;
            this.targetDetail = targetDetail;
        }

        public Timetable getTimetable() {
            return timetable;
        }

        public TimetableDetail getSourceDetail() {
            return sourceDetail;
        }

        public TimetableDetail getTargetDetail() {
            return targetDetail;
        }
    }

    private static class SwapContext {
        private final Timetable timetable;
        private final TimetableDetail detail1;
        private final TimetableDetail detail2;

        private SwapContext(Timetable timetable, TimetableDetail detail1, TimetableDetail detail2) {
            this.timetable = timetable;
            this.detail1 = detail1;
            this.detail2 = detail2;
        }

        public Timetable getTimetable() {
            return timetable;
        }

        public TimetableDetail getDetail1() {
            return detail1;
        }

        public TimetableDetail getDetail2() {
            return detail2;
        }
    }
}
