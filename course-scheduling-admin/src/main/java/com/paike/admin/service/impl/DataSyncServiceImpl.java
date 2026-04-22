package com.paike.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paike.admin.dto.DataSyncApplyResponse;
import com.paike.admin.dto.DataSyncDatasetApplyResult;
import com.paike.admin.dto.DataSyncDatasetPreview;
import com.paike.admin.dto.DataSyncPreviewResponse;
import com.paike.admin.dto.DataSyncRequest;
import com.paike.admin.entity.Campus;
import com.paike.admin.entity.Clazz;
import com.paike.admin.entity.Classroom;
import com.paike.admin.entity.Course;
import com.paike.admin.entity.Department;
import com.paike.admin.mapper.CampusMapper;
import com.paike.admin.mapper.ClassMapper;
import com.paike.admin.mapper.ClassroomMapper;
import com.paike.admin.mapper.CourseMapper;
import com.paike.admin.mapper.DepartmentMapper;
import com.paike.admin.service.DataSyncService;
import com.paike.common.exception.BusinessException;
import com.paike.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DataSyncServiceImpl implements DataSyncService {

    private static final String DATASET_COURSES = "courses";
    private static final String DATASET_CLASSES = "classes";
    private static final String DATASET_CLASSROOMS = "classrooms";
    private static final List<String> SUPPORTED_DATASETS = List.of(
            DATASET_COURSES,
            DATASET_CLASSES,
            DATASET_CLASSROOMS
    );

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private ClassroomMapper classroomMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private CampusMapper campusMapper;

    @Override
    public DataSyncPreviewResponse preview(DataSyncRequest request) {
        NormalizedRequest normalized = normalizeRequest(request);
        List<DataSyncDatasetPreview> datasets = List.of(
                buildPreview(prepareCourses(normalized)),
                buildPreview(prepareClasses(normalized)),
                buildPreview(prepareClassrooms(normalized))
        );

        DataSyncPreviewResponse response = new DataSyncPreviewResponse();
        response.setProvider(normalized.provider());
        response.setDefaultDeptId(normalized.defaultDeptId());
        response.setDefaultCampusId(normalized.defaultCampusId());
        response.setPreviewTime(LocalDateTime.now());
        response.setDatasets(datasets);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataSyncApplyResponse apply(DataSyncRequest request) {
        NormalizedRequest normalized = normalizeRequest(request);
        List<DataSyncDatasetApplyResult> datasets = List.of(
                applyCourses(prepareCourses(normalized)),
                applyClasses(prepareClasses(normalized)),
                applyClassrooms(prepareClassrooms(normalized))
        );

        DataSyncApplyResponse response = new DataSyncApplyResponse();
        response.setProvider(normalized.provider());
        response.setSyncTime(LocalDateTime.now());
        response.setDatasets(datasets);
        response.setTotalCreated(datasets.stream().mapToInt(item -> safeInt(item.getCreatedCount())).sum());
        response.setTotalUpdated(datasets.stream().mapToInt(item -> safeInt(item.getUpdatedCount())).sum());
        response.setTotalSkipped(datasets.stream().mapToInt(item -> safeInt(item.getSkippedCount())).sum());
        return response;
    }

    private NormalizedRequest normalizeRequest(DataSyncRequest request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Map<String, List<Map<String, Object>>> payload = request.getPayload() == null
                ? Collections.emptyMap()
                : request.getPayload();
        boolean hasPayload = SUPPORTED_DATASETS.stream().anyMatch(dataset -> !rowsOf(payload, dataset).isEmpty());
        if (!hasPayload) {
            throw new BusinessException("请至少提供课程、班级或教室中的一类同步数据");
        }

        Map<String, Map<String, String>> mappings = request.getMappings() == null
                ? Collections.emptyMap()
                : request.getMappings();
        String provider = StringUtils.hasText(request.getProvider())
                ? request.getProvider().trim().toUpperCase(Locale.ROOT)
                : "CUSTOM";

        if (request.getDefaultDeptId() != null && !loadDepartments(Set.of(request.getDefaultDeptId())).containsKey(request.getDefaultDeptId())) {
            throw new BusinessException("默认院系不存在，请重新选择");
        }
        if (request.getDefaultCampusId() != null && !loadCampuses(Set.of(request.getDefaultCampusId())).containsKey(request.getDefaultCampusId())) {
            throw new BusinessException("默认校区不存在，请重新选择");
        }

        return new NormalizedRequest(provider, request.getDefaultDeptId(), request.getDefaultCampusId(), mappings, payload);
    }

    private PreparedDataset<Course> prepareCourses(NormalizedRequest request) {
        List<Map<String, Object>> rows = rowsOf(request.payload(), DATASET_COURSES);
        Map<String, String> mapping = mappingOf(request.mappings(), DATASET_COURSES);
        Map<String, Course> existing = loadCourses(extractCodes(rows, mapping, "code"));
        Map<Long, Department> departments = loadDepartments(collectReferencedIds(rows, mapping, "deptId", request.defaultDeptId()));
        List<PreparedRow<Course>> preparedRows = new ArrayList<>();

        for (int index = 0; index < rows.size(); index++) {
            int rowNo = index + 1;
            Map<String, Object> row = rows.get(index);
            List<String> warnings = new ArrayList<>();
            String code = readString(row, mapping, "code");
            String name = readString(row, mapping, "name");
            Long deptId = readLong(row, mapping, "deptId", warnings, "院系ID");
            if (deptId == null) {
                deptId = request.defaultDeptId();
                if (deptId != null) {
                    warnings.add("未提供院系ID，已使用默认院系");
                }
            }

            if (!StringUtils.hasText(code)) {
                warnings.add("课程编码不能为空");
            }
            if (!StringUtils.hasText(name)) {
                warnings.add("课程名称不能为空");
            }
            if (deptId == null) {
                warnings.add("缺少院系ID，且未配置默认院系");
            } else if (!departments.containsKey(deptId)) {
                warnings.add("院系ID不存在: " + deptId);
            }

            String courseType = normalizeCourseType(readString(row, mapping, "type"), warnings);
            BigDecimal credit = readDecimal(row, mapping, "credit", warnings, "学分");
            if (credit == null) {
                credit = BigDecimal.ZERO;
                warnings.add("未提供学分，已按 0 处理");
            }
            Integer theoryHours = readInteger(row, mapping, "theoryHours", warnings, "理论学时");
            Integer practiceHours = readInteger(row, mapping, "practiceHours", warnings, "实践学时");
            Integer totalHours = readInteger(row, mapping, "totalHours", warnings, "总学时");
            if (totalHours == null) {
                totalHours = safeInt(theoryHours) + safeInt(practiceHours);
            }

            Course entity = existing.get(code);
            boolean create = entity == null;
            if (create) {
                entity = new Course();
                entity.setCourseCode(code);
            }
            if (warnings.stream().noneMatch(this::isFatalWarning)) {
                entity.setCourseName(name);
                entity.setCourseType(courseType);
                entity.setCredit(credit);
                entity.setTheoryHours(defaultInteger(theoryHours));
                entity.setPracticeHours(defaultInteger(practiceHours));
                entity.setTotalHours(defaultInteger(totalHours));
                entity.setDeptId(deptId);
                entity.setPriority(defaultInteger(readInteger(row, mapping, "priority", warnings, "优先级"), 1));
                entity.setNeedMultimedia(defaultFlag(row, mapping, "needMultimedia", warnings, "多媒体需求"));
                entity.setNeedLab(defaultFlag(row, mapping, "needLab", warnings, "实验室需求"));
                entity.setMinCapacity(readInteger(row, mapping, "minCapacity", warnings, "最小容量"));
                entity.setMaxCapacity(readInteger(row, mapping, "maxCapacity", warnings, "最大容量"));
                entity.setCourseDesc(readString(row, mapping, "courseDesc"));
                entity.setStatus(defaultInteger(readInteger(row, mapping, "status", warnings, "状态"), 1));
            }

            preparedRows.add(new PreparedRow<>(
                    rowNo,
                    create ? "新增" : "更新",
                    warnings.stream().noneMatch(this::isFatalWarning),
                    entity,
                    warnings,
                    buildCourseSample(rowNo, code, name, deptId, courseType, create ? "新增" : "更新", warnings)
            ));
        }

        return new PreparedDataset<>(DATASET_COURSES, "课程信息", preparedRows);
    }

    private PreparedDataset<Clazz> prepareClasses(NormalizedRequest request) {
        List<Map<String, Object>> rows = rowsOf(request.payload(), DATASET_CLASSES);
        Map<String, String> mapping = mappingOf(request.mappings(), DATASET_CLASSES);
        Map<String, Clazz> existing = loadClasses(extractCodes(rows, mapping, "code"));
        Map<Long, Department> departments = loadDepartments(collectReferencedIds(rows, mapping, "deptId", request.defaultDeptId()));
        List<PreparedRow<Clazz>> preparedRows = new ArrayList<>();

        for (int index = 0; index < rows.size(); index++) {
            int rowNo = index + 1;
            Map<String, Object> row = rows.get(index);
            List<String> warnings = new ArrayList<>();
            String code = readString(row, mapping, "code");
            String name = readString(row, mapping, "name");
            Long deptId = readLong(row, mapping, "deptId", warnings, "院系ID");
            if (deptId == null) {
                deptId = request.defaultDeptId();
                if (deptId != null) {
                    warnings.add("未提供院系ID，已使用默认院系");
                }
            }

            if (!StringUtils.hasText(code)) {
                warnings.add("班级编码不能为空");
            }
            if (!StringUtils.hasText(name)) {
                warnings.add("班级名称不能为空");
            }
            if (deptId == null) {
                warnings.add("缺少院系ID，且未配置默认院系");
            } else if (!departments.containsKey(deptId)) {
                warnings.add("院系ID不存在: " + deptId);
            }

            Clazz entity = existing.get(code);
            boolean create = entity == null;
            if (create) {
                entity = new Clazz();
                entity.setClassCode(code);
            }
            if (warnings.stream().noneMatch(this::isFatalWarning)) {
                entity.setClassName(name);
                entity.setDeptId(deptId);
                entity.setGrade(readString(row, mapping, "grade"));
                entity.setStudentCount(defaultInteger(readInteger(row, mapping, "studentCount", warnings, "学生人数")));
                entity.setCounselorName(readString(row, mapping, "counselorName"));
                entity.setCounselorPhone(readString(row, mapping, "counselorPhone"));
                entity.setStatus(defaultInteger(readInteger(row, mapping, "status", warnings, "状态"), 1));
            }

            preparedRows.add(new PreparedRow<>(
                    rowNo,
                    create ? "新增" : "更新",
                    warnings.stream().noneMatch(this::isFatalWarning),
                    entity,
                    warnings,
                    buildClassSample(rowNo, code, name, deptId, readString(row, mapping, "grade"), create ? "新增" : "更新", warnings)
            ));
        }

        return new PreparedDataset<>(DATASET_CLASSES, "班级信息", preparedRows);
    }

    private PreparedDataset<Classroom> prepareClassrooms(NormalizedRequest request) {
        List<Map<String, Object>> rows = rowsOf(request.payload(), DATASET_CLASSROOMS);
        Map<String, String> mapping = mappingOf(request.mappings(), DATASET_CLASSROOMS);
        Map<String, Classroom> existing = loadClassrooms(extractCodes(rows, mapping, "roomNo"));
        Map<Long, Campus> campuses = loadCampuses(collectReferencedIds(rows, mapping, "campusId", request.defaultCampusId()));
        List<PreparedRow<Classroom>> preparedRows = new ArrayList<>();

        for (int index = 0; index < rows.size(); index++) {
            int rowNo = index + 1;
            Map<String, Object> row = rows.get(index);
            List<String> warnings = new ArrayList<>();
            String roomNo = readString(row, mapping, "roomNo");
            String roomName = readString(row, mapping, "roomName");
            Long campusId = readLong(row, mapping, "campusId", warnings, "校区ID");
            if (campusId == null) {
                campusId = request.defaultCampusId();
                if (campusId != null) {
                    warnings.add("未提供校区ID，已使用默认校区");
                }
            }
            Integer capacity = readInteger(row, mapping, "capacity", warnings, "容量");

            if (!StringUtils.hasText(roomNo)) {
                warnings.add("教室编号不能为空");
            }
            if (capacity == null) {
                warnings.add("教室容量不能为空");
            }
            if (campusId == null) {
                warnings.add("缺少校区ID，且未配置默认校区");
            } else if (!campuses.containsKey(campusId)) {
                warnings.add("校区ID不存在: " + campusId);
            }

            Classroom entity = existing.get(roomNo);
            boolean create = entity == null;
            if (create) {
                entity = new Classroom();
                entity.setRoomNo(roomNo);
            }
            if (warnings.stream().noneMatch(this::isFatalWarning)) {
                entity.setRoomName(StringUtils.hasText(roomName) ? roomName : roomNo);
                entity.setCampusId(campusId);
                entity.setBuilding(readString(row, mapping, "building"));
                entity.setFloor(readInteger(row, mapping, "floor", warnings, "楼层"));
                entity.setCapacity(capacity);
                entity.setRoomType(normalizeRoomType(readString(row, mapping, "roomType"), warnings));
                entity.setHasProjector(defaultFlag(row, mapping, "hasProjector", warnings, "投影设备"));
                entity.setHasMicrophone(defaultFlag(row, mapping, "hasMicrophone", warnings, "扩音设备"));
                entity.setHasAirConditioner(defaultFlag(row, mapping, "hasAirConditioner", warnings, "空调设备"));
                entity.setEquipmentDesc(readString(row, mapping, "equipmentDesc"));
                entity.setStatus(defaultInteger(readInteger(row, mapping, "status", warnings, "状态"), 1));
            }

            preparedRows.add(new PreparedRow<>(
                    rowNo,
                    create ? "新增" : "更新",
                    warnings.stream().noneMatch(this::isFatalWarning),
                    entity,
                    warnings,
                    buildClassroomSample(rowNo, roomNo, roomName, campusId, capacity, create ? "新增" : "更新", warnings)
            ));
        }

        return new PreparedDataset<>(DATASET_CLASSROOMS, "教室信息", preparedRows);
    }

    private DataSyncDatasetPreview buildPreview(PreparedDataset<?> dataset) {
        DataSyncDatasetPreview preview = new DataSyncDatasetPreview();
        preview.setDataset(dataset.dataset());
        preview.setLabel(dataset.label());
        preview.setTotalCount(dataset.rows().size());
        preview.setReadyCount((int) dataset.rows().stream().filter(PreparedRow::ready).count());
        preview.setCreateCount((int) dataset.rows().stream().filter(item -> item.ready() && "新增".equals(item.action())).count());
        preview.setUpdateCount((int) dataset.rows().stream().filter(item -> item.ready() && "更新".equals(item.action())).count());
        preview.setSkippedCount((int) dataset.rows().stream().filter(item -> !item.ready()).count());
        preview.setWarnings(dataset.rows().stream()
                .flatMap(item -> item.warnings().stream().map(warning -> "第" + item.rowNo() + "行：" + warning))
                .limit(8)
                .toList());
        preview.setSampleRows(dataset.rows().stream()
                .limit(3)
                .map(PreparedRow::sampleRow)
                .toList());
        return preview;
    }

    private DataSyncDatasetApplyResult applyCourses(PreparedDataset<Course> dataset) {
        int created = 0;
        int updated = 0;
        int skipped = 0;
        for (PreparedRow<Course> row : dataset.rows()) {
            if (!row.ready()) {
                skipped++;
                continue;
            }
            if ("新增".equals(row.action())) {
                created += courseMapper.insert(row.entity()) > 0 ? 1 : 0;
            } else {
                updated += courseMapper.updateById(row.entity()) > 0 ? 1 : 0;
            }
        }
        return buildApplyResult(dataset, created, updated, skipped);
    }

    private DataSyncDatasetApplyResult applyClasses(PreparedDataset<Clazz> dataset) {
        int created = 0;
        int updated = 0;
        int skipped = 0;
        for (PreparedRow<Clazz> row : dataset.rows()) {
            if (!row.ready()) {
                skipped++;
                continue;
            }
            if ("新增".equals(row.action())) {
                created += classMapper.insert(row.entity()) > 0 ? 1 : 0;
            } else {
                updated += classMapper.updateById(row.entity()) > 0 ? 1 : 0;
            }
        }
        return buildApplyResult(dataset, created, updated, skipped);
    }

    private DataSyncDatasetApplyResult applyClassrooms(PreparedDataset<Classroom> dataset) {
        int created = 0;
        int updated = 0;
        int skipped = 0;
        for (PreparedRow<Classroom> row : dataset.rows()) {
            if (!row.ready()) {
                skipped++;
                continue;
            }
            if ("新增".equals(row.action())) {
                created += classroomMapper.insert(row.entity()) > 0 ? 1 : 0;
            } else {
                updated += classroomMapper.updateById(row.entity()) > 0 ? 1 : 0;
            }
        }
        return buildApplyResult(dataset, created, updated, skipped);
    }

    private DataSyncDatasetApplyResult buildApplyResult(PreparedDataset<?> dataset, int created, int updated, int skipped) {
        DataSyncDatasetApplyResult result = new DataSyncDatasetApplyResult();
        result.setDataset(dataset.dataset());
        result.setLabel(dataset.label());
        result.setCreatedCount(created);
        result.setUpdatedCount(updated);
        result.setSkippedCount(skipped);
        return result;
    }

    private Map<String, Course> loadCourses(Set<String> codes) {
        if (codes.isEmpty()) {
            return Collections.emptyMap();
        }
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                        .in(Course::getCourseCode, codes))
                .stream()
                .filter(item -> StringUtils.hasText(item.getCourseCode()))
                .collect(Collectors.toMap(Course::getCourseCode, Function.identity(), (left, right) -> left));
    }

    private Map<String, Clazz> loadClasses(Set<String> codes) {
        if (codes.isEmpty()) {
            return Collections.emptyMap();
        }
        return classMapper.selectList(new LambdaQueryWrapper<Clazz>()
                        .in(Clazz::getClassCode, codes))
                .stream()
                .filter(item -> StringUtils.hasText(item.getClassCode()))
                .collect(Collectors.toMap(Clazz::getClassCode, Function.identity(), (left, right) -> left));
    }

    private Map<String, Classroom> loadClassrooms(Set<String> roomNos) {
        if (roomNos.isEmpty()) {
            return Collections.emptyMap();
        }
        return classroomMapper.selectList(new LambdaQueryWrapper<Classroom>()
                        .in(Classroom::getRoomNo, roomNos))
                .stream()
                .filter(item -> StringUtils.hasText(item.getRoomNo()))
                .collect(Collectors.toMap(Classroom::getRoomNo, Function.identity(), (left, right) -> left));
    }

    private Map<Long, Department> loadDepartments(Set<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return departmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(Department::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, Campus> loadCampuses(Set<Long> campusIds) {
        if (campusIds == null || campusIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return campusMapper.selectBatchIds(campusIds).stream()
                .collect(Collectors.toMap(Campus::getId, Function.identity(), (left, right) -> left));
    }

    private Set<String> extractCodes(List<Map<String, Object>> rows, Map<String, String> mapping, String field) {
        return rows.stream()
                .map(row -> readString(row, mapping, field))
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> collectReferencedIds(List<Map<String, Object>> rows, Map<String, String> mapping, String field, Long fallbackId) {
        Set<Long> ids = rows.stream()
                .map(row -> readLong(row, mapping, field, new ArrayList<>(), field))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (fallbackId != null) {
            ids.add(fallbackId);
        }
        return ids;
    }

    private List<Map<String, Object>> rowsOf(Map<String, List<Map<String, Object>>> payload, String dataset) {
        List<Map<String, Object>> rows = payload.get(dataset);
        return rows == null ? Collections.emptyList() : rows;
    }

    private Map<String, String> mappingOf(Map<String, Map<String, String>> mappings, String dataset) {
        Map<String, String> mapping = mappings.get(dataset);
        return mapping == null ? Collections.emptyMap() : mapping;
    }

    private String readString(Map<String, Object> row, Map<String, String> mapping, String targetField) {
        Object value = readValue(row, mapping, targetField);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Integer readInteger(Map<String, Object> row, Map<String, String> mapping, String targetField, List<String> warnings, String label) {
        Object value = readValue(row, mapping, targetField);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            warnings.add(label + "格式不正确: " + value);
            return null;
        }
    }

    private Long readLong(Map<String, Object> row, Map<String, String> mapping, String targetField, List<String> warnings, String label) {
        Object value = readValue(row, mapping, targetField);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            warnings.add(label + "格式不正确: " + value);
            return null;
        }
    }

    private BigDecimal readDecimal(Map<String, Object> row, Map<String, String> mapping, String targetField, List<String> warnings, String label) {
        Object value = readValue(row, mapping, targetField);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            warnings.add(label + "格式不正确: " + value);
            return null;
        }
    }

    private Integer defaultFlag(Map<String, Object> row, Map<String, String> mapping, String targetField, List<String> warnings, String label) {
        Object value = readValue(row, mapping, targetField);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return 0;
        }
        String text = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        if (List.of("1", "true", "yes", "y", "是", "有").contains(text)) {
            return 1;
        }
        if (List.of("0", "false", "no", "n", "否", "无").contains(text)) {
            return 0;
        }
        warnings.add(label + "无法识别，已按否处理: " + value);
        return 0;
    }

    private Object readValue(Map<String, Object> row, Map<String, String> mapping, String targetField) {
        String sourceField = mapping.get(targetField);
        if (!StringUtils.hasText(sourceField)) {
            sourceField = targetField;
        }
        if (row.containsKey(sourceField)) {
            return row.get(sourceField);
        }
        if (!Objects.equals(sourceField, targetField) && row.containsKey(targetField)) {
            return row.get(targetField);
        }
        return null;
    }

    private String normalizeCourseType(String rawType, List<String> warnings) {
        if (!StringUtils.hasText(rawType)) {
            return "REQUIRED";
        }
        String normalized = rawType.trim().toUpperCase(Locale.ROOT);
        if (List.of("REQUIRED", "ELECTIVE", "PRACTICE", "EXPERIMENT").contains(normalized)) {
            return normalized;
        }
        if (rawType.contains("必修")) {
            return "REQUIRED";
        }
        if (rawType.contains("选修")) {
            return "ELECTIVE";
        }
        if (rawType.contains("实验")) {
            return "EXPERIMENT";
        }
        if (rawType.contains("实践")) {
            return "PRACTICE";
        }
        warnings.add("课程类型无法识别，已按必修处理: " + rawType);
        return "REQUIRED";
    }

    private String normalizeRoomType(String rawType, List<String> warnings) {
        if (!StringUtils.hasText(rawType)) {
            return "GENERAL";
        }
        String normalized = rawType.trim().toUpperCase(Locale.ROOT);
        if (List.of("GENERAL", "MULTIMEDIA", "LAB", "COMPUTER", "LECTURE_HALL").contains(normalized)) {
            return normalized;
        }
        if (rawType.contains("多媒体")) {
            return "MULTIMEDIA";
        }
        if (rawType.contains("实验")) {
            return "LAB";
        }
        if (rawType.contains("机房") || rawType.contains("计算机")) {
            return "COMPUTER";
        }
        if (rawType.contains("阶梯") || rawType.contains("报告厅") || rawType.contains("讲堂")) {
            return "LECTURE_HALL";
        }
        if (rawType.contains("普通") || rawType.contains("标准")) {
            return "GENERAL";
        }
        warnings.add("教室类型无法识别，已按普通教室处理: " + rawType);
        return "GENERAL";
    }

    private boolean isFatalWarning(String warning) {
        return warning.contains("不能为空")
                || warning.contains("缺少")
                || warning.contains("不存在");
    }

    private Integer defaultInteger(Integer value) {
        return defaultInteger(value, 0);
    }

    private Integer defaultInteger(Integer value, Integer fallback) {
        return value == null ? fallback : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private Map<String, String> buildCourseSample(int rowNo, String code, String name, Long deptId, String courseType, String action, List<String> warnings) {
        Map<String, String> sample = new LinkedHashMap<>();
        sample.put("行号", String.valueOf(rowNo));
        sample.put("编码", defaultText(code));
        sample.put("名称", defaultText(name));
        sample.put("类型", defaultText(courseType));
        sample.put("院系ID", deptId == null ? "-" : String.valueOf(deptId));
        sample.put("操作", warnings.stream().noneMatch(this::isFatalWarning) ? action : "跳过");
        return sample;
    }

    private Map<String, String> buildClassSample(int rowNo, String code, String name, Long deptId, String grade, String action, List<String> warnings) {
        Map<String, String> sample = new LinkedHashMap<>();
        sample.put("行号", String.valueOf(rowNo));
        sample.put("编码", defaultText(code));
        sample.put("名称", defaultText(name));
        sample.put("年级", defaultText(grade));
        sample.put("院系ID", deptId == null ? "-" : String.valueOf(deptId));
        sample.put("操作", warnings.stream().noneMatch(this::isFatalWarning) ? action : "跳过");
        return sample;
    }

    private Map<String, String> buildClassroomSample(int rowNo, String roomNo, String roomName, Long campusId, Integer capacity, String action, List<String> warnings) {
        Map<String, String> sample = new LinkedHashMap<>();
        sample.put("行号", String.valueOf(rowNo));
        sample.put("教室编号", defaultText(roomNo));
        sample.put("教室名称", defaultText(roomName));
        sample.put("校区ID", campusId == null ? "-" : String.valueOf(campusId));
        sample.put("容量", capacity == null ? "-" : String.valueOf(capacity));
        sample.put("操作", warnings.stream().noneMatch(this::isFatalWarning) ? action : "跳过");
        return sample;
    }

    private String defaultText(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private record NormalizedRequest(
            String provider,
            Long defaultDeptId,
            Long defaultCampusId,
            Map<String, Map<String, String>> mappings,
            Map<String, List<Map<String, Object>>> payload
    ) {
    }

    private record PreparedDataset<T>(
            String dataset,
            String label,
            List<PreparedRow<T>> rows
    ) {
    }

    private record PreparedRow<T>(
            int rowNo,
            String action,
            boolean ready,
            T entity,
            List<String> warnings,
            Map<String, String> sampleRow
    ) {
    }
}
