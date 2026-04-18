package com.paike.admin.service;

import com.paike.admin.dto.ChangePasswordRequest;
import com.paike.admin.dto.CurrentUserInfoResponse;
import com.paike.admin.dto.LoginRequest;
import com.paike.admin.dto.LoginResponse;
import com.paike.admin.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {

    LoginResponse login(LoginRequest request);

    User getByUsername(String username);

    CurrentUserInfoResponse buildCurrentUserInfo(User user);

    void updateLoginInfo(Long userId, String ip);

    void changePassword(ChangePasswordRequest request);
}
