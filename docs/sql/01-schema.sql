-- 智能排课系统数据库初始化脚本
-- Database: course_scheduling

SET NAMES utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS course_scheduling DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE course_scheduling;

-- ----------------------------
-- 1. 校区表
-- ----------------------------
DROP TABLE IF EXISTS sys_campus;
CREATE TABLE sys_campus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    campus_code VARCHAR(50) NOT NULL COMMENT '校区编码',
    campus_name VARCHAR(100) NOT NULL COMMENT '校区名称',
    address VARCHAR(255) COMMENT '校区地址',
    commute_time INT DEFAULT 0 COMMENT '通勤时间(分钟)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_campus_code (campus_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校区表';

-- ----------------------------
-- 2. 院系表
-- ----------------------------
DROP TABLE IF EXISTS sys_department;
CREATE TABLE sys_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dept_code VARCHAR(50) NOT NULL COMMENT '院系编码',
    dept_name VARCHAR(100) NOT NULL COMMENT '院系名称',
    campus_id BIGINT COMMENT '所属校区ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    leader_name VARCHAR(50) COMMENT '负责人姓名',
    leader_phone VARCHAR(20) COMMENT '负责人电话',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_dept_code (dept_code),
    KEY idx_campus_id (campus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院系表';

-- ----------------------------
-- 3. 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL COMMENT '角色：ADMIN-管理员，TEACHER-教师，STUDENT-学生',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    KEY idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 4. 班级表
-- ----------------------------
DROP TABLE IF EXISTS edu_class;
CREATE TABLE edu_class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    class_code VARCHAR(50) NOT NULL COMMENT '班级编码',
    class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
    dept_id BIGINT COMMENT '所属院系ID',
    grade VARCHAR(20) COMMENT '年级',
    student_count INT DEFAULT 0 COMMENT '学生人数',
    counselor_name VARCHAR(50) COMMENT '辅导员姓名',
    counselor_phone VARCHAR(20) COMMENT '辅导员电话',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_class_code (class_code),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- ----------------------------
-- 5. 教师表
-- ----------------------------
DROP TABLE IF EXISTS edu_teacher;
CREATE TABLE edu_teacher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '关联用户ID',
    teacher_no VARCHAR(50) NOT NULL COMMENT '工号',
    dept_id BIGINT COMMENT '所属院系ID',
    title VARCHAR(50) COMMENT '职称',
    research_area VARCHAR(255) COMMENT '研究方向',
    office_location VARCHAR(100) COMMENT '办公地点',
    office_phone VARCHAR(20) COMMENT '办公电话',
    max_hours_per_week INT DEFAULT 20 COMMENT '每周最大课时',
    campus_id BIGINT COMMENT '所属校区ID',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_teacher_no (teacher_no),
    KEY idx_user_id (user_id),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- ----------------------------
-- 6. 学生表
-- ----------------------------
DROP TABLE IF EXISTS edu_student;
CREATE TABLE edu_student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '关联用户ID',
    student_no VARCHAR(50) NOT NULL COMMENT '学号',
    dept_id BIGINT COMMENT '所属院系ID',
    class_id BIGINT COMMENT '所属班级ID',
    grade VARCHAR(20) COMMENT '年级',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_no (student_no),
    KEY idx_user_id (user_id),
    KEY idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- ----------------------------
-- 7. 教室表
-- ----------------------------
DROP TABLE IF EXISTS edu_classroom;
CREATE TABLE edu_classroom (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    room_no VARCHAR(50) NOT NULL COMMENT '教室编号',
    room_name VARCHAR(100) NOT NULL COMMENT '教室名称',
    campus_id BIGINT COMMENT '所属校区ID',
    building VARCHAR(50) COMMENT '所在楼栋',
    floor INT COMMENT '楼层',
    capacity INT DEFAULT 0 COMMENT '容纳人数',
    room_type VARCHAR(20) DEFAULT 'GENERAL' COMMENT '教室类型：GENERAL-普通，MULTIMEDIA-多媒体，LAB-实验室，COMPUTER-计算机房，LECTURE_HALL-阶梯教室',
    has_projector INT DEFAULT 0 COMMENT '是否有投影：0-无，1-有',
    has_microphone INT DEFAULT 0 COMMENT '是否有麦克风：0-无，1-有',
    has_air_conditioner INT DEFAULT 0 COMMENT '是否有空调：0-无，1-有',
    equipment_desc VARCHAR(500) COMMENT '设备描述',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_room_no (room_no),
    KEY idx_campus_id (campus_id),
    KEY idx_room_type (room_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教室表';

-- ----------------------------
-- 8. 课程表
-- ----------------------------
DROP TABLE IF EXISTS edu_course;
CREATE TABLE edu_course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    course_code VARCHAR(50) NOT NULL COMMENT '课程编码',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_type VARCHAR(20) DEFAULT 'REQUIRED' COMMENT '课程类型：REQUIRED-必修，ELECTIVE-选修，PRACTICE-实践，EXPERIMENT-实验',
    credit DECIMAL(3,1) DEFAULT 1.0 COMMENT '学分',
    theory_hours INT DEFAULT 0 COMMENT '理论学时',
    practice_hours INT DEFAULT 0 COMMENT '实践学时',
    total_hours INT DEFAULT 0 COMMENT '总学时',
    dept_id BIGINT COMMENT '开课院系ID',
    priority INT DEFAULT 5 COMMENT '排课优先级：1-10，数字越小优先级越高',
    need_multimedia INT DEFAULT 0 COMMENT '是否需要多媒体：0-否，1-是',
    need_lab INT DEFAULT 0 COMMENT '是否需要实验室：0-否，1-是',
    min_capacity INT COMMENT '最小容量',
    max_capacity INT COMMENT '最大容量',
    course_desc VARCHAR(500) COMMENT '课程描述',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_course_code (course_code),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- ----------------------------
-- 9. 教学任务表
-- ----------------------------
DROP TABLE IF EXISTS edu_teaching_task;
CREATE TABLE edu_teaching_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    semester VARCHAR(20) NOT NULL COMMENT '学期，格式：2024-1',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    teacher_id BIGINT COMMENT '教师ID',
    class_id BIGINT COMMENT '班级ID',
    student_count INT DEFAULT 0 COMMENT '学生人数',
    weekly_hours INT DEFAULT 2 COMMENT '周学时',
    total_weeks INT DEFAULT 16 COMMENT '总周数',
    weeks VARCHAR(100) COMMENT '上课周次，如：1-16',
    course_nature VARCHAR(20) DEFAULT 'REQUIRED' COMMENT '课程性质',
    priority_level INT DEFAULT 5 COMMENT '优先级：1-10',
    fixed_day INT COMMENT '固定星期：1-7',
    fixed_slot INT COMMENT '固定节次：1-12',
    fixed_classroom BIGINT COMMENT '固定教室ID',
    time_preference VARCHAR(100) COMMENT '时间偏好',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待排课，SCHEDULED-已排课，ADJUSTING-调整中，COMPLETED-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_semester (semester),
    KEY idx_course_id (course_id),
    KEY idx_teacher_id (teacher_id),
    KEY idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学任务表';

-- ----------------------------
-- 10. 课表主表
-- ----------------------------
DROP TABLE IF EXISTS sch_timetable;
CREATE TABLE sch_timetable (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    semester VARCHAR(20) NOT NULL COMMENT '学期',
    version INT DEFAULT 1 COMMENT '版本号',
    name VARCHAR(100) COMMENT '课表名称',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档',
    generate_type VARCHAR(20) COMMENT '生成方式：GREEDY-贪心算法，GENETIC-遗传算法',
    algorithm_config VARCHAR(500) COMMENT '算法配置JSON',
    task_count INT DEFAULT 0 COMMENT '任务总数',
    scheduled_count INT DEFAULT 0 COMMENT '已排课数量',
    conflict_count INT DEFAULT 0 COMMENT '冲突数量',
    utilization_rate DECIMAL(5,2) COMMENT '教室利用率',
    satisfaction_score DECIMAL(5,2) COMMENT '满意度评分',
    generate_time DATETIME COMMENT '生成时间',
    publish_time DATETIME COMMENT '发布时间',
    publisher_id BIGINT COMMENT '发布人ID',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_semester (semester),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课表主表';

-- ----------------------------
-- 11. 课表明细表
-- ----------------------------
DROP TABLE IF EXISTS sch_timetable_detail;
CREATE TABLE sch_timetable_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    timetable_id BIGINT NOT NULL COMMENT '课表ID',
    task_id BIGINT COMMENT '教学任务ID',
    course_id BIGINT COMMENT '课程ID',
    course_name VARCHAR(100) COMMENT '课程名称',
    teacher_id BIGINT COMMENT '教师ID',
    teacher_name VARCHAR(50) COMMENT '教师姓名',
    class_id BIGINT COMMENT '班级ID',
    class_name VARCHAR(100) COMMENT '班级名称',
    classroom_id BIGINT COMMENT '教室ID',
    classroom_name VARCHAR(100) COMMENT '教室名称',
    day_of_week INT NOT NULL COMMENT '星期：1-7',
    slot_no INT NOT NULL COMMENT '节次：1-12',
    weeks VARCHAR(100) COMMENT '上课周次',
    is_conflict INT DEFAULT 0 COMMENT '是否有冲突：0-否，1-是',
    conflict_info VARCHAR(500) COMMENT '冲突信息',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_timetable_id (timetable_id),
    KEY idx_task_id (task_id),
    KEY idx_time (day_of_week, slot_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课表明细表';

-- ----------------------------
-- 12. 数据同步配置表
-- ----------------------------
DROP TABLE IF EXISTS sys_sync_config;
CREATE TABLE sys_sync_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    system_name VARCHAR(100) NOT NULL COMMENT '系统名称',
    system_code VARCHAR(50) NOT NULL COMMENT '系统编码',
    system_type VARCHAR(20) COMMENT '系统类型：ZF-正方，QG-青果',
    api_url VARCHAR(255) COMMENT 'API地址',
    auth_type VARCHAR(20) COMMENT '认证类型',
    auth_config VARCHAR(500) COMMENT '认证配置JSON',
    field_mapping VARCHAR(1000) COMMENT '字段映射JSON',
    sync_interval INT DEFAULT 60 COMMENT '同步间隔(分钟)',
    last_sync_time DATETIME COMMENT '最后同步时间',
    last_sync_status INT COMMENT '最后同步状态：0-失败，1-成功',
    last_sync_msg VARCHAR(500) COMMENT '最后同步消息',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_system_code (system_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据同步配置表';
