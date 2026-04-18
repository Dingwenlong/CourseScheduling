package com.paike.admin.utils;

import com.paike.admin.entity.TeachingTask;
import com.paike.admin.entity.TimetableDetail;
import com.paike.common.exception.BusinessException;
import com.paike.common.result.ResultCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityUtilsTest {

    @Test
    void checkTeachingTaskAccessRejectsTeacherReadingAnotherTeachersTask() {
        TestableSecurityUtils securityUtils = new TestableSecurityUtils();
        securityUtils.teacher = true;
        securityUtils.currentTeacherId = 100L;

        TeachingTask task = new TeachingTask();
        task.setTeacherId(200L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> securityUtils.checkTeachingTaskAccess(task));

        assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
        assertEquals("无权访问该教学任务", exception.getMessage());
    }

    @Test
    void checkClassAccessRejectsStudentReadingAnotherClassTimetable() {
        TestableSecurityUtils securityUtils = new TestableSecurityUtils();
        securityUtils.student = true;
        securityUtils.currentClassId = 3001L;

        BusinessException exception = assertThrows(BusinessException.class,
                () -> securityUtils.checkClassAccess(4001L));

        assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
        assertEquals("无权访问该班级课表", exception.getMessage());
    }

    @Test
    void filterTimetableDetailsKeepsOnlyCurrentTeachersRows() {
        TestableSecurityUtils securityUtils = new TestableSecurityUtils();
        securityUtils.teacher = true;
        securityUtils.currentTeacherId = 100L;

        TimetableDetail ownDetail = new TimetableDetail();
        ownDetail.setTeacherId(100L);
        TimetableDetail otherDetail = new TimetableDetail();
        otherDetail.setTeacherId(200L);

        List<TimetableDetail> details = securityUtils.filterTimetableDetails(List.of(ownDetail, otherDetail));

        assertEquals(1, details.size());
        assertTrue(details.stream().allMatch(detail -> detail.getTeacherId().equals(100L)));
    }

    private static class TestableSecurityUtils extends SecurityUtils {
        private boolean admin;
        private boolean teacher;
        private boolean student;
        private Long currentTeacherId;
        private Long currentClassId;

        @Override
        public boolean isAdmin() {
            return admin;
        }

        @Override
        public boolean isTeacher() {
            return teacher;
        }

        @Override
        public boolean isStudent() {
            return student;
        }

        @Override
        public Long requireCurrentTeacherId() {
            return currentTeacherId;
        }

        @Override
        public Long requireCurrentStudentClassId() {
            return currentClassId;
        }
    }
}
