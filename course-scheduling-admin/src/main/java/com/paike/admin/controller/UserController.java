package com.paike.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paike.admin.dto.UserCreateRequest;
import com.paike.admin.dto.UserQueryRequest;
import com.paike.admin.dto.UserUpdateRequest;
import com.paike.admin.entity.User;
import com.paike.admin.service.UserService;
import com.paike.common.result.PageResult;
import com.paike.common.result.Result;
import com.paike.common.result.ResultCode;
import com.paike.common.utils.PasswordUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<User>> list(UserQueryRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            wrapper.like(User::getUsername, request.getUsername());
        }
        if (request.getRealName() != null && !request.getRealName().isEmpty()) {
            wrapper.like(User::getRealName, request.getRealName());
        }
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            wrapper.eq(User::getRole, request.getRole());
        }
        if (request.getStatus() != null) {
            wrapper.eq(User::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<User> result = userService.page(page, wrapper);

        result.getRecords().forEach(user -> user.setPassword(null));

        return Result.success(PageResult.of(
            result.getRecords(),
            result.getTotal(),
            (long) result.getSize(),
            (long) result.getCurrent()
        ));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@Valid @RequestBody UserCreateRequest request) {
        User existingUser = userService.getByUsername(request.getUsername());
        if (existingUser != null) {
            return Result.fail(ResultCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtils.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());

        userService.save(user);
        return Result.success();
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        if (!id.equals(request.getId())) {
            return Result.fail(ResultCode.PARAM_ERROR);
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND);
        }

        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());

        userService.updateById(user);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND);
        }

        userService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "重置用户密码")
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> resetPassword(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND);
        }

        String defaultPassword = "123456";
        user.setPassword(PasswordUtils.encode(defaultPassword));
        userService.updateById(user);

        return Result.success("密码已重置为: " + defaultPassword);
    }

    @Operation(summary = "启用/禁用用户")
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail(ResultCode.USER_NOT_FOUND);
        }

        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userService.updateById(user);

        return Result.success();
    }
}
