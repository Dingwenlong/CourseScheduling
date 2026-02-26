-- 智能排课系统测试数据
-- Database: course_scheduling

SET NAMES utf8mb4;

USE course_scheduling;

-- ----------------------------
-- 1. 校区数据
-- ----------------------------
INSERT INTO sys_campus (campus_code, campus_name, address, commute_time, sort_order, status) VALUES
('MAIN', '主校区', '北京市海淀区学院路1号', 0, 1, 1),
('EAST', '东校区', '北京市海淀区学院路100号', 15, 2, 1),
('SOUTH', '南校区', '北京市海淀区学院路200号', 20, 3, 1);

-- ----------------------------
-- 2. 院系数据
-- ----------------------------
INSERT INTO sys_department (dept_code, dept_name, campus_id, parent_id, leader_name, leader_phone, sort_order, status) VALUES
('CS', '计算机学院', 1, 0, '张教授', '13800000001', 1, 1),
('EE', '电子工程学院', 1, 0, '李教授', '13800000002', 2, 1),
('MATH', '数学学院', 1, 0, '王教授', '13800000003', 3, 1),
('PHYS', '物理学院', 2, 0, '赵教授', '13800000004', 4, 1),
('ENG', '外国语学院', 3, 0, '刘教授', '13800000005', 5, 1);

-- ----------------------------
-- 3. 用户数据 (密码都是 123456，使用BCrypt加密)
-- ----------------------------
INSERT INTO sys_user (username, password, real_name, role, phone, email, status) VALUES
('admin', '$2a$10$EqKcp1WFKVQISheBxmXJGePJwJbvHfEFvEqJjGWQv2Mb6AqPQvWIi', '系统管理员', 'ADMIN', '13800000000', 'admin@example.com', 1),
('teacher001', '$2a$10$EqKcp1WFKVQISheBxmXJGePJwJbvHfEFvEqJjGWQv2Mb6AqPQvWIi', '张三', 'TEACHER', '13800000011', 'zhangsan@example.com', 1),
('teacher002', '$2a$10$EqKcp1WFKVQISheBxmXJGePJwJbvHfEFvEqJjGWQv2Mb6AqPQvWIi', '李四', 'TEACHER', '13800000012', 'lisi@example.com', 1),
('teacher003', '$2a$10$EqKcp1WFKVQISheBxmXJGePJwJbvHfEFvEqJjGWQv2Mb6AqPQvWIi', '王五', 'TEACHER', '13800000013', 'wangwu@example.com', 1),
('teacher004', '$2a$10$EqKcp1WFKVQISheBxmXJGePJwJbvHfEFvEqJjGWQv2Mb6AqPQvWIi', '赵六', 'TEACHER', '13800000014', 'zhaoliu@example.com', 1),
('teacher005', '$2a$10$EqKcp1WFKVQISheBxmXJGePJwJbvHfEFvEqJjGWQv2Mb6AqPQvWIi', '钱七', 'TEACHER', '13800000015', 'qianqi@example.com', 1),
('student001', '$2a$10$EqKcp1WFKVQISheBxmXJGePJwJbvHfEFvEqJjGWQv2Mb6AqPQvWIi', '学生甲', 'STUDENT', '13800000021', 'student001@example.com', 1),
('student002', '$2a$10$EqKcp1WFKVQISheBxmXJGePJwJbvHfEFvEqJjGWQv2Mb6AqPQvWIi', '学生乙', 'STUDENT', '13800000022', 'student002@example.com', 1);

-- ----------------------------
-- 4. 教师数据
-- ----------------------------
INSERT INTO edu_teacher (user_id, teacher_no, dept_id, title, research_area, office_location, max_hours_per_week, campus_id, status) VALUES
(2, 'T001', 1, '教授', '人工智能', '计算机楼301', 20, 1, 1),
(3, 'T002', 1, '副教授', '软件工程', '计算机楼302', 18, 1, 1),
(4, 'T003', 2, '教授', '通信工程', '电子楼201', 20, 1, 1),
(5, 'T004', 3, '副教授', '应用数学', '数学楼101', 16, 1, 1),
(6, 'T005', 4, '讲师', '理论物理', '物理楼301', 14, 2, 1);

-- ----------------------------
-- 5. 班级数据
-- ----------------------------
INSERT INTO edu_class (class_code, class_name, dept_id, grade, student_count, counselor_name, counselor_phone, status) VALUES
('CS202101', '计算机科学2021级1班', 1, '2021', 45, '辅导员A', '13900000001', 1),
('CS202102', '计算机科学2021级2班', 1, '2021', 42, '辅导员A', '13900000001', 1),
('CS202201', '计算机科学2022级1班', 1, '2022', 48, '辅导员B', '13900000002', 1),
('EE202101', '电子工程2021级1班', 2, '2021', 40, '辅导员C', '13900000003', 1),
('EE202201', '电子工程2022级1班', 2, '2022', 44, '辅导员C', '13900000003', 1),
('MATH202101', '数学2021级1班', 3, '2021', 35, '辅导员D', '13900000004', 1);

-- ----------------------------
-- 6. 学生数据
-- ----------------------------
INSERT INTO edu_student (user_id, student_no, dept_id, class_id, grade, status) VALUES
(7, 'S2021001', 1, 1, '2021', 1),
(8, 'S2021002', 1, 1, '2021', 1);

