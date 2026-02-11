-- ========================================================
-- 高校智能排课系统数据库初始化脚本
-- 数据库: course_scheduling
-- 字符集: utf8mb4
-- ========================================================

CREATE DATABASE IF NOT EXISTS course_scheduling 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE course_scheduling;

-- ========================================================
-- 1. 系统管理相关表
-- ========================================================

-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL COMMENT '角色:ADMIN-管理员,TEACHER-教师,STUDENT-学生',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '电子邮箱',
    avatar VARCHAR(200) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 操作日志表
CREATE TABLE sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(100) COMMENT '操作类型',
    module VARCHAR(50) COMMENT '操作模块',
    description VARCHAR(500) COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(200) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_data TEXT COMMENT '响应数据',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    execution_time INT COMMENT '执行时长(ms)',
    status TINYINT COMMENT '状态:0-失败,1-成功',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_module (module),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ========================================================
-- 2. 基础数据相关表
-- ========================================================

-- 校区表
CREATE TABLE sys_campus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    campus_code VARCHAR(20) NOT NULL COMMENT '校区代码',
    campus_name VARCHAR(100) NOT NULL COMMENT '校区名称',
    address VARCHAR(200) COMMENT '校区地址',
    commute_time INT DEFAULT 30 COMMENT '通勤时间(分钟)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_campus_code (campus_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校区表';

