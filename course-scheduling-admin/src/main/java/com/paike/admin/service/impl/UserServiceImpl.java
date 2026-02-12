package com.paike.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paike.admin.dto.LoginRequest;
import com.paike.admin.dto.LoginResponse;
import com.paike.admin.entity.User;
import com.paike.admin.mapper.UserMapper;
import com.paike.admin.service.UserService;
import com.paike.common.exception.BusinessException;
import com.paike.common.result.ResultCode;
import com.paike.common.utils.JwtUtils;
import com.paike.common.utils.PasswordUtils;
import com.paike.common.utils.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = getByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!PasswordUtils.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        updateLoginInfo(user.getId(), ServletUtils.getClientIp());

        return LoginResponse.of(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public void updateLoginInfo(Long userId, String ip) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        updateById(user);
    }
}