-- ----------------------------
-- 7. 教室数据
-- ----------------------------
INSERT INTO edu_classroom (room_no, room_name, campus_id, building, floor, capacity, room_type, has_projector, has_microphone, has_air_conditioner, status) VALUES
('A101', '教学楼A101', 1, '教学楼A', 1, 60, 'GENERAL', 1, 1, 1, 1),
('A102', '教学楼A102', 1, '教学楼A', 1, 50, 'GENERAL', 1, 0, 1, 1),
('A201', '教学楼A201', 1, '教学楼A', 2, 80, 'MULTIMEDIA', 1, 1, 1, 1),
('A202', '教学楼A202', 1, '教学楼A', 2, 100, 'LECTURE_HALL', 1, 1, 1, 1),
('A301', '教学楼A301', 1, '教学楼A', 3, 45, 'GENERAL', 1, 0, 1, 1),
('B101', '教学楼B101', 1, '教学楼B', 1, 40, 'LAB', 0, 0, 1, 1),
('B102', '教学楼B102', 1, '教学楼B', 1, 40, 'LAB', 0, 0, 1, 1),
('B201', '教学楼B201', 1, '教学楼B', 2, 50, 'COMPUTER', 1, 0, 1, 1),
('B202', '教学楼B202', 1, '教学楼B', 2, 50, 'COMPUTER', 1, 0, 1, 1),
('C101', '教学楼C101', 2, '教学楼C', 1, 60, 'GENERAL', 1, 1, 1, 1),
('C102', '教学楼C102', 2, '教学楼C', 1, 55, 'MULTIMEDIA', 1, 1, 1, 1),
('C201', '教学楼C201', 2, '教学楼C', 2, 90, 'LECTURE_HALL', 1, 1, 1, 1);

-- ----------------------------
-- 8. 课程数据
-- ----------------------------
INSERT INTO edu_course (course_code, course_name, course_type, credit, theory_hours, practice_hours, total_hours, dept_id, priority, need_multimedia, need_lab, status) VALUES
('CS101', '程序设计基础', 'REQUIRED', 4.0, 48, 16, 64, 1, 1, 1, 0, 1),
('CS102', '数据结构', 'REQUIRED', 4.0, 48, 16, 64, 1, 1, 1, 0, 1),
('CS201', '操作系统', 'REQUIRED', 3.5, 48, 8, 56, 1, 2, 1, 0, 1),
('CS202', '计算机网络', 'REQUIRED', 3.5, 40, 16, 56, 1, 2, 1, 0, 1),
('CS301', '人工智能导论', 'ELECTIVE', 3.0, 32, 16, 48, 1, 3, 1, 0, 1),
('CS302', '软件工程', 'REQUIRED', 3.0, 40, 8, 48, 1, 2, 1, 0, 1),
('EE101', '电路分析', 'REQUIRED', 4.0, 48, 16, 64, 2, 1, 0, 1, 1),
('EE102', '模拟电子技术', 'REQUIRED', 3.5, 40, 16, 56, 2, 1, 0, 1, 1),
('EE201', '数字信号处理', 'REQUIRED', 3.0, 40, 8, 48, 2, 2, 1, 0, 1),
('MATH101', '高等数学A(上)', 'REQUIRED', 5.0, 80, 0, 80, 3, 1, 0, 0, 1),
('MATH102', '高等数学A(下)', 'REQUIRED', 5.0, 80, 0, 80, 3, 1, 0, 0, 1),
('MATH201', '线性代数', 'REQUIRED', 3.0, 48, 0, 48, 3, 1, 0, 0, 1),
('PHYS101', '大学物理(上)', 'REQUIRED', 4.0, 56, 8, 64, 4, 1, 1, 0, 1),
('PHYS102', '大学物理(下)', 'REQUIRED', 4.0, 56, 8, 64, 4, 1, 1, 0, 1);

-- ----------------------------
-- 9. 教学任务数据 (2024-1学期)
-- ----------------------------
INSERT INTO edu_teaching_task (semester, course_id, teacher_id, class_id, student_count, weekly_hours, total_weeks, weeks, course_nature, priority_level, status) VALUES
('2024-1', 1, 1, 1, 45, 4, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 1, 1, 2, 42, 4, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 2, 2, 1, 45, 4, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 2, 2, 2, 42, 4, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 3, 1, 3, 48, 4, 16, '1-16', 'REQUIRED', 2, 'PENDING'),
('2024-1', 4, 2, 3, 48, 4, 16, '1-16', 'REQUIRED', 2, 'PENDING'),
('2024-1', 5, 1, 1, 45, 3, 16, '1-16', 'ELECTIVE', 3, 'PENDING'),
('2024-1', 6, 2, 2, 42, 3, 16, '1-16', 'REQUIRED', 2, 'PENDING'),
('2024-1', 7, 3, 4, 40, 4, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 7, 3, 5, 44, 4, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 8, 3, 4, 40, 4, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 10, 4, 1, 45, 5, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 10, 4, 2, 42, 5, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 10, 4, 3, 48, 5, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 12, 4, 1, 45, 3, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 12, 4, 4, 40, 3, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 13, 5, 1, 45, 4, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 13, 5, 4, 40, 4, 16, '1-16', 'REQUIRED', 1, 'PENDING'),
('2024-1', 13, 5, 6, 35, 4, 16, '1-16', 'REQUIRED', 1, 'PENDING');
