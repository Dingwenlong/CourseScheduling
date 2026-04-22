package com.paike.admin.controller;

import com.paike.admin.dto.ChangePasswordRequest;
import com.paike.admin.dto.CurrentUserInfoResponse;
import com.paike.admin.dto.LoginRequest;
import com.paike.admin.dto.LoginResponse;
import com.paike.admin.dto.ResetPasswordRequest;
import com.paike.admin.dto.UpdateCurrentProfileRequest;
import com.paike.admin.entity.User;
import com.paike.admin.mapper.UserMapper;
import com.paike.admin.service.UserService;
import com.paike.admin.utils.SecurityUtils;
import com.paike.common.result.Result;
import com.paike.common.utils.JwtUtils;
import com.paike.common.utils.PasswordUtils;
import com.paike.common.utils.RedisUtils;
import com.paike.common.utils.ServletUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@Tag(name = "认证管理", description = "认证相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String DEFAULT_PASSWORD = "123456";

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<CurrentUserInfoResponse> info() {
        User user = securityUtils.getCurrentUser();
        return Result.success(userService.buildCurrentUserInfo(user));
    }

    @Operation(summary = "更新当前用户资料")
    @PutMapping("/profile")
    public Result<CurrentUserInfoResponse> updateProfile(@RequestBody UpdateCurrentProfileRequest request) {
        return Result.success(userService.updateCurrentProfile(request));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        String token = ServletUtils.getRequest().getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long expiration = jwtUtils.getExpire(token);
            if (expiration != null && expiration > 0) {
                redisUtils.set(TOKEN_BLACKLIST_PREFIX + token, 1, expiration, TimeUnit.SECONDS);
            }
        }
        return Result.success();
    }

    @Operation(summary = "重置用户密码")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        User user = userService.getByUsername(request.getUsername());
        if (user == null) {
            return Result.fail(1001, "用户不存在");
        }
        user.setPassword(PasswordUtils.encode(DEFAULT_PASSWORD));
        userMapper.updateById(user);
        return Result.success("密码已重置为: " + DEFAULT_PASSWORD + "，请提醒用户尽快修改密码");
    }

    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return Result.success();
    }
}
