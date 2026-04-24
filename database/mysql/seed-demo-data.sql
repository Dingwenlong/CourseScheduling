-- ========================================================
-- 智能排课系统演示数据脚本
-- 说明:
-- 1. 仅清理并重建 "[演示脚本]" 前缀的课表及 DEMO-* 申请单
-- 2. 依赖 init-schema.sql 中的基础数据
-- 3. 会按当前日期自动推导前端首页使用的当前学期
-- ========================================================

SET NAMES utf8mb4;

SET @demo_prefix = '[演示脚本] ';
SET @demo_app_prefix = 'DEMO-';
SET @current_year = YEAR(CURDATE());
SET @current_month = MONTH(CURDATE());
SET @current_semester = IF(
    @current_month < 8,
    CONCAT(@current_year - 1, '-', @current_year, '-2'),
    CONCAT(@current_year, '-', @current_year + 1, '-1')
);

-- ----------------------------
-- 清理旧的演示数据
-- ----------------------------
DELETE FROM adj_swap_application
WHERE application_no LIKE CONCAT(@demo_app_prefix, '%');

DELETE FROM adj_application
WHERE application_no LIKE CONCAT(@demo_app_prefix, '%');

DELETE detail
FROM sch_timetable_detail detail
INNER JOIN sch_timetable timetable ON timetable.id = detail.timetable_id
WHERE timetable.name LIKE CONCAT(@demo_prefix, '%');

DELETE FROM sch_timetable
WHERE name LIKE CONCAT(@demo_prefix, '%');

-- ----------------------------
-- 创建演示课表
-- ----------------------------
INSERT INTO sch_timetable (
    semester,
    version,
    name,
    status,
    generate_type,
    algorithm_config,
    task_count,
    scheduled_count,
    conflict_count,
    utilization_rate,
    satisfaction_score,
    generate_time,
    publish_time,
    publisher_id,
    remark
)
SELECT
    @current_semester,
    timetable_template.version,
    CONCAT(@demo_prefix, @current_semester, ' ', timetable_template.suffix_name),
    timetable_template.status,
    timetable_template.generate_type,
    timetable_template.algorithm_config,
    0,
    0,
    0,
    0,
    0,
    DATE_SUB(NOW(), INTERVAL timetable_template.generate_hours_ago HOUR),
    CASE
        WHEN timetable_template.status = 'PUBLISHED'
            THEN DATE_SUB(NOW(), INTERVAL timetable_template.publish_hours_ago HOUR)
        WHEN timetable_template.status = 'ARCHIVED'
            THEN DATE_SUB(NOW(), INTERVAL timetable_template.publish_hours_ago HOUR)
        ELSE NULL
    END,
    admin_user.id,
    timetable_template.remark
FROM (
    SELECT
        900 AS version,
        '历史归档课表' AS suffix_name,
        'ARCHIVED' AS status,
        'AUTO' AS generate_type,
        '{"algorithmType":"GREEDY","daysPerWeek":5,"slotsPerDay":10,"seed":"demo-script"}' AS algorithm_config,
        240 AS generate_hours_ago,
        228 AS publish_hours_ago,
        '用于演示历史版本查看。' AS remark
    UNION ALL
    SELECT
        901,
        '预演草稿课表',
        'DRAFT',
        'AUTO',
        '{"algorithmType":"GENETIC","daysPerWeek":5,"slotsPerDay":10,"maxGenerations":300,"seed":"demo-script"}',
        72,
        0,
        '用于演示未发布的排课版本。'
    UNION ALL
    SELECT
        902,
        '发布课表',
        'PUBLISHED',
        'AUTO',
        '{"algorithmType":"GREEDY","daysPerWeek":5,"slotsPerDay":10,"seed":"demo-script"}',
        24,
        23,
        '用于首页、课表、统计和调课演示。'
) AS timetable_template
INNER JOIN sys_user admin_user ON admin_user.username = 'admin';

