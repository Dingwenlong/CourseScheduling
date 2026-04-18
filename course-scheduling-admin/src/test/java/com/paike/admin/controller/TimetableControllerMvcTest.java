package com.paike.admin.controller;

import com.paike.admin.service.TimetableDetailService;
import com.paike.admin.utils.SecurityUtils;
import com.paike.common.exception.BusinessException;
import com.paike.common.exception.GlobalExceptionHandler;
import com.paike.common.result.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Proxy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TimetableControllerMvcTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TimetableController controller = new TimetableController();
        ReflectionTestUtils.setField(controller, "securityUtils", new SecurityUtils() {
            @Override
            public void checkClassAccess(Long classId) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该班级课表");
            }
        });
        ReflectionTestUtils.setField(controller, "timetableDetailService", emptyTimetableDetailService());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getClassTimetableReturnsHttp403WhenStudentReadsAnotherClass() throws Exception {
        mockMvc.perform(get("/timetable/1/class/4001"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权访问该班级课表"));
    }

    private TimetableDetailService emptyTimetableDetailService() {
        return (TimetableDetailService) Proxy.newProxyInstance(
                TimetableDetailService.class.getClassLoader(),
                new Class[]{TimetableDetailService.class},
                (proxy, method, args) -> {
                    if (method.getReturnType().equals(java.util.List.class)) {
                        return java.util.List.of();
                    }
                    if (method.getReturnType().equals(int.class)) {
                        return 0;
                    }
                    if (method.getReturnType().equals(boolean.class)) {
                        return false;
                    }
                    if (method.getReturnType().equals(long.class)) {
                        return 0L;
                    }
                    return null;
                });
    }
}
