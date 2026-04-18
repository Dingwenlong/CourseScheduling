package com.paike.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paike.admin.dto.LookupOptionResponse;
import com.paike.admin.entity.Clazz;
import com.paike.admin.entity.Classroom;
import com.paike.admin.entity.Course;
import com.paike.admin.entity.Teacher;
import com.paike.admin.entity.User;
import com.paike.admin.mapper.ClassMapper;
import com.paike.admin.mapper.ClassroomMapper;
import com.paike.admin.mapper.CourseMapper;
import com.paike.admin.mapper.TeacherMapper;
import com.paike.admin.mapper.UserMapper;
import com.paike.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Tag(name = "轻量字典查询", description = "前端搜索选择器使用的轻量查询接口")
@RestController
@RequestMapping("/lookup")
@PreAuthorize("hasRole('ADMIN')")
public class LookupController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private ClassroomMapper classroomMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Operation(summary = "搜索教师")
    @GetMapping("/teachers")
    public Result<List<LookupOptionResponse>> teachers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer limit) {
        int pageSize = normalizeLimit(limit);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getStatus, 1)
                .orderByAsc(Teacher::getTeacherNo)
                .last("LIMIT " + pageSize);
        if (StringUtils.hasText(keyword)) {
            List<Long> matchedUserIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                            .select(User::getId)
                            .like(User::getRealName, keyword.trim()))
                    .stream()
                    .map(User::getId)
                    .toList();
            wrapper.and(q -> {
                q.like(Teacher::getTeacherNo, keyword.trim());
                if (!matchedUserIds.isEmpty()) {
                    q.or().in(Teacher::getUserId, matchedUserIds);
                }
            });
        }

        List<Teacher> teachers = teacherMapper.selectList(wrapper);
        if (teachers.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        Set<Long> userIds = teachers.stream()
                .map(Teacher::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, User> users = loadUsers(userIds);

        List<LookupOptionResponse> options = teachers.stream()
                .map(teacher -> {
                    User user = users.get(teacher.getUserId());
                    String realName = user != null ? user.getRealName() : null;
                    String label = StringUtils.hasText(realName)
                            ? realName + " (" + teacher.getTeacherNo() + ")"
                            : teacher.getTeacherNo();
                    return LookupOptionResponse.of(teacher.getId(), label, teacher.getTeacherNo());
                })
                .toList();
        return Result.success(options);
    }

    @Operation(summary = "搜索班级")
    @GetMapping("/classes")
    public Result<List<LookupOptionResponse>> classes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer limit) {
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<Clazz>()
                .eq(Clazz::getStatus, 1)
                .orderByAsc(Clazz::getClassCode)
                .last("LIMIT " + normalizeLimit(limit));
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(Clazz::getClassCode, keyword).or().like(Clazz::getClassName, keyword));
        }

        List<LookupOptionResponse> options = classMapper.selectList(wrapper).stream()
                .map(clazz -> LookupOptionResponse.of(
                        clazz.getId(),
                        clazz.getClassName() + " (" + clazz.getClassCode() + ")",
                        clazz.getClassCode()
                ))
                .toList();
        return Result.success(options);
    }

    @Operation(summary = "搜索教室")
    @GetMapping("/classrooms")
    public Result<List<LookupOptionResponse>> classrooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer limit) {
        LambdaQueryWrapper<Classroom> wrapper = new LambdaQueryWrapper<Classroom>()
                .eq(Classroom::getStatus, 1)
                .orderByAsc(Classroom::getRoomNo)
                .last("LIMIT " + normalizeLimit(limit));
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(Classroom::getRoomNo, keyword)
                    .or().like(Classroom::getRoomName, keyword)
                    .or().like(Classroom::getBuilding, keyword));
        }

        List<LookupOptionResponse> options = classroomMapper.selectList(wrapper).stream()
                .map(classroom -> LookupOptionResponse.of(
                        classroom.getId(),
                        buildClassroomLabel(classroom),
                        classroom.getRoomNo()
                ))
                .toList();
        return Result.success(options);
    }

    @Operation(summary = "搜索课程")
    @GetMapping("/courses")
    public Result<List<LookupOptionResponse>> courses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer limit) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, 1)
                .orderByAsc(Course::getCourseCode)
                .last("LIMIT " + normalizeLimit(limit));
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(Course::getCourseCode, keyword).or().like(Course::getCourseName, keyword));
        }

        List<LookupOptionResponse> options = courseMapper.selectList(wrapper).stream()
                .map(course -> LookupOptionResponse.of(
                        course.getId(),
                        course.getCourseName() + " (" + course.getCourseCode() + ")",
                        course.getCourseCode()
                ))
                .toList();
        return Result.success(options);
    }

    private Map<Long, User> loadUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String buildClassroomLabel(Classroom classroom) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(classroom.getRoomName())) {
            builder.append(classroom.getRoomName());
        } else {
            builder.append(classroom.getRoomNo());
        }
        if (StringUtils.hasText(classroom.getRoomNo()) && !builder.toString().contains(classroom.getRoomNo())) {
            builder.append(" (").append(classroom.getRoomNo()).append(")");
        }
        if (StringUtils.hasText(classroom.getBuilding())) {
            builder.append(" · ").append(classroom.getBuilding());
        }
        return builder.toString();
    }
}
