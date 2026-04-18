package com.paike.admin.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paike.admin.entity.Student;
import com.paike.admin.entity.Teacher;
import com.paike.admin.entity.TeachingTask;
import com.paike.admin.entity.TimetableDetail;
import com.paike.admin.entity.User;
import com.paike.admin.mapper.StudentMapper;
import com.paike.admin.mapper.TeacherMapper;
import com.paike.admin.mapper.UserMapper;
import com.paike.common.constants.UserRole;
import com.paike.common.exception.BusinessException;
import com.paike.common.result.ResultCode;
import com.paike.common.utils.JwtUtils;
import com.paike.common.utils.ServletUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class SecurityUtils {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapper studentMapper;

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

    public User requireCurrentUser() {
        User user = getCurrentUser();
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        if (!Objects.equals(user.getStatus(), 1)) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        return user;
    }

    public String getCurrentRole() {
        User user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN.getCode());
    }

    public boolean isTeacher() {
        return hasRole(UserRole.TEACHER.getCode());
    }

    public boolean isStudent() {
        return hasRole(UserRole.STUDENT.getCode());
    }

    public Teacher getCurrentTeacher() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return teacherMapper.selectOne(new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getUserId, userId)
                .last("LIMIT 1"));
    }

    public Student getCurrentStudent() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId)
                .last("LIMIT 1"));
    }

    public Long requireCurrentTeacherId() {
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "当前教师身份未绑定教师档案");
        }
        return teacher.getId();
    }

    public Long requireCurrentStudentClassId() {
        Student student = getCurrentStudent();
        if (student == null || student.getClassId() == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "当前学生身份未绑定班级档案");
        }
        return student.getClassId();
    }

    public void checkTeachingTaskAccess(TeachingTask task) {
        if (task == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        if (isAdmin()) {
            return;
        }
        if (isTeacher() && Objects.equals(task.getTeacherId(), requireCurrentTeacherId())) {
            return;
        }
        throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该教学任务");
    }

    public void checkTeacherAccess(Long teacherId) {
        if (isAdmin()) {
            return;
        }
        if (isTeacher() && Objects.equals(teacherId, requireCurrentTeacherId())) {
            return;
        }
        throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该教师课表");
    }

    public void checkClassAccess(Long classId) {
        if (isAdmin()) {
            return;
        }
        if (isStudent() && Objects.equals(classId, requireCurrentStudentClassId())) {
            return;
        }
        throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该班级课表");
    }

    public void checkClassroomAccess() {
        if (!isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问教室课表");
        }
    }

    public List<TimetableDetail> filterTimetableDetails(List<TimetableDetail> details) {
        if (details == null || details.isEmpty()) {
            return Collections.emptyList();
        }
        if (isAdmin()) {
            return details;
        }
        if (isTeacher()) {
            Long teacherId = requireCurrentTeacherId();
            return details.stream()
                    .filter(detail -> Objects.equals(detail.getTeacherId(), teacherId))
                    .toList();
        }
        if (isStudent()) {
            Long classId = requireCurrentStudentClassId();
            return details.stream()
                    .filter(detail -> Objects.equals(detail.getClassId(), classId))
                    .toList();
        }
        throw new BusinessException(ResultCode.FORBIDDEN, "无权访问课表数据");
    }

    private boolean hasRole(String roleCode) {
        String role = getCurrentRole();
        return role != null && role.equalsIgnoreCase(roleCode);
    }
}
