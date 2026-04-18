package com.paike.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.paike.admin.dto.ChangePasswordRequest;
import com.paike.admin.dto.CurrentUserInfoResponse;
import com.paike.admin.dto.LoginRequest;
import com.paike.admin.dto.LoginResponse;
import com.paike.admin.entity.Student;
import com.paike.admin.entity.Teacher;
import com.paike.admin.entity.User;
import com.paike.admin.mapper.StudentMapper;
import com.paike.admin.mapper.TeacherMapper;
import com.paike.admin.mapper.UserMapper;
import com.paike.admin.service.UserService;
import com.paike.admin.utils.SecurityUtils;
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

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapper studentMapper;

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

        Teacher teacher = teacherMapper.selectOne(new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getUserId, user.getId())
                .last("LIMIT 1"));
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, user.getId())
                .last("LIMIT 1"));

        return LoginResponse.of(
                token,
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole(),
                teacher != null ? teacher.getId() : null,
                student != null ? student.getClassId() : null
        );
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public CurrentUserInfoResponse buildCurrentUserInfo(User user) {
        if (user == null) {
            return null;
        }

        Teacher teacher = teacherMapper.selectOne(new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getUserId, user.getId())
                .last("LIMIT 1"));
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, user.getId())
                .last("LIMIT 1"));

        CurrentUserInfoResponse response = new CurrentUserInfoResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setStatus(user.getStatus());
        response.setTeacherId(teacher != null ? teacher.getId() : null);
        response.setClassId(student != null ? student.getClassId() : null);
        return response;
    }

    @Override
    public void updateLoginInfo(Long userId, String ip) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        updateById(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }

        if (!PasswordUtils.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(1002, "两次密码输入不一致");
        }

        currentUser.setPassword(PasswordUtils.encode(request.getNewPassword()));
        updateById(currentUser);
    }
}
