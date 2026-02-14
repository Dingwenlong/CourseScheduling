package com.paike.admin.controller;

import com.paike.admin.dto.LoginRequest;
import com.paike.admin.dto.LoginResponse;
import com.paike.admin.entity.User;
import com.paike.admin.mapper.UserMapper;
import com.paike.admin.service.UserService;
import com.paike.common.result.Result;
import com.paike.common.utils.PasswordUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "认证相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<?> info() {
        return Result.success("功能开发中");
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @Operation(summary = "重置用户密码为123456")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestParam("username") String username) {
        User user = userService.getByUsername(username);
        if (user == null) {
            return Result.fail(1001, "用户不存在");
        }
        user.setPassword(PasswordUtils.encode("123456"));
        userMapper.updateById(user);
        return Result.success("密码已重置为: 123456");
    }
}
