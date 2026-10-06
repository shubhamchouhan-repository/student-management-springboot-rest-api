package com.example.sms.service.impl;

import com.example.sms.dto.DepartmentRequestDto;
import com.example.sms.dto.DepartmentResponseDto;
import com.example.sms.entity.Department;
import com.example.sms.exception.ConflictException;
import com.example.sms.exception.DuplicateResourceException;
import com.example.sms.exception.ResourceNotFoundException;
import com.example.sms.repository.CourseRepository;
import com.example.sms.repository.DepartmentRepository;
import com.example.sms.repository.StudentRepository;
import com.example.sms.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Departments change rarely but are read on almost every student/course
 * request, so they're a good first candidate for caching:
 * - getAllDepartments() / getDepartmentById(): cached, served from Redis
 *   after the first call.
 * - create/update/delete: evict the cache so the next read rebuilds it
 *   from the database rather than serving stale data.
 */


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository  studentRepository;
    private final CourseRepository courseRepository;

    // value = CACHE_NAME -> value specifies the name of the cache where Spring will store the result.
    private static final String CACHE_NAME = "departments";

    // CREATE
    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public DepartmentResponseDto createDepartment(DepartmentRequestDto departmentRequestDto) {

        log.info("Creating department with code: {}", departmentRequestDto.getCode());

        if (departmentRepository.existsByNameIgnoreCase(departmentRequestDto.getName().trim())) {
            throw new DuplicateResourceException("Department already exists with name : " + departmentRequestDto.getName());
        }
        if (departmentRepository.existsByCodeIgnoreCase(departmentRequestDto.getCode().trim())) {
            throw new DuplicateResourceException("Department already exists with code : " + departmentRequestDto.getCode());
        }

        Department departmentBuild = Department.builder()
                .name(departmentRequestDto.getName())
                .code(departmentRequestDto.getCode())
                .description(departmentRequestDto.getDescription())
                .build();

        Department saved = departmentRepository.save(departmentBuild);

        log.info("Department created with id: {}", saved.getId());
        return entityToDto(saved);
    }


    // GET ALL
    @Override
    @Cacheable(value = CACHE_NAME, key = "'all'")
    public List<DepartmentResponseDto> getAllDepartments() {
        log.debug("Fetching all departments from DB (cache miss if you see this log)");
        return departmentRepository.findAll().stream()
                .map(this::entityToDto)
                .toList();
    }


    // GET BY ID
    @Override
    @Cacheable(value = CACHE_NAME, key = "#id")
    public DepartmentResponseDto getDepartmentById(Long id) {
        log.debug("Fetching department with id: {} (cache miss if you see this log)", id);
        return entityToDto(findDepartmentById(id));
    }


    // UPDATE
    @Override
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto departmentRequestDto) {
        log.info("Updating department with ID: {}", id);

        Department department = findDepartmentById(id);

        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(departmentRequestDto.getName().trim(), id)) {
            throw new DuplicateResourceException("Another department already exists with name : " + departmentRequestDto.getName());
        }
        if (departmentRepository.existsByCodeIgnoreCaseAndIdNot(departmentRequestDto.getCode().trim(), id)) {
            throw new DuplicateResourceException("Another department already exists with code : " + departmentRequestDto.getCode());
        }

        department.setName(departmentRequestDto.getName().trim());
        department.setCode(departmentRequestDto.getCode().trim().toUpperCase());
        department.setDescription(departmentRequestDto.getDescription());

        Department updated = departmentRepository.save(department);
        log.info("Department with id {} updated", updated.getId());
        return entityToDto(updated);
    }


    // DELETE
    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteDepartment(Long id) {

        log.info("Deleting department with ID: {}", id);

        Department department = findDepartmentById(id);

        if (studentRepository.existsByDepartmentId(id)) {
            throw new ConflictException("Cannot delete department: students are still assigned to it");
        }
        if (courseRepository.existsByDepartmentId(id)) {
            throw new ConflictException("Cannot delete department: it still has courses");
        }

        // cascade = ALL + orphanRemoval on Department.courses means every
        // course under this department is deleted along with it.
        departmentRepository.delete(department);

        log.info("Department with id {} deleted (its courses cascaded)", id);
    }



    // HELPER METHODS
    public Department findDepartmentById(Long id) {
        log.info("Finding department with id: {}", id);
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found : " + id));
    }

    // entity -> dto
    private DepartmentResponseDto entityToDto(Department department) {
        return DepartmentResponseDto.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .description(department.getDescription())
                .courseCount(department.getCourses() == null ? 0 : department.getCourses().size())
                .build();
    }
}
