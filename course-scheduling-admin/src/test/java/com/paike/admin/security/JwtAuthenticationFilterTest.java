package com.paike.admin.security;

import com.paike.admin.entity.User;
import com.paike.admin.mapper.UserMapper;
import com.paike.common.utils.JwtUtils;
import com.paike.common.utils.RedisUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private StubRedisUtils redisUtils;
    private JwtUtils jwtUtils;
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        redisUtils = new StubRedisUtils();
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", "paike-scheduling-system-secret-key-2024");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 86400000L);
        ReflectionTestUtils.setField(filter, "jwtUtils", jwtUtils);
        ReflectionTestUtils.setField(filter, "redisUtils", redisUtils);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterAuthenticatesWhenTokenMatchesActiveUser() throws Exception {
        User user = buildUser(1L, "teacher1", "TEACHER", 1);
        userMapper = userMapperReturning(user);
        ReflectionTestUtils.setField(filter, "userMapper", userMapper);

        String token = jwtUtils.generateToken(1L, "teacher1", "TEACHER");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(200, response.getStatus());
        assertEquals(1L, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_TEACHER".equals(authority.getAuthority())));
    }

    @Test
    void doFilterRejectsDisabledUserEvenWithValidToken() throws Exception {
        User user = buildUser(1L, "teacher1", "TEACHER", 0);
        userMapper = userMapperReturning(user);
        ReflectionTestUtils.setField(filter, "userMapper", userMapper);

        String token = jwtUtils.generateToken(1L, "teacher1", "TEACHER");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":1003"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterRejectsTokenWhenRoleChangedAfterLogin() throws Exception {
        User user = buildUser(1L, "teacher1", "ADMIN", 1);
        userMapper = userMapperReturning(user);
        ReflectionTestUtils.setField(filter, "userMapper", userMapper);

        String token = jwtUtils.generateToken(1L, "teacher1", "TEACHER");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":1006"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
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

    private static class StubRedisUtils extends RedisUtils {
        @Override
        public Boolean hasKey(String key) {
            return false;
        }
    }
}
