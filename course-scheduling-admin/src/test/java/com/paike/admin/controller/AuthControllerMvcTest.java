package com.paike.admin.controller;

import com.paike.admin.dto.CurrentUserInfoResponse;
import com.paike.admin.entity.User;
import com.paike.admin.mapper.UserMapper;
import com.paike.admin.security.JwtAuthenticationFilter;
import com.paike.admin.service.UserService;
import com.paike.admin.utils.SecurityUtils;
import com.paike.common.utils.JwtUtils;
import com.paike.common.utils.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Proxy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerMvcTest {

    private MockMvc mockMvc;
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", "paike-scheduling-system-secret-key-2024");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 86400000L);
    }

    @Test
    void infoReturnsHttp403AndUserDisabledCodeWhenTokenUserDisabled() throws Exception {
        mockMvc = buildMockMvc(userMapperReturning(buildUser(1L, "teacher1", "TEACHER", 0)));

        String token = jwtUtils.generateToken(1L, "teacher1", "TEACHER");
        mockMvc.perform(get("/auth/info").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(1003))
                .andExpect(jsonPath("$.message").value("用户已被禁用"));
    }

    @Test
    void infoReturnsHttp403AndTokenInvalidCodeWhenRoleChangedAfterLogin() throws Exception {
        mockMvc = buildMockMvc(userMapperReturning(buildUser(1L, "teacher1", "ADMIN", 1)));

        String token = jwtUtils.generateToken(1L, "teacher1", "TEACHER");
        mockMvc.perform(get("/auth/info").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(1006))
                .andExpect(jsonPath("$.message").value("Token无效"));
    }

    @Test
    void infoReturnsTeacherAndClassContextWhenTokenIsValid() throws Exception {
        User user = buildUser(1L, "teacher1", "TEACHER", 1);
        mockMvc = buildMockMvc(userMapperReturning(user));

        String token = jwtUtils.generateToken(1L, "teacher1", "TEACHER");
        mockMvc.perform(get("/auth/info").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("teacher1"))
                .andExpect(jsonPath("$.data.teacherId").value(101))
                .andExpect(jsonPath("$.data.classId").value(301));
    }

    private MockMvc buildMockMvc(UserMapper userMapper) {
        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "userService", userServiceReturning());
        ReflectionTestUtils.setField(controller, "securityUtils", securityUtilsReturning(buildUser(1L, "teacher1", "TEACHER", 1)));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "jwtUtils", jwtUtils);
        ReflectionTestUtils.setField(filter, "redisUtils", new RedisUtils() {
            @Override
            public Boolean hasKey(String key) {
                return false;
            }
        });
        ReflectionTestUtils.setField(filter, "userMapper", userMapper);

        return MockMvcBuilders.standaloneSetup(controller)
                .addFilters(filter)
                .build();
    }

    private User buildUser(Long id, String username, String role, Integer status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    private UserMapper userMapperReturning(User user) {
        return (UserMapper) Proxy.newProxyInstance(
                UserMapper.class.getClassLoader(),
                new Class[]{UserMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return user;
                    }
                    return null;
                });
    }

    private UserService userServiceReturning() {
        return (UserService) Proxy.newProxyInstance(
                UserService.class.getClassLoader(),
                new Class[]{UserService.class},
                (proxy, method, args) -> {
                    if ("buildCurrentUserInfo".equals(method.getName())) {
                        CurrentUserInfoResponse response = new CurrentUserInfoResponse();
                        response.setId(1L);
                        response.setUsername("teacher1");
                        response.setRole("TEACHER");
                        response.setTeacherId(101L);
                        response.setClassId(301L);
                        return response;
                    }
                    return null;
                });
    }

    private SecurityUtils securityUtilsReturning(User user) {
        return new SecurityUtils() {
            @Override
            public User getCurrentUser() {
                return user;
            }
        };
    }
}
