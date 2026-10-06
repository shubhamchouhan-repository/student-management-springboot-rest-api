package com.example.sms.service.impl;

import com.example.sms.dto.CourseRequestDto;
import com.example.sms.dto.CourseResponseDto;
import com.example.sms.dto.DepartmentSummaryDto;
import com.example.sms.entity.Course;
import com.example.sms.entity.Department;
import com.example.sms.exception.ConflictException;
import com.example.sms.exception.DuplicateResourceException;
import com.example.sms.exception.ResourceNotFoundException;
import com.example.sms.repository.CourseRepository;
import com.example.sms.repository.DepartmentRepository;
import com.example.sms.repository.StudentRepository;
import com.example.sms.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;

    private static final String CACHE_NAME = "courses";

    // CREATE
    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public CourseResponseDto createCourse(CourseRequestDto courseRequestDto) {

        log.info("Creating course with code: {}", courseRequestDto.getCourseCode());

        String code = courseRequestDto.getCourseCode().trim().toUpperCase();
        if (courseRepository.existsByCourseCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Course already exists with code : " + code);
        }

        Course courseBuild = Course.builder()
                .courseCode(courseRequestDto.getCourseCode())
                .title(courseRequestDto.getTitle())
                .description(courseRequestDto.getDescription())
                .credits(courseRequestDto.getCredits())
                .department(findDepartmentById(courseRequestDto.getDepartmentId()))
                .capacity(courseRequestDto.getCapacity())
                .build();

        Course saved = courseRepository.save(courseBuild);
        log.info("Course created with id: {}", saved.getId());
        return entityToDto(saved);
    }


    // GET ALL
    @Override
    @Cacheable(value = CACHE_NAME, key = "'all'")
    public List<CourseResponseDto> getAllCourses() {
        log.debug("Fetching all courses from DB (cache miss if you see this log)");
        return courseRepository.findAll().stream()
                .map(this::entityToDto)
                .toList();
    }


    // GET BY ID
    @Override
    @Cacheable(value = CACHE_NAME, key = "#id")
    public CourseResponseDto getCourseById(Long id) {
        log.debug("Fetching course with id: {} (cache miss if you see this log)", id);
        Course courseById = findCourseById(id);
        return entityToDto(courseById);
    }


    // UPDATE
    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public CourseResponseDto updateCourse(Long id, CourseRequestDto courseRequestDto) {
        log.info("Updating course with ID: {}", id);

        Course course = findCourseById(id);

        String code = courseRequestDto.getCourseCode().trim().toUpperCase();
        if (courseRepository.existsByCourseCodeIgnoreCaseAndIdNot(code, id)) {
            throw new DuplicateResourceException("Another course already exists with code : " + code);
        }

        course.setCourseCode(code);
        course.setTitle(courseRequestDto.getTitle().trim());
        course.setDescription(courseRequestDto.getDescription());
        course.setCredits(courseRequestDto.getCredits());
        course.setDepartment(findDepartmentById(courseRequestDto.getDepartmentId()));
        course.setCapacity(courseRequestDto.getCapacity());
        // enrolledCount is deliberately NOT updated here — it only ever
        // changes inside EnrollmentRequestServiceImpl.approve()

        Course updated = courseRepository.save(course);
        log.info("Course with id {} updated", updated.getId());
        return entityToDto(updated);
    }


    // DELETE
    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteCourse(Long id) {
        log.info("Deleting course with ID: {}", id);

        Course course = findCourseById(id);

        if (studentRepository.existsByCoursesId(id)) {
            throw new ConflictException("Cannot delete course: students are still enrolled in it");
        }

        courseRepository.delete(course);
        log.info("Deleted course with ID: {}", id);
    }


    // HELPERS ---------------------------------------------------------------------------------------------------------

    // FIND COURSE
    private Course findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found : " + id));
    }

    // FIND DEPARTMENT
    private Department findDepartmentById(Long id) {
        log.info("Finding department with id: {}", id);
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found : " + id));
    }

    // entity to dto
    private CourseResponseDto entityToDto(Course course) {
        Department department = course.getDepartment();

        return CourseResponseDto.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .title(course.getTitle())
                .description(course.getDescription())
                .credits(course.getCredits())
                .department(DepartmentSummaryDto.builder()
                        .id(department.getId())
                        .name(department.getName())
                        .code(department.getCode())
                        .build())
                .capacity(course.getCapacity())
                .enrolledCount(course.getEnrolledCount())
                .build();
    }
}
