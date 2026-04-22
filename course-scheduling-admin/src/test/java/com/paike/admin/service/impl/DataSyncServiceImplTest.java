package com.paike.admin.service.impl;

import com.paike.admin.dto.DataSyncApplyResponse;
import com.paike.admin.dto.DataSyncDatasetPreview;
import com.paike.admin.dto.DataSyncPreviewResponse;
import com.paike.admin.dto.DataSyncRequest;
import com.paike.admin.entity.Campus;
import com.paike.admin.entity.Classroom;
import com.paike.admin.entity.Course;
import com.paike.admin.entity.Department;
import com.paike.admin.mapper.CampusMapper;
import com.paike.admin.mapper.ClassMapper;
import com.paike.admin.mapper.ClassroomMapper;
import com.paike.admin.mapper.CourseMapper;
import com.paike.admin.mapper.DepartmentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSyncServiceImplTest {

    @Test
    void previewUsesDefaultDepartmentAndMarksInvalidRowsAsSkipped() {
        DataSyncServiceImpl service = new DataSyncServiceImpl();
        Department department = new Department();
        department.setId(9L);
        ReflectionTestUtils.setField(service, "courseMapper", courseMapperProxy(List.of(), new ArrayList<>(), new ArrayList<>()));
        ReflectionTestUtils.setField(service, "classMapper", classMapperProxy());
        ReflectionTestUtils.setField(service, "classroomMapper", classroomMapperProxy(List.of(), new ArrayList<>(), new ArrayList<>()));
        ReflectionTestUtils.setField(service, "departmentMapper", departmentMapperProxy(Map.of(9L, department)));
        ReflectionTestUtils.setField(service, "campusMapper", campusMapperProxy(Map.of()));

        DataSyncRequest request = new DataSyncRequest();
        request.setProvider("custom");
        request.setDefaultDeptId(9L);
        request.setPayload(Map.of(
                "courses", List.of(
                        new HashMap<>(Map.of("code", "CS101", "name", "数据结构")),
                        new HashMap<>(Map.of("name", "缺少编码"))
                )
        ));

        DataSyncPreviewResponse response = service.preview(request);
        DataSyncDatasetPreview courses = response.getDatasets().stream()
                .filter(item -> "courses".equals(item.getDataset()))
                .findFirst()
                .orElseThrow();

        assertEquals(2, courses.getTotalCount());
        assertEquals(1, courses.getReadyCount());
        assertEquals(1, courses.getCreateCount());
        assertEquals(1, courses.getSkippedCount());
        assertTrue(courses.getWarnings().stream().anyMatch(item -> item.contains("课程编码不能为空")));
    }

    @Test
    void applyClassroomsCreatesAndUpdatesRowsWithDefaultCampus() {
        DataSyncServiceImpl service = new DataSyncServiceImpl();
        Campus campus = new Campus();
        campus.setId(3L);
        Classroom existing = new Classroom();
        existing.setId(11L);
        existing.setRoomNo("A101");
        existing.setRoomName("旧教室");

        List<Classroom> inserted = new ArrayList<>();
        List<Classroom> updated = new ArrayList<>();
        ReflectionTestUtils.setField(service, "courseMapper", courseMapperProxy(List.of(), new ArrayList<>(), new ArrayList<>()));
        ReflectionTestUtils.setField(service, "classMapper", classMapperProxy());
        ReflectionTestUtils.setField(service, "classroomMapper", classroomMapperProxy(List.of(existing), inserted, updated));
        ReflectionTestUtils.setField(service, "departmentMapper", departmentMapperProxy(Map.of()));
        ReflectionTestUtils.setField(service, "campusMapper", campusMapperProxy(Map.of(3L, campus)));

        DataSyncRequest request = new DataSyncRequest();
        request.setDefaultCampusId(3L);
        request.setPayload(Map.of(
                "classrooms", List.of(
                        new HashMap<>(Map.of("roomNo", "A101", "roomName", "一教101", "capacity", 80)),
                        new HashMap<>(Map.of("roomNo", "B202", "roomName", "二教202", "capacity", 60))
                )
        ));

        DataSyncApplyResponse response = service.apply(request);

        assertEquals(1, response.getTotalCreated());
        assertEquals(1, response.getTotalUpdated());
        assertEquals(0, response.getTotalSkipped());
        assertEquals(1, inserted.size());
        assertEquals(1, updated.size());
        assertEquals(3L, inserted.get(0).getCampusId());
        assertEquals(3L, updated.get(0).getCampusId());
    }

    private CourseMapper courseMapperProxy(List<Course> existing, List<Course> inserted, List<Course> updated) {
        return (CourseMapper) Proxy.newProxyInstance(
                CourseMapper.class.getClassLoader(),
                new Class[]{CourseMapper.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "selectList" -> existing;
                    case "insert" -> {
                        inserted.add(copyCourse((Course) args[0]));
                        yield 1;
                    }
                    case "updateById" -> {
                        updated.add(copyCourse((Course) args[0]));
                        yield 1;
                    }
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private ClassMapper classMapperProxy() {
        return (ClassMapper) Proxy.newProxyInstance(
                ClassMapper.class.getClassLoader(),
                new Class[]{ClassMapper.class},
                (proxy, method, args) -> defaultValue(method.getReturnType())
        );
    }

    private ClassroomMapper classroomMapperProxy(List<Classroom> existing, List<Classroom> inserted, List<Classroom> updated) {
        return (ClassroomMapper) Proxy.newProxyInstance(
                ClassroomMapper.class.getClassLoader(),
                new Class[]{ClassroomMapper.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "selectList" -> existing;
                    case "insert" -> {
                        inserted.add(copyClassroom((Classroom) args[0]));
                        yield 1;
                    }
                    case "updateById" -> {
                        updated.add(copyClassroom((Classroom) args[0]));
                        yield 1;
                    }
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

    private DepartmentMapper departmentMapperProxy(Map<Long, Department> departments) {
        return (DepartmentMapper) Proxy.newProxyInstance(
                DepartmentMapper.class.getClassLoader(),
                new Class[]{DepartmentMapper.class},
                (proxy, method, args) -> {
                    if ("selectBatchIds".equals(method.getName())) {
                        Collection<?> ids = (Collection<?>) args[0];
                        return ids.stream().map(id -> departments.get(id)).filter(item -> item != null).toList();
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private CampusMapper campusMapperProxy(Map<Long, Campus> campuses) {
        return (CampusMapper) Proxy.newProxyInstance(
                CampusMapper.class.getClassLoader(),
                new Class[]{CampusMapper.class},
                (proxy, method, args) -> {
                    if ("selectBatchIds".equals(method.getName())) {
                        Collection<?> ids = (Collection<?>) args[0];
                        return ids.stream().map(id -> campuses.get(id)).filter(item -> item != null).toList();
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private Course copyCourse(Course source) {
        Course target = new Course();
        target.setId(source.getId());
        target.setCourseCode(source.getCourseCode());
        target.setCourseName(source.getCourseName());
        target.setDeptId(source.getDeptId());
        return target;
    }

    private Classroom copyClassroom(Classroom source) {
        Classroom target = new Classroom();
        target.setId(source.getId());
        target.setRoomNo(source.getRoomNo());
        target.setRoomName(source.getRoomName());
        target.setCampusId(source.getCampusId());
        target.setCapacity(source.getCapacity());
        return target;
    }

    private Object defaultValue(Class<?> type) {
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == boolean.class) {
            return false;
        }
        return null;
    }
}
