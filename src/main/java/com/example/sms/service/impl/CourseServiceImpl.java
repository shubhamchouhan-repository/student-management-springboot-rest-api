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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;


    // CREATE
    @Override
    @Transactional
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
                .build();

        Course saved = courseRepository.save(courseBuild);
        return entityToDto(saved);
    }


    // GET BY ID
    @Override
    public CourseResponseDto getCourseById(Long id) {
        log.info("Getting course with id: {}", id);
        Course courseById = findCourseById(id);
        return entityToDto(courseById);
    }


    // UPDATE
    @Override
    @Transactional
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

        return entityToDto(courseRepository.save(course));
    }


    // DELETE
    @Override
    @Transactional
    public void deleteCourse(Long id) {
        log.info("Deleting course with ID: {}", id);

        Course course = findCourseById(id);

        if (studentRepository.existsByCoursesId(id)) {
            throw new ConflictException("Cannot delete course: students are still enrolled in it");
        }

        courseRepository.delete(course);
        log.info("Deleted course with ID: {}", id);
    }


    // HELPER
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
    private CourseResponseDto entityToDto(Course c) {
        Department d = c.getDepartment();
        return CourseResponseDto.builder()
                .id(c.getId())
                .courseCode(c.getCourseCode())
                .title(c.getTitle())
                .description(c.getDescription())
                .credits(c.getCredits())
                .department(DepartmentSummaryDto.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .code(d.getCode())
                        .build())
                .build();
    }
}
