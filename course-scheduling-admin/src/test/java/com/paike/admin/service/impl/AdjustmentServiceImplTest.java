package com.paike.admin.service.impl;

import com.paike.admin.dto.AdjustmentRequest;
import com.paike.admin.dto.AdjustmentRecommendationResponse;
import com.paike.admin.dto.AdjustmentResult;
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
import com.paike.admin.service.TimetableDetailService;
import com.paike.admin.utils.SecurityUtils;
import com.paike.common.exception.BusinessException;
import com.paike.common.result.ResultCode;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdjustmentServiceImplTest {

    private static final RuntimeException STOP_AFTER_REFRESH = new RuntimeException("stop-after-refresh");

    @Test
    void getPendingApplicationRejectsTeacherManagingAnotherTeachersDetail() {
        AdjustmentServiceImpl service = new AdjustmentServiceImpl();
        ReflectionTestUtils.setField(service, "timetableDetailMapper", timetableDetailMapperReturning(buildDetail()));
        ReflectionTestUtils.setField(service, "timetableMapper", timetableMapperReturning(buildTimetable()));
        ReflectionTestUtils.setField(service, "securityUtils", teacherSecurityUtils());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.getPendingApplication(1L, 2L));

        assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
        assertEquals("无权操作该课程明细", exception.getMessage());
    }

    @Test
    void checkAdjustmentDetectsConflictAcrossOccupiedTwoSlotWindow() {
        AdjustmentServiceImpl service = new AdjustmentServiceImpl();
        TimetableDetail sourceDetail = buildDetail();
        sourceDetail.setDayOfWeek(1);
        sourceDetail.setSlotNo(5);
        sourceDetail.setTeacherId(100L);
        sourceDetail.setClassroomId(10L);

        TimetableDetail existingDetail = new TimetableDetail();
        existingDetail.setId(3L);
        existingDetail.setTimetableId(1L);
        existingDetail.setDayOfWeek(1);
        existingDetail.setSlotNo(1);
        existingDetail.setTeacherId(100L);
        existingDetail.setCourseName("高等数学");
        existingDetail.setStatus(1);

        ReflectionTestUtils.setField(service, "timetableDetailMapper",
                timetableDetailMapperReturning(sourceDetail, List.of(existingDetail)));
        ReflectionTestUtils.setField(service, "timetableMapper", timetableMapperReturning(buildTimetable()));
        ReflectionTestUtils.setField(service, "securityUtils", adminSecurityUtils());

        AdjustmentRequest request = new AdjustmentRequest();
        request.setTimetableId(1L);
        request.setDetailId(2L);
        request.setNewDayOfWeek(1);
        request.setNewSlotNo(2);

        AdjustmentResult result = service.checkAdjustment(request);

        assertFalse(Boolean.TRUE.equals(result.getSuccess()));
        assertTrue(result.getConflicts().stream().anyMatch(item -> item.contains("教师冲突")));
    }

    @Test
    void checkAdjustmentIgnoresMirroredSourceDetailsWhenMovingTwoSlotWindow() {
        AdjustmentServiceImpl service = new AdjustmentServiceImpl();
        TimetableDetail sourceDetail = buildDetail();
        sourceDetail.setDayOfWeek(1);
        sourceDetail.setSlotNo(1);
        sourceDetail.setTeacherId(100L);
        sourceDetail.setClassId(200L);
        sourceDetail.setClassroomId(10L);
        sourceDetail.setWeeks("1-16");
        sourceDetail.setStatus(1);

        TimetableDetail mirroredDetail = new TimetableDetail();
        mirroredDetail.setId(3L);
        mirroredDetail.setTimetableId(1L);
        mirroredDetail.setCourseId(sourceDetail.getCourseId());
        mirroredDetail.setTeacherId(100L);
        mirroredDetail.setClassId(200L);
        mirroredDetail.setClassroomId(10L);
        mirroredDetail.setDayOfWeek(1);
        mirroredDetail.setSlotNo(1);
        mirroredDetail.setWeeks("1-16");
        mirroredDetail.setStatus(1);
        mirroredDetail.setCourseName("程序设计基础");

        ReflectionTestUtils.setField(service, "timetableDetailMapper",
                timetableDetailMapperReturning(sourceDetail, List.of(sourceDetail, mirroredDetail)));
        ReflectionTestUtils.setField(service, "timetableMapper", timetableMapperReturning(buildTimetable()));
        ReflectionTestUtils.setField(service, "securityUtils", adminSecurityUtils());

        AdjustmentRequest request = new AdjustmentRequest();
        request.setTimetableId(1L);
        request.setDetailId(2L);
        request.setNewDayOfWeek(1);
        request.setNewSlotNo(2);

        AdjustmentResult result = service.checkAdjustment(request);

        assertTrue(Boolean.TRUE.equals(result.getSuccess()));
    }

    @Test
    void listAdjustmentRecommendationsReturnsAvailablePlans() {
        AdjustmentServiceImpl service = new AdjustmentServiceImpl();
        TimetableDetail sourceDetail = buildDetail();
        sourceDetail.setDayOfWeek(1);
        sourceDetail.setSlotNo(1);
        sourceDetail.setTeacherId(100L);
        sourceDetail.setClassId(300L);
        sourceDetail.setClassroomId(10L);
        sourceDetail.setClassroomName("A101");
        sourceDetail.setTaskId(20L);
        sourceDetail.setStatus(1);

        TimetableDetail conflictDetail = new TimetableDetail();
        conflictDetail.setId(4L);
        conflictDetail.setTimetableId(1L);
        conflictDetail.setDayOfWeek(1);
        conflictDetail.setSlotNo(2);
        conflictDetail.setTeacherId(100L);
        conflictDetail.setClassId(999L);
        conflictDetail.setCourseName("冲突课程");
        conflictDetail.setStatus(1);

        TeachingTask task = new TeachingTask();
        task.setId(20L);
        task.setStudentCount(40);

        Classroom currentClassroom = new Classroom();
        currentClassroom.setId(10L);
        currentClassroom.setRoomName("A101");
        currentClassroom.setRoomNo("A101");
        currentClassroom.setCapacity(50);
        currentClassroom.setStatus(1);

        ReflectionTestUtils.setField(service, "timetableDetailMapper",
                timetableDetailMapperReturning(sourceDetail, List.of(conflictDetail)));
        ReflectionTestUtils.setField(service, "timetableMapper", timetableMapperReturning(buildTimetable()));
        ReflectionTestUtils.setField(service, "teachingTaskMapper", teachingTaskMapperReturning(task));
        ReflectionTestUtils.setField(service, "classroomMapper", classroomMapperReturning(currentClassroom, List.of(currentClassroom)));
        ReflectionTestUtils.setField(service, "securityUtils", adminSecurityUtils());

        List<AdjustmentRecommendationResponse> recommendations = service.listAdjustmentRecommendations(1L, 2L, 3);

        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.stream().noneMatch(item -> item.getDayOfWeek() == 1 && item.getSlotNo() == 2));
    }

    @Test
    void getLatestApplicationReturnsNewestVisibleStatus() {
        AdjustmentServiceImpl service = new AdjustmentServiceImpl();
        TimetableDetail detail = buildDetail();
        detail.setTeacherId(100L);

        AdjustmentApplication application = new AdjustmentApplication();
        application.setId(9L);
        application.setDetailId(detail.getId());
        application.setTeacherId(10L);
        application.setStatus("APPROVED");

        ReflectionTestUtils.setField(service, "timetableDetailMapper", timetableDetailMapperReturning(detail));
        ReflectionTestUtils.setField(service, "timetableMapper", timetableMapperReturning(buildTimetable()));
        ReflectionTestUtils.setField(service, "adjustmentApplicationMapper", adjustmentApplicationMapperReturning(application));
        ReflectionTestUtils.setField(service, "securityUtils", teacherSecurityUtils());

        AdjustmentApplication result = service.getLatestApplication(1L, 2L);

        assertEquals(9L, result.getId());
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void getLatestSwapApplicationReturnsNewestVisibleStatus() {
        AdjustmentServiceImpl service = new AdjustmentServiceImpl();

        TimetableDetail detail1 = buildDetail();
        detail1.setId(2L);
        detail1.setTeacherId(100L);
        TimetableDetail detail2 = buildDetail();
        detail2.setId(3L);
        detail2.setTeacherId(100L);

        SwapAdjustmentApplication application = new SwapAdjustmentApplication();
        application.setId(11L);
        application.setTimetableId(1L);
        application.setDetailId1(2L);
        application.setDetailId2(3L);
        application.setTeacherId(10L);
        application.setStatus("CANCELLED");

        ReflectionTestUtils.setField(service, "timetableDetailMapper",
                timetableDetailMapperReturningMultiple(Map.of(2L, detail1, 3L, detail2), List.of()));
        ReflectionTestUtils.setField(service, "timetableMapper", timetableMapperReturning(buildTimetable()));
        ReflectionTestUtils.setField(service, "swapAdjustmentApplicationMapper", swapAdjustmentApplicationMapperReturning(application));
        ReflectionTestUtils.setField(service, "securityUtils", teacherSecurityUtils());

        SwapAdjustmentApplication result = service.getLatestSwapApplication(1L, 2L, 3L);

        assertEquals(11L, result.getId());
        assertEquals("CANCELLED", result.getStatus());
    }

    @Test
    void executeAdjustmentRefreshesBothOriginalAndTargetOccupiedSlots() {
        AdjustmentServiceImpl service = new AdjustmentServiceImpl();
        Timetable timetable = buildTimetable();
        timetable.setStatus("DRAFT");

        TimetableDetail sourceDetail = buildDetail();
        sourceDetail.setDayOfWeek(1);
        sourceDetail.setSlotNo(1);
        sourceDetail.setClassroomId(10L);
        sourceDetail.setClassroomName("A101");
        sourceDetail.setStatus(1);

        AtomicReference<Collection<String>> capturedSlotKeys = new AtomicReference<>();
        ReflectionTestUtils.setField(service, "timetableDetailMapper",
                timetableDetailMapperReturning(sourceDetail, List.of()));
        ReflectionTestUtils.setField(service, "timetableMapper", timetableMapperReturning(timetable));
        ReflectionTestUtils.setField(service, "timetableDetailService", timetableDetailServiceReturning(capturedSlotKeys, true));
        ReflectionTestUtils.setField(service, "securityUtils", adminSecurityUtils());

        AdjustmentRequest request = new AdjustmentRequest();
        request.setTimetableId(1L);
        request.setDetailId(2L);
        request.setNewDayOfWeek(1);
        request.setNewSlotNo(3);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.executeAdjustment(request));

        assertEquals(STOP_AFTER_REFRESH, exception);
        assertEquals(Set.of("1_1", "1_2", "1_3", "1_4"), Set.copyOf(capturedSlotKeys.get()));
    }

    @Test
    void executeAdjustmentMovesMirroredDetailsTogether() {
        AdjustmentServiceImpl service = new AdjustmentServiceImpl();
        Timetable timetable = buildTimetable();
        timetable.setStatus("DRAFT");

        TimetableDetail sourceDetail = buildDetail();
        sourceDetail.setCourseId(1L);
        sourceDetail.setDayOfWeek(1);
        sourceDetail.setSlotNo(1);
        sourceDetail.setTeacherId(100L);
        sourceDetail.setClassId(200L);
        sourceDetail.setClassroomId(10L);
        sourceDetail.setClassroomName("A101");
        sourceDetail.setWeeks("1-16");
        sourceDetail.setStatus(1);
        sourceDetail.setTaskId(20L);

        TimetableDetail mirroredDetail = new TimetableDetail();
        mirroredDetail.setId(3L);
        mirroredDetail.setTimetableId(1L);
        mirroredDetail.setTaskId(21L);
        mirroredDetail.setCourseId(1L);
        mirroredDetail.setTeacherId(100L);
        mirroredDetail.setClassId(200L);
        mirroredDetail.setClassroomId(10L);
        mirroredDetail.setClassroomName("A101");
        mirroredDetail.setDayOfWeek(1);
        mirroredDetail.setSlotNo(1);
        mirroredDetail.setWeeks("1-16");
        mirroredDetail.setStatus(1);
        mirroredDetail.setCourseName("程序设计基础");

        AtomicReference<Collection<String>> capturedSlotKeys = new AtomicReference<>();
        List<TimetableDetail> updatedDetails = new CopyOnWriteArrayList<>();
        ReflectionTestUtils.setField(service, "timetableDetailMapper",
                timetableDetailMapperReturningMultiple(
                        Map.of(sourceDetail.getId(), sourceDetail, mirroredDetail.getId(), mirroredDetail),
                        List.of(sourceDetail, mirroredDetail),
                        updatedDetails));
        ReflectionTestUtils.setField(service, "timetableMapper", timetableMapperReturning(timetable));
        ReflectionTestUtils.setField(service, "timetableDetailService", timetableDetailServiceReturning(capturedSlotKeys, true));
        ReflectionTestUtils.setField(service, "securityUtils", adminSecurityUtils());

        AdjustmentRequest request = new AdjustmentRequest();
        request.setTimetableId(1L);
        request.setDetailId(2L);
        request.setNewDayOfWeek(2);
        request.setNewSlotNo(1);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.executeAdjustment(request));

        assertEquals(STOP_AFTER_REFRESH, exception);
        assertEquals(2, updatedDetails.size());
        assertTrue(updatedDetails.stream().allMatch(detail -> detail.getDayOfWeek() == 2));
        assertTrue(updatedDetails.stream().allMatch(detail -> detail.getSlotNo() == 1));
    }

    private Timetable buildTimetable() {
        Timetable timetable = new Timetable();
        timetable.setId(1L);
        return timetable;
    }

    private TimetableDetail buildDetail() {
        TimetableDetail detail = new TimetableDetail();
        detail.setId(2L);
        detail.setTimetableId(1L);
        detail.setTeacherId(200L);
        return detail;
    }

    private SecurityUtils adminSecurityUtils() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setRole("ADMIN");
        currentUser.setStatus(1);

        return new SecurityUtils() {
            @Override
            public User requireCurrentUser() {
                return currentUser;
            }
        };
    }

    private SecurityUtils teacherSecurityUtils() {
        User currentUser = new User();
        currentUser.setId(10L);
        currentUser.setRole("TEACHER");
        currentUser.setStatus(1);

        return new SecurityUtils() {
            @Override
            public User requireCurrentUser() {
                return currentUser;
            }

            @Override
            public boolean isTeacher() {
                return true;
            }

            @Override
            public Long requireCurrentTeacherId() {
                return 100L;
            }
        };
    }

    private TimetableMapper timetableMapperReturning(Timetable timetable) {
        return (TimetableMapper) Proxy.newProxyInstance(
                TimetableMapper.class.getClassLoader(),
                new Class[]{TimetableMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return timetable;
                    }
                    if ("update".equals(method.getName())) {
                        return 1;
                    }
                    return null;
                });
    }

    private TimetableDetailMapper timetableDetailMapperReturning(TimetableDetail detail) {
        return timetableDetailMapperReturning(detail, List.of());
    }

    private TimetableDetailMapper timetableDetailMapperReturning(TimetableDetail detail, List<TimetableDetail> detailsForSlotQuery) {
        return timetableDetailMapperReturningMultiple(Map.of(detail.getId(), detail), detailsForSlotQuery, null);
    }

    private TimetableDetailMapper timetableDetailMapperReturningMultiple(Map<Long, TimetableDetail> detailsById,
                                                                        List<TimetableDetail> detailsForSlotQuery) {
        return timetableDetailMapperReturningMultiple(detailsById, detailsForSlotQuery, null);
    }

    private TimetableDetailMapper timetableDetailMapperReturningMultiple(Map<Long, TimetableDetail> detailsById,
                                                                        List<TimetableDetail> detailsForSlotQuery,
                                                                        List<TimetableDetail> capturedUpdates) {
        return (TimetableDetailMapper) Proxy.newProxyInstance(
                TimetableDetailMapper.class.getClassLoader(),
                new Class[]{TimetableDetailMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return detailsById.get(args[0]);
                    }
                    if ("selectList".equals(method.getName())) {
                        return detailsForSlotQuery;
                    }
                    if ("updateById".equals(method.getName())) {
                        if (capturedUpdates != null && args != null && args.length > 0 && args[0] instanceof TimetableDetail detail) {
                            capturedUpdates.add(detail);
                        }
                        return 1;
                    }
                    if ("selectBatchIds".equals(method.getName())) {
                        List<TimetableDetail> results = new ArrayList<>();
                        for (Object id : (Iterable<?>) args[0]) {
                            TimetableDetail matched = detailsById.get(id);
                            if (matched != null) {
                                results.add(matched);
                            }
                        }
                        return results;
                    }
                    return null;
                });
    }

    private AdjustmentApplicationMapper adjustmentApplicationMapperReturning(AdjustmentApplication application) {
        return (AdjustmentApplicationMapper) Proxy.newProxyInstance(
                AdjustmentApplicationMapper.class.getClassLoader(),
                new Class[]{AdjustmentApplicationMapper.class},
                (proxy, method, args) -> {
                    if ("selectOne".equals(method.getName())) {
                        return application;
                    }
                    return null;
                });
    }

    private SwapAdjustmentApplicationMapper swapAdjustmentApplicationMapperReturning(SwapAdjustmentApplication application) {
        return (SwapAdjustmentApplicationMapper) Proxy.newProxyInstance(
                SwapAdjustmentApplicationMapper.class.getClassLoader(),
                new Class[]{SwapAdjustmentApplicationMapper.class},
                (proxy, method, args) -> {
                    if ("selectOne".equals(method.getName())) {
                        return application;
                    }
                    return null;
                });
    }

    private TimetableDetailService timetableDetailServiceReturning(AtomicReference<Collection<String>> capturedSlotKeys,
                                                                  boolean stopAfterRefresh) {
        return (TimetableDetailService) Proxy.newProxyInstance(
                TimetableDetailService.class.getClassLoader(),
                new Class[]{TimetableDetailService.class},
                (proxy, method, args) -> {
                    if ("markConflictsForSlots".equals(method.getName())) {
                        capturedSlotKeys.set(new ArrayList<>((Collection<String>) args[1]));
                        if (stopAfterRefresh) {
                            throw STOP_AFTER_REFRESH;
                        }
                        return null;
                    }
                    if ("countConflicts".equals(method.getName())) {
                        return 0;
                    }
                    return null;
                });
    }

    private TeachingTaskMapper teachingTaskMapperReturning(TeachingTask task) {
        return (TeachingTaskMapper) Proxy.newProxyInstance(
                TeachingTaskMapper.class.getClassLoader(),
                new Class[]{TeachingTaskMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return task;
                    }
                    return null;
                });
    }

    private ClassroomMapper classroomMapperReturning(Classroom byIdClassroom, List<Classroom> classroomList) {
        return (ClassroomMapper) Proxy.newProxyInstance(
                ClassroomMapper.class.getClassLoader(),
                new Class[]{ClassroomMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return byIdClassroom;
                    }
                    if ("selectList".equals(method.getName())) {
                        return classroomList;
                    }
                    return null;
                });
    }
}
