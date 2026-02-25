package com.paike.admin.utils;

import com.paike.admin.entity.User;
import com.paike.admin.mapper.UserMapper;
import com.paike.common.utils.JwtUtils;
import com.paike.common.utils.ServletUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    public Long getCurrentUserId() {
        String token = ServletUtils.getRequest().getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtils.getUserIdFromToken(token);
        }
        return null;
    }

    public String getCurrentUsername() {
        String token = ServletUtils.getRequest().getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtils.getUsernameFromToken(token);
        }
        return null;
    }

    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId != null) {
            return userMapper.selectById(userId);
        }
        return null;
    }
}