-- 院系表
CREATE TABLE sys_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dept_code VARCHAR(20) NOT NULL COMMENT '院系代码',
    dept_name VARCHAR(100) NOT NULL COMMENT '院系名称',
    campus_id BIGINT NOT NULL COMMENT '所属校区ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID,0表示顶级',
    leader_name VARCHAR(50) COMMENT '负责人姓名',
    leader_phone VARCHAR(20) COMMENT '负责人电话',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_dept_code (dept_code),
    INDEX idx_campus_id (campus_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院系表';

-- 班级表
CREATE TABLE edu_class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    class_code VARCHAR(20) NOT NULL COMMENT '班级代码',
    class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
    dept_id BIGINT NOT NULL COMMENT '所属院系ID',
    grade VARCHAR(10) COMMENT '年级,如:2024',
    student_count INT DEFAULT 0 COMMENT '学生人数',
    counselor_name VARCHAR(50) COMMENT '辅导员姓名',
    counselor_phone VARCHAR(20) COMMENT '辅导员电话',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_class_code (class_code),
    INDEX idx_dept_id (dept_id),
    INDEX idx_grade (grade),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 教师表
CREATE TABLE edu_teacher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    teacher_no VARCHAR(20) NOT NULL COMMENT '教师工号',
    dept_id BIGINT NOT NULL COMMENT '所属院系ID',
    title VARCHAR(20) COMMENT '职称:PROFESSOR-教授,ASSOCIATE_PROFESSOR-副教授,LECTURER-讲师,ASSISTANT-助教',
    research_area VARCHAR(200) COMMENT '研究方向',
    office_location VARCHAR(100) COMMENT '办公地点',
    office_phone VARCHAR(20) COMMENT '办公电话',
    max_hours_per_week INT DEFAULT 16 COMMENT '每周最大课时',
    campus_id BIGINT COMMENT '常驻校区ID',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_teacher_no (teacher_no),
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_dept_id (dept_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 学生表
CREATE TABLE edu_student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    student_no VARCHAR(20) NOT NULL COMMENT '学号',
    dept_id BIGINT NOT NULL COMMENT '所属院系ID',
    class_id BIGINT NOT NULL COMMENT '所属班级ID',
    grade VARCHAR(10) COMMENT '年级,如:2024',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_no (student_no),
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_dept_id (dept_id),
    INDEX idx_class_id (class_id),
    INDEX idx_grade (grade),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- ========================================================
-- 3. 教学资源相关表
-- ========================================================

-- 教室表
CREATE TABLE edu_classroom (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    room_no VARCHAR(20) NOT NULL COMMENT '教室编号',
    room_name VARCHAR(100) COMMENT '教室名称',
    campus_id BIGINT NOT NULL COMMENT '所属校区ID',
    building VARCHAR(50) COMMENT '教学楼',
    floor INT COMMENT '楼层',
    capacity INT NOT NULL COMMENT '容量(人数)',
    room_type ENUM('GENERAL', 'MULTIMEDIA', 'LAB', 'COMPUTER', 'LECTURE_HALL') 
        DEFAULT 'GENERAL' COMMENT '教室类型',
    has_projector TINYINT DEFAULT 0 COMMENT '是否有投影仪:0-否,1-是',
    has_microphone TINYINT DEFAULT 0 COMMENT '是否有麦克风:0-否,1-是',
    has_air_conditioner TINYINT DEFAULT 0 COMMENT '是否有空调:0-否,1-是',
    equipment_desc TEXT COMMENT '设备描述',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用,2-维修中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_room_no (room_no),
    INDEX idx_campus_id (campus_id),
    INDEX idx_room_type (room_type),
    INDEX idx_capacity (capacity),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教室表';

-- 课程表
CREATE TABLE edu_course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    course_code VARCHAR(20) NOT NULL COMMENT '课程代码',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_type ENUM('REQUIRED', 'ELECTIVE', 'PRACTICE', 'EXPERIMENT') 
        NOT NULL COMMENT '课程类型:REQUIRED-必修课,ELECTIVE-选修课,PRACTICE-实践课,EXPERIMENT-实验课',
    credit DECIMAL(3,1) NOT NULL COMMENT '学分',
    theory_hours INT DEFAULT 0 COMMENT '理论课时',
    practice_hours INT DEFAULT 0 COMMENT '实践课时',
    total_hours INT DEFAULT 0 COMMENT '总课时',
    dept_id BIGINT NOT NULL COMMENT '开课院系ID',
    priority INT DEFAULT 5 COMMENT '优先级(1-10,数值越大优先级越高)',
    need_multimedia TINYINT DEFAULT 0 COMMENT '是否需要多媒体:0-否,1-是',
    need_lab TINYINT DEFAULT 0 COMMENT '是否需要实验室:0-否,1-是',
    min_capacity INT DEFAULT 0 COMMENT '最小容量',
    max_capacity INT DEFAULT 999 COMMENT '最大容量',
    course_desc TEXT COMMENT '课程描述',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_course_code (course_code),
    INDEX idx_dept_id (dept_id),
    INDEX idx_course_type (course_type),
    INDEX idx_priority (priority),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- ========================================================
-- 4. 排课业务相关表
-- ========================================================

-- 教学任务表(开课计划)
CREATE TABLE edu_teaching_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    semester VARCHAR(20) NOT NULL COMMENT '学期,如:2024-2025-1',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    teacher_id BIGINT NOT NULL COMMENT '任课教师ID',
    class_id BIGINT NOT NULL COMMENT '上课班级ID',
    student_count INT NOT NULL COMMENT '学生人数',
    weekly_hours INT NOT NULL COMMENT '周学时',
    total_weeks INT DEFAULT 16 COMMENT '总周数',
    weeks VARCHAR(50) COMMENT '上课周次,如:1-16',
    course_nature ENUM('THEORY', 'PRACTICE', 'BOTH') 
        NOT NULL COMMENT '课程性质:THEORY-纯理论,PRACTICE-纯实践,BOTH-理论+实践',
    priority_level INT DEFAULT 5 COMMENT '优先级等级(1-10)',
    fixed_day TINYINT COMMENT '固定星期(1-7)',
    fixed_slot TINYINT COMMENT '固定节次(1-12)',
    fixed_classroom BIGINT COMMENT '固定教室ID',
    time_preference VARCHAR(100) COMMENT '时间偏好,JSON格式',
    status ENUM('PENDING', 'SCHEDULED', 'ADJUSTING', 'COMPLETED') 
        DEFAULT 'PENDING' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_semester (semester),
    INDEX idx_course_id (course_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_class_id (class_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学任务表';

-- 课表主表
CREATE TABLE sch_timetable (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    semester VARCHAR(20) NOT NULL COMMENT '学期',
    version INT DEFAULT 1 COMMENT '版本号',
    name VARCHAR(100) COMMENT '课表名称',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT' COMMENT '状态',
    generate_type ENUM('AUTO', 'MANUAL') DEFAULT 'AUTO' COMMENT '生成方式',
    algorithm_config TEXT COMMENT '算法配置,JSON格式',
    task_count INT DEFAULT 0 COMMENT '任务总数',
    scheduled_count INT DEFAULT 0 COMMENT '已排课程数',
    conflict_count INT DEFAULT 0 COMMENT '冲突数量',
    utilization_rate DECIMAL(5,2) COMMENT '教室利用率(%)',
    satisfaction_score DECIMAL(5,2) COMMENT '满意度评分',
    generate_time DATETIME COMMENT '生成时间',
    publish_time DATETIME COMMENT '发布时间',
    publisher_id BIGINT COMMENT '发布人ID',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_semester (semester),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课表主表';

-- 课表明细表
CREATE TABLE sch_timetable_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    timetable_id BIGINT NOT NULL COMMENT '课表ID',
    task_id BIGINT NOT NULL COMMENT '教学任务ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    teacher_name VARCHAR(50) COMMENT '教师姓名',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    class_name VARCHAR(100) COMMENT '班级名称',
    classroom_id BIGINT NOT NULL COMMENT '教室ID',
    classroom_name VARCHAR(100) COMMENT '教室名称',
    day_of_week TINYINT NOT NULL COMMENT '星期几(1-7)',
    slot_no TINYINT NOT NULL COMMENT '节次(1-12)',
    weeks VARCHAR(50) NOT NULL COMMENT '上课周次',
    is_conflict TINYINT DEFAULT 0 COMMENT '是否冲突:0-否,1-是',
    conflict_info VARCHAR(500) COMMENT '冲突信息',
    status TINYINT DEFAULT 1 COMMENT '状态:0-取消,1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_timetable_id (timetable_id),
    INDEX idx_task_id (task_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_class_id (class_id),
    INDEX idx_classroom_id (classroom_id),
    INDEX idx_day_slot (day_of_week, slot_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课表明细表';

-- ========================================================
-- 5. 约束条件相关表
-- ========================================================

-- 教师时间约束表
CREATE TABLE con_teacher_time (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    semester VARCHAR(20) NOT NULL COMMENT '学期',
    day_of_week TINYINT NOT NULL COMMENT '星期几(1-7)',
    slot_no TINYINT NOT NULL COMMENT '节次(1-12)',
    is_available TINYINT DEFAULT 1 COMMENT '是否可用:0-不可用,1-可用',
    constraint_type ENUM('FORBIDDEN', 'PREFERRED', 'UNPREFERRED') 
        DEFAULT 'FORBIDDEN' COMMENT '约束类型',
    reason VARCHAR(200) COMMENT '原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_teacher_time (teacher_id, semester, day_of_week, slot_no),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_semester (semester)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师时间约束表';

-- 教室时间约束表
CREATE TABLE con_classroom_time (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    classroom_id BIGINT NOT NULL COMMENT '教室ID',
    semester VARCHAR(20) NOT NULL COMMENT '学期',
    day_of_week TINYINT NOT NULL COMMENT '星期几(1-7)',
    slot_no TINYINT NOT NULL COMMENT '节次(1-12)',
    is_available TINYINT DEFAULT 1 COMMENT '是否可用:0-不可用,1-可用',
    reason VARCHAR(200) COMMENT '原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_classroom_time (classroom_id, semester, day_of_week, slot_no),
    INDEX idx_classroom_id (classroom_id),
    INDEX idx_semester (semester)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教室时间约束表';

-- ========================================================
-- 6. 调课相关表
-- ========================================================

-- 调课申请表
CREATE TABLE adj_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    application_no VARCHAR(30) NOT NULL COMMENT '申请编号',
    semester VARCHAR(20) NOT NULL COMMENT '学期',
    detail_id BIGINT NOT NULL COMMENT '原课表明细ID',
    teacher_id BIGINT NOT NULL COMMENT '申请教师ID',
    old_day TINYINT NOT NULL COMMENT '原星期',
    old_slot TINYINT NOT NULL COMMENT '原节次',
    old_classroom BIGINT NOT NULL COMMENT '原教室ID',
    new_day TINYINT COMMENT '新星期',
    new_slot TINYINT COMMENT '新节次',
    new_classroom BIGINT COMMENT '新教室ID',
    reason VARCHAR(500) NOT NULL COMMENT '调课原因',
    attachment_url VARCHAR(200) COMMENT '附件URL',
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') 
        DEFAULT 'PENDING' COMMENT '状态',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    auditor_id BIGINT COMMENT '审核人ID',
    audit_time DATETIME COMMENT '审核时间',
    audit_remark VARCHAR(500) COMMENT '审核备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_application_no (application_no),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_detail_id (detail_id),
    INDEX idx_status (status),
    INDEX idx_apply_time (apply_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调课申请表';

-- ========================================================
-- 7. 数据同步相关表
-- ========================================================

-- 数据同步配置表
CREATE TABLE sync_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    system_name VARCHAR(50) NOT NULL COMMENT '系统名称',
    system_code VARCHAR(20) NOT NULL COMMENT '系统代码',
    system_type ENUM('ZF', 'QG', 'CUSTOM') NOT NULL COMMENT '系统类型:ZF-正方,QG-青果,CUSTOM-自定义',
    api_url VARCHAR(200) COMMENT 'API地址',
    auth_type VARCHAR(20) COMMENT '认证类型:NONE,BASIC,TOKEN,OAUTH2',
    auth_config TEXT COMMENT '认证配置,JSON格式',
    field_mapping TEXT COMMENT '字段映射配置,JSON格式',
    sync_interval INT DEFAULT 3600 COMMENT '同步间隔(秒)',
    last_sync_time DATETIME COMMENT '上次同步时间',
    last_sync_status TINYINT COMMENT '上次同步状态:0-失败,1-成功',
    last_sync_msg VARCHAR(500) COMMENT '上次同步消息',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_system_code (system_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据同步配置表';

-- 数据同步日志表
CREATE TABLE sync_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_id BIGINT NOT NULL COMMENT '配置ID',
    sync_type VARCHAR(50) COMMENT '同步类型',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    total_count INT DEFAULT 0 COMMENT '总记录数',
    success_count INT DEFAULT 0 COMMENT '成功数',
    fail_count INT DEFAULT 0 COMMENT '失败数',
    status TINYINT COMMENT '状态:0-失败,1-成功,2-进行中',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_config_id (config_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据同步日志表';

-- ========================================================
-- 插入初始化数据
-- ========================================================

-- 初始化管理员账号(密码: admin123)
INSERT INTO sys_user (username, password, real_name, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '系统管理员', 'ADMIN', 1);

-- 初始化校区数据
INSERT INTO sys_campus (campus_code, campus_name, address, commute_time, sort_order) VALUES
('MAIN', '主校区', 'XX市XX区XX路1号', 0, 1),
('EAST', '东校区', 'XX市XX区XX路2号', 20, 2),
('WEST', '西校区', 'XX市XX区XX路3号', 25, 3);

-- 初始化院系数据
INSERT INTO sys_department (dept_code, dept_name, campus_id, parent_id, sort_order) VALUES
('CS', '计算机学院', 1, 0, 1),
('EE', '电子信息学院', 1, 0, 2),
('MATH', '数学学院', 1, 0, 3),
('ENG', '外国语学院', 1, 0, 4);

-- 初始化教室类型数据
INSERT INTO edu_classroom (room_no, room_name, campus_id, building, floor, capacity, room_type, has_projector, status) VALUES
('A101', 'A101多媒体教室', 1, 'A教学楼', 1, 120, 'MULTIMEDIA', 1, 1),
('A102', 'A102普通教室', 1, 'A教学楼', 1, 80, 'GENERAL', 0, 1),
('B201', 'B201计算机实验室', 1, 'B实验楼', 2, 60, 'COMPUTER', 1, 1),
('C301', 'C301阶梯教室', 1, 'C教学楼', 3, 200, 'LECTURE_HALL', 1, 1);
