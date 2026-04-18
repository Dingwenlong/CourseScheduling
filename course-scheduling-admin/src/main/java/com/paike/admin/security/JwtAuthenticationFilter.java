package com.paike.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paike.admin.entity.User;
import com.paike.admin.mapper.UserMapper;
import com.paike.common.result.Result;
import com.paike.common.result.ResultCode;
import com.paike.common.utils.JwtUtils;
import com.paike.common.utils.RedisUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            if (Boolean.TRUE.equals(redisUtils.hasKey(TOKEN_BLACKLIST_PREFIX + token))) {
                log.warn("Token已在黑名单中，拒绝访问");
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ResultCode.FORBIDDEN);
                return;
            }

            if (jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserIdFromToken(token);
                String username = jwtUtils.getUsernameFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);

                if (userId != null && username != null && role != null) {
                    User user = userMapper.selectById(userId);
                    if (user == null) {
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ResultCode.TOKEN_INVALID);
                        return;
                    }
                    if (!Integer.valueOf(1).equals(user.getStatus())) {
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ResultCode.USER_DISABLED);
                        return;
                    }
                    if (!username.equals(user.getUsername()) || !role.equals(user.getRole())) {
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ResultCode.TOKEN_INVALID);
                        return;
                    }

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.singletonList(authority));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ResultCode.TOKEN_INVALID);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, ResultCode resultCode) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<?> result = Result.fail(resultCode);
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