-- ----------------------------
-- 演示课表明细模板
-- 所有 slot_no 使用奇数，和前端 2 节连排的显示逻辑一致
-- ----------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_demo_schedule;
CREATE TEMPORARY TABLE tmp_demo_schedule (
    sort_order INT NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    teacher_no VARCHAR(20) NOT NULL,
    class_code VARCHAR(20) NOT NULL,
    room_no VARCHAR(20) NOT NULL,
    day_of_week TINYINT NOT NULL,
    slot_no TINYINT NOT NULL,
    weeks VARCHAR(50) NOT NULL,
    is_conflict TINYINT NOT NULL DEFAULT 0,
    conflict_info VARCHAR(500) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO tmp_demo_schedule (
    sort_order,
    course_code,
    teacher_no,
    class_code,
    room_no,
    day_of_week,
    slot_no,
    weeks,
    is_conflict,
    conflict_info
) VALUES
    (10, 'CS101', 'T001', 'CS202101', 'A101', 1, 1, '1-16', 1, '教师冲突:人工智能导论;教室冲突:电路分析;班级冲突:高等数学A(上)'),
    (11, 'CS301', 'T001', 'CS202101', 'A201', 1, 1, '1-16', 1, '教师冲突:程序设计基础;班级冲突:程序设计基础'),
    (12, 'MATH101', 'T004', 'CS202101', 'A202', 1, 1, '1-16', 1, '班级冲突:程序设计基础'),
    (13, 'EE101', 'T003', 'EE202101', 'A101', 1, 1, '1-16', 1, '教室冲突:程序设计基础'),
    (20, 'CS102', 'T002', 'CS202101', 'A201', 1, 3, '1-16', 0, NULL),
    (21, 'PHYS101', 'T005', 'MATH202101', 'B101', 1, 9, '1-16', 0, NULL),
    (30, 'CS101', 'T001', 'CS202102', 'A102', 2, 1, '1-16', 0, NULL),
    (31, 'CS102', 'T002', 'CS202102', 'A201', 2, 3, '1-16', 0, NULL),
    (32, 'EE101', 'T003', 'EE202201', 'B102', 2, 5, '1-16', 0, NULL),
    (33, 'MATH101', 'T004', 'CS202102', 'A202', 2, 7, '1-16', 0, NULL),
    (40, 'CS201', 'T001', 'CS202201', 'B201', 3, 3, '1-16', 0, NULL),
    (41, 'CS202', 'T002', 'CS202201', 'A201', 3, 5, '1-16', 0, NULL),
    (42, 'MATH101', 'T004', 'CS202201', 'A202', 3, 7, '1-16', 0, NULL),
    (50, 'EE102', 'T003', 'EE202101', 'B101', 4, 1, '1-16', 0, NULL),
    (51, 'PHYS101', 'T005', 'CS202101', 'C102', 4, 1, '1-16', 0, NULL),
    (52, 'CS302', 'T002', 'CS202102', 'A201', 4, 3, '1-16', 0, NULL),
    (53, 'MATH201', 'T004', 'CS202101', 'A301', 4, 5, '1-16', 0, NULL),
    (60, 'PHYS101', 'T005', 'EE202101', 'C201', 5, 5, '1-16', 0, NULL),
    (61, 'MATH201', 'T004', 'EE202101', 'C101', 5, 9, '1-16', 0, NULL);

INSERT INTO sch_timetable_detail (
    timetable_id,
    task_id,
    course_id,
    course_name,
    teacher_id,
    teacher_name,
    class_id,
    class_name,
    classroom_id,
    classroom_name,
    day_of_week,
    slot_no,
    weeks,
    is_conflict,
    conflict_info,
    status
)
SELECT
    publish_timetable.id,
    teaching_task.id,
    course.id,
    course.course_name,
    teacher.id,
    teacher_user.real_name,
    class_info.id,
    class_info.class_name,
    classroom.id,
    COALESCE(classroom.room_name, classroom.room_no),
    demo_schedule.day_of_week,
    demo_schedule.slot_no,
    demo_schedule.weeks,
    demo_schedule.is_conflict,
    demo_schedule.conflict_info,
    1
FROM tmp_demo_schedule demo_schedule
INNER JOIN edu_course course
    ON course.course_code = demo_schedule.course_code
INNER JOIN edu_teacher teacher
    ON teacher.teacher_no = demo_schedule.teacher_no
INNER JOIN sys_user teacher_user
    ON teacher_user.id = teacher.user_id
INNER JOIN edu_class class_info
    ON class_info.class_code = demo_schedule.class_code
INNER JOIN edu_classroom classroom
    ON classroom.room_no = demo_schedule.room_no
INNER JOIN (
    SELECT
        MIN(id) AS id,
        semester,
        course_id,
        teacher_id,
        class_id
    FROM edu_teaching_task
    GROUP BY semester, course_id, teacher_id, class_id
) teaching_task_ref
    ON teaching_task_ref.semester = @current_semester
    AND teaching_task_ref.course_id = course.id
    AND teaching_task_ref.teacher_id = teacher.id
    AND teaching_task_ref.class_id = class_info.id
INNER JOIN edu_teaching_task teaching_task
    ON teaching_task.id = teaching_task_ref.id
INNER JOIN sch_timetable publish_timetable
    ON publish_timetable.name = CONCAT(@demo_prefix, @current_semester, ' 发布课表')
ORDER BY demo_schedule.sort_order;

INSERT INTO sch_timetable_detail (
    timetable_id,
    task_id,
    course_id,
    course_name,
    teacher_id,
    teacher_name,
    class_id,
    class_name,
    classroom_id,
    classroom_name,
    day_of_week,
    slot_no,
    weeks,
    is_conflict,
    conflict_info,
    status
)
SELECT
    draft_timetable.id,
    publish_detail.task_id,
    publish_detail.course_id,
    publish_detail.course_name,
    publish_detail.teacher_id,
    publish_detail.teacher_name,
    publish_detail.class_id,
    publish_detail.class_name,
    publish_detail.classroom_id,
    publish_detail.classroom_name,
    publish_detail.day_of_week,
    publish_detail.slot_no,
    publish_detail.weeks,
    publish_detail.is_conflict,
    publish_detail.conflict_info,
    publish_detail.status
FROM sch_timetable_detail publish_detail
INNER JOIN sch_timetable publish_timetable
    ON publish_timetable.id = publish_detail.timetable_id
INNER JOIN sch_timetable draft_timetable
    ON draft_timetable.name = CONCAT(@demo_prefix, @current_semester, ' 预演草稿课表')
WHERE publish_timetable.name = CONCAT(@demo_prefix, @current_semester, ' 发布课表')
ORDER BY publish_detail.id;

INSERT INTO sch_timetable_detail (
    timetable_id,
    task_id,
    course_id,
    course_name,
    teacher_id,
    teacher_name,
    class_id,
    class_name,
    classroom_id,
    classroom_name,
    day_of_week,
    slot_no,
    weeks,
    is_conflict,
    conflict_info,
    status
)
SELECT
    archived_timetable.id,
    publish_detail.task_id,
    publish_detail.course_id,
    publish_detail.course_name,
    publish_detail.teacher_id,
    publish_detail.teacher_name,
    publish_detail.class_id,
    publish_detail.class_name,
    publish_detail.classroom_id,
    publish_detail.classroom_name,
    publish_detail.day_of_week,
    publish_detail.slot_no,
    publish_detail.weeks,
    publish_detail.is_conflict,
    publish_detail.conflict_info,
    publish_detail.status
FROM sch_timetable_detail publish_detail
INNER JOIN sch_timetable publish_timetable
    ON publish_timetable.id = publish_detail.timetable_id
INNER JOIN sch_timetable archived_timetable
    ON archived_timetable.name = CONCAT(@demo_prefix, @current_semester, ' 历史归档课表')
WHERE publish_timetable.name = CONCAT(@demo_prefix, @current_semester, ' 发布课表')
ORDER BY publish_detail.id;

-- ----------------------------
-- 更新演示课表统计值
-- ----------------------------
SET @active_classroom_count = (
    SELECT COUNT(*)
    FROM edu_classroom
    WHERE status = 1
);

UPDATE sch_timetable timetable
SET
    timetable.task_count = (
        SELECT COUNT(*)
        FROM sch_timetable_detail detail
        WHERE detail.timetable_id = timetable.id
    ),
    timetable.scheduled_count = (
        SELECT COUNT(*)
        FROM sch_timetable_detail detail
        WHERE detail.timetable_id = timetable.id
            AND detail.status = 1
    ),
    timetable.conflict_count = (
        SELECT COUNT(*)
        FROM sch_timetable_detail detail
        WHERE detail.timetable_id = timetable.id
            AND detail.is_conflict = 1
    ),
    timetable.utilization_rate = ROUND(
        62 + (
            SELECT COUNT(*)
            FROM sch_timetable_detail detail
            WHERE detail.timetable_id = timetable.id
                AND detail.status = 1
        ) * 0.6 - (
            SELECT COUNT(*)
            FROM sch_timetable_detail detail
            WHERE detail.timetable_id = timetable.id
                AND detail.is_conflict = 1
        ) * 2,
        2
    ),
    timetable.satisfaction_score = ROUND(
        88 - (
            SELECT COUNT(*)
            FROM sch_timetable_detail detail
            WHERE detail.timetable_id = timetable.id
                AND detail.is_conflict = 1
        ) * 3.5,
        2
    )
WHERE timetable.name LIKE CONCAT(@demo_prefix, '%');

-- ----------------------------
-- 更新教学任务状态，形成更适合演示的状态分布
-- ----------------------------
UPDATE edu_teaching_task task
INNER JOIN sch_timetable_detail detail ON detail.task_id = task.id
INNER JOIN sch_timetable timetable ON timetable.id = detail.timetable_id
SET task.status = 'SCHEDULED'
WHERE timetable.name = CONCAT(@demo_prefix, @current_semester, ' 发布课表')
    AND task.semester = @current_semester;

UPDATE edu_teaching_task task
INNER JOIN edu_course course ON course.id = task.course_id
INNER JOIN edu_teacher teacher ON teacher.id = task.teacher_id
INNER JOIN edu_class class_info ON class_info.id = task.class_id
SET task.status = 'ADJUSTING'
WHERE task.semester = @current_semester
    AND (
        (course.course_code = 'CS101' AND teacher.teacher_no = 'T001' AND class_info.class_code = 'CS202102')
        OR (course.course_code = 'CS201' AND teacher.teacher_no = 'T001' AND class_info.class_code = 'CS202201')
        OR (course.course_code = 'PHYS101' AND teacher.teacher_no = 'T005' AND class_info.class_code = 'CS202101')
    );

UPDATE edu_teaching_task task
INNER JOIN edu_course course ON course.id = task.course_id
INNER JOIN edu_teacher teacher ON teacher.id = task.teacher_id
INNER JOIN edu_class class_info ON class_info.id = task.class_id
SET task.status = 'COMPLETED'
WHERE task.semester = @current_semester
    AND course.course_code = 'PHYS101'
    AND teacher.teacher_no = 'T005'
    AND class_info.class_code = 'EE202101';

-- ----------------------------
-- 插入演示调课申请
-- teacher_id 字段按当前后端实现保存的是 sys_user.id
-- ----------------------------
INSERT INTO adj_application (
    application_no,
    semester,
    detail_id,
    teacher_id,
    old_day,
    old_slot,
    old_classroom,
    new_day,
    new_slot,
    new_classroom,
    reason,
    status,
    apply_time,
    auditor_id,
    audit_time,
    audit_remark
)
SELECT
    'DEMO-ADJ-PENDING-001',
    @current_semester,
    detail.id,
    teacher_user.id,
    detail.day_of_week,
    detail.slot_no,
    detail.classroom_id,
    5,
    7,
    detail.classroom_id,
    '演示用途：教师临时参加学院活动，申请顺延到周五晚上。',
    'PENDING',
    DATE_SUB(NOW(), INTERVAL 6 HOUR),
    NULL,
    NULL,
    NULL
FROM sch_timetable_detail detail
INNER JOIN sch_timetable timetable
    ON timetable.id = detail.timetable_id
INNER JOIN edu_teaching_task task
    ON task.id = detail.task_id
INNER JOIN edu_teacher teacher
    ON teacher.id = task.teacher_id
INNER JOIN sys_user teacher_user
    ON teacher_user.id = teacher.user_id
INNER JOIN edu_course course
    ON course.id = task.course_id
INNER JOIN edu_class class_info
    ON class_info.id = task.class_id
WHERE timetable.name = CONCAT(@demo_prefix, @current_semester, ' 发布课表')
    AND course.course_code = 'PHYS101'
    AND teacher.teacher_no = 'T005'
    AND class_info.class_code = 'CS202101';

INSERT INTO adj_application (
    application_no,
    semester,
    detail_id,
    teacher_id,
    old_day,
    old_slot,
    old_classroom,
    new_day,
    new_slot,
    new_classroom,
    reason,
    status,
    apply_time,
    auditor_id,
    audit_time,
    audit_remark
)
SELECT
    'DEMO-ADJ-DONE-001',
    @current_semester,
    detail.id,
    teacher_user.id,
    5,
    1,
    detail.classroom_id,
    detail.day_of_week,
    detail.slot_no,
    detail.classroom_id,
    '演示用途：实验室巡检结束后，课程已顺利调整到新的时段。',
    'APPROVED',
    DATE_SUB(NOW(), INTERVAL 48 HOUR),
    admin_user.id,
    DATE_SUB(NOW(), INTERVAL 46 HOUR),
    '演示脚本预置的已完成调课记录'
FROM sch_timetable_detail detail
INNER JOIN sch_timetable timetable
    ON timetable.id = detail.timetable_id
INNER JOIN edu_teaching_task task
    ON task.id = detail.task_id
INNER JOIN edu_teacher teacher
    ON teacher.id = task.teacher_id
INNER JOIN sys_user teacher_user
    ON teacher_user.id = teacher.user_id
INNER JOIN sys_user admin_user
    ON admin_user.username = 'admin'
INNER JOIN edu_course course
    ON course.id = task.course_id
INNER JOIN edu_class class_info
    ON class_info.id = task.class_id
WHERE timetable.name = CONCAT(@demo_prefix, @current_semester, ' 发布课表')
    AND course.course_code = 'PHYS101'
    AND teacher.teacher_no = 'T005'
    AND class_info.class_code = 'EE202101';

-- ----------------------------
-- 插入演示换课申请
-- ----------------------------
INSERT INTO adj_swap_application (
    application_no,
    semester,
    timetable_id,
    detail_id1,
    detail_id2,
    teacher_id,
    old_day1,
    old_slot1,
    old_classroom1,
    old_day2,
    old_slot2,
    old_classroom2,
    reason,
    status,
    apply_time,
    auditor_id,
    audit_time,
    audit_remark
)
SELECT
    'DEMO-SWP-PENDING-001',
    @current_semester,
    timetable.id,
    detail_one.id,
    detail_two.id,
    teacher_user.id,
    detail_one.day_of_week,
    detail_one.slot_no,
    detail_one.classroom_id,
    detail_two.day_of_week,
    detail_two.slot_no,
    detail_two.classroom_id,
    '演示用途：同一位教师希望互换两门课的授课时间，便于展示换课流程。',
    'PENDING',
    DATE_SUB(NOW(), INTERVAL 5 HOUR),
    NULL,
    NULL,
    NULL
FROM sch_timetable timetable
INNER JOIN sch_timetable_detail detail_one
    ON detail_one.timetable_id = timetable.id
INNER JOIN sch_timetable_detail detail_two
    ON detail_two.timetable_id = timetable.id
    AND detail_two.id > detail_one.id
INNER JOIN edu_teaching_task task_one
    ON task_one.id = detail_one.task_id
INNER JOIN edu_teaching_task task_two
    ON task_two.id = detail_two.task_id
INNER JOIN edu_teacher teacher
    ON teacher.id = task_one.teacher_id
    AND teacher.id = task_two.teacher_id
INNER JOIN sys_user teacher_user
    ON teacher_user.id = teacher.user_id
INNER JOIN edu_course course_one
    ON course_one.id = task_one.course_id
INNER JOIN edu_course course_two
    ON course_two.id = task_two.course_id
INNER JOIN edu_class class_one
    ON class_one.id = task_one.class_id
INNER JOIN edu_class class_two
    ON class_two.id = task_two.class_id
WHERE timetable.name = CONCAT(@demo_prefix, @current_semester, ' 发布课表')
    AND teacher.teacher_no = 'T001'
    AND course_one.course_code = 'CS101'
    AND class_one.class_code = 'CS202102'
    AND course_two.course_code = 'CS201'
    AND class_two.class_code = 'CS202201';

DROP TEMPORARY TABLE IF EXISTS tmp_demo_schedule;

-- ----------------------------
-- 输出摘要，方便脚本执行后直接确认
-- ----------------------------
SELECT
    @current_semester AS current_semester,
    (
        SELECT COUNT(*)
        FROM sch_timetable
        WHERE name LIKE CONCAT(@demo_prefix, '%')
    ) AS demo_timetable_count,
    (
        SELECT COUNT(*)
        FROM sch_timetable_detail detail
        INNER JOIN sch_timetable timetable ON timetable.id = detail.timetable_id
        WHERE timetable.name LIKE CONCAT(@demo_prefix, '%')
    ) AS demo_detail_count,
    (
        SELECT COUNT(*)
        FROM adj_application
        WHERE application_no LIKE CONCAT(@demo_app_prefix, '%')
    ) AS demo_adjustment_count,
    (
        SELECT COUNT(*)
        FROM adj_swap_application
        WHERE application_no LIKE CONCAT(@demo_app_prefix, '%')
    ) AS demo_swap_count;

SELECT
    timetable.name,
    timetable.status,
    timetable.task_count,
    timetable.scheduled_count,
    timetable.conflict_count,
    timetable.utilization_rate,
    timetable.satisfaction_score
FROM sch_timetable timetable
WHERE timetable.name LIKE CONCAT(@demo_prefix, '%')
ORDER BY timetable.version;
