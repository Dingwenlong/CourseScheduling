package com.paike.admin.service.impl;

import com.paike.admin.entity.Timetable;
import com.paike.admin.entity.TimetableDetail;
import com.paike.admin.entity.User;
import com.paike.admin.mapper.TimetableDetailMapper;
import com.paike.admin.mapper.TimetableMapper;
import com.paike.admin.utils.SecurityUtils;
import com.paike.common.exception.BusinessException;
import com.paike.common.result.ResultCode;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdjustmentServiceImplTest {

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
                    return null;
                });
    }

    private TimetableDetailMapper timetableDetailMapperReturning(TimetableDetail detail) {
        return (TimetableDetailMapper) Proxy.newProxyInstance(
                TimetableDetailMapper.class.getClassLoader(),
                new Class[]{TimetableDetailMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return detail;
                    }
                    return null;
                });
    }
}
