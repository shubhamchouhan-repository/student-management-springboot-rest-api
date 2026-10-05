package com.example.sms.service.impl;

import com.example.sms.dto.StudentPatchRequestDto;
import com.example.sms.dto.StudentRequestDto;
import com.example.sms.dto.StudentResponseDto;
import com.example.sms.entity.Course;
import com.example.sms.entity.Department;
import com.example.sms.entity.Student;
import com.example.sms.exception.DuplicateResourceException;
import com.example.sms.exception.ResourceNotFoundException;
import com.example.sms.repository.CourseRepository;
import com.example.sms.repository.DepartmentRepository;
import com.example.sms.repository.StudentRepository;
import com.example.sms.service.FileStorageService;
import com.example.sms.service.StudentService;
import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final FileStorageService fileStorageService;

//    public static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);


    // CREATE
    @Override
    @Transactional
    public StudentResponseDto createStudent(StudentRequestDto studentRequestDto) {

        log.warn("Creating student with email: {}", studentRequestDto.getEmail());
        if (studentRepository.existsByEmail(studentRequestDto.getEmail())) {
            log.warn("Student creation failed. Email already exists: {}", studentRequestDto.getEmail());

            throw new DuplicateResourceException("Student already exists with email : " + studentRequestDto.getEmail());
        }

        Student student = dtoToEntity(studentRequestDto);
        Student savedStudent = studentRepository.save(student);

        log.info("Student created successfully with id: {}", savedStudent.getId());
        return entityToDto(savedStudent);
    }


    // GET BY ID
    @Override
    public StudentResponseDto getStudentById(Long id) {
        log.debug("Fetching student with id: {}", id);
        Student student = findStudentById(id);

        log.debug("Student found with id: {}", id);
        return entityToDto(student);
    }


    // GET ALL // PAGINATION
    @Override
    public Page<StudentResponseDto> getAllStudents(
            int page,
            int size,
            String sortBy,
            String direction) {

        log.debug(
                "Fetching students - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction
        );

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StudentResponseDto> students = studentRepository.findAll(pageable)
                .map(this::entityToDto);

        log.info("Fetched {} students", students.getNumberOfElements());

        return students;
    }


    // UPDATE
    @Override
    @Transactional
    public StudentResponseDto updateStudent(Long id, StudentRequestDto studentRequestDto) {

        log.info("Updating student with id: {}", id);
        Student existingStudent = findStudentById(id);

        if (studentRepository.existsByEmailAndIdNot(studentRequestDto.getEmail(), id)) {
            log.warn("Student update failed. Email {} is already used by another student", studentRequestDto.getEmail());

            throw new DuplicateResourceException("Another student already exists with email : " + studentRequestDto.getEmail());
        }
        // Update existing entity
        existingStudent.setFirstName(studentRequestDto.getFirstName());
        existingStudent.setLastName(studentRequestDto.getLastName());
        existingStudent.setEmail(studentRequestDto.getEmail());
        existingStudent.setPhoneNumber(studentRequestDto.getPhoneNumber());
        existingStudent.setDateOfBirth(studentRequestDto.getDateOfBirth());

        Student updatedStudent = studentRepository.save(existingStudent);

        log.info("Student updated successfully with id: {}", updatedStudent.getId());
        return entityToDto(updatedStudent);
    }


    // PATCH
    @Override
    @Transactional
    public StudentResponseDto patchStudent(Long id, StudentPatchRequestDto dto) {

        log.info("Partially updating student with id: {}", id);
        Student existingStudent = findStudentById(id);

        boolean updated = false;

        if (dto.getFirstName() != null) {
            existingStudent.setFirstName(dto.getFirstName());
            updated = true;
            log.debug("First name updated for student id: {}", id);
        }

        if (dto.getLastName() != null) {
            existingStudent.setLastName(dto.getLastName());
            updated = true;
            log.debug("Last name updated for student id: {}", id);
        }

        if (dto.getEmail() != null) {

            if (studentRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
                log.warn("Student patch failed. Email {} is already used by another student", dto.getEmail());

                throw new DuplicateResourceException("Another student already exists with email : " + dto.getEmail());
            }

            existingStudent.setEmail(dto.getEmail());
            updated = true;

            log.debug("Email updated for student id: {}", id);
        }


        if (dto.getPhoneNumber() != null) {
            existingStudent.setPhoneNumber(dto.getPhoneNumber());
            updated = true;
            log.debug("Phone number updated for student id: {}", id);
        }

        if (dto.getDateOfBirth() != null) {
            existingStudent.setDateOfBirth(dto.getDateOfBirth());
            updated = true;
            log.debug("Date of birth updated for student id: {}", id);
        }

        if (!updated) {
            log.warn("No fields provided for patch operation. Student id: {}", id);

            // It would be better to reject an empty PATCH request.
            throw new IllegalArgumentException(
                    "At least one field must be provided for update"
            );
        }

        Student patchStudent = studentRepository.save(existingStudent);
        log.info("Student patched successfully with id: {}", patchStudent.getId());
        return entityToDto(patchStudent);
    }


    // DELETE
    @Override
    @Transactional
    public void deleteStudent(Long id) {
        log.info("Deleting student with id: {}", id);
        Student existingStudent = findStudentById(id);

        studentRepository.delete(existingStudent);
        log.info("Student deleted successfully with id: {}", id);
    }


    // =================================================================================================================
    // MAPPING: student, department & courses
    // =================================================================================================================


    @Override
    @Transactional
    public StudentResponseDto assignDepartmentToStudent(Long studentId, Long departmentId) {

        log.info("Assigning student {} to department {}", studentId, departmentId);

        Student existingStudent = findStudentById(studentId);
        existingStudent.setDepartment(findDepartmentById(departmentId));

        return entityToDto(studentRepository.save(existingStudent));
    }


    @Override
    @Transactional
    public StudentResponseDto enrollStudentInCourse(Long studentId, Long courseId) {
        log.info("Enrolling student {} in course {}", studentId, courseId);

        Student student = findStudentById(studentId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found : " + courseId));

        boolean alreadyEnrolled = student.getCourses().stream()
                .anyMatch(c -> c.getId().equals(courseId));

        if (alreadyEnrolled) {
            throw new DuplicateResourceException("Student " + studentId + " is already enrolled in course " + courseId);
        }

        student.getCourses().add(course);

        return entityToDto(studentRepository.save(student));
    }


    @Override
    @Transactional
    public StudentResponseDto unenrollStudentFromCourse(Long studentId, Long courseId) {
        log.info("Removing student {} from course {}", studentId, courseId);

        Student student = findStudentById(studentId);

        boolean removed = student.getCourses().removeIf(c -> c.getId().equals(courseId));

        if (!removed) {
            throw new ResourceNotFoundException("Student " + studentId + " is not enrolled in course " + courseId);
        }

        return entityToDto(studentRepository.save(student));
    }


    // =================================================================================================================
    // MULTIPART: PROFILE IMAGE
    // =================================================================================================================

    // UPLOAD / REPLACE PROFILE IMAGE
    @Override
    @Transactional
    public String uploadProfileImage(Long studentId, MultipartFile file) {

        log.info("Uploading profile image for student id: {}", studentId);

        // 1. Find student
        Student student = findStudentById(studentId);

        // 2. Keep old image name
        String oldImageName = student.getProfileImageName();

        // 3. Store new image
        String newImageName = fileStorageService.store(file);

        try {
            // 4. Update database with new image name
            student.setProfileImageName(newImageName);

            studentRepository.save(student);

            // 5. Delete old image after successful update
            if (oldImageName != null && !oldImageName.isBlank()) {
                fileStorageService.delete(oldImageName);
            }

            log.info("Profile image uploaded successfully for student id: {}", studentId);

            // Return public API URL
            return "/api/v1/students/"
                    + studentId
                    + "/profile-image";

        } catch (RuntimeException e) {
            /*
             * If database update fails after the new file
             * was created, remove the new file.
             */
            fileStorageService.delete(newImageName);
            throw e;
        }
    }


    // GET PROFILE IMAGE
    @Override
    public Resource getProfileImage(Long studentId) {

        log.debug("Fetching profile image for student id: {}", studentId);

        // 1. Find student
        Student student = findStudentById(studentId);

        // 2. Get image filename
        String imageName = student.getProfileImageName();

        // 3. Check image exists
        if (imageName == null || imageName.isBlank()) {
            throw new ResourceNotFoundException("Profile image not found for student : " + studentId);
        }

        // 4. Load image
        return fileStorageService.load(imageName);
    }


    // DELETE PROFILE IMAGE
    @Override
    @Transactional
    public void deleteProfileImage(Long studentId) {

        log.info(
                "Deleting profile image for student id: {}",
                studentId
        );

        // 1. Find student
        Student student = findStudentById(studentId);

        // 2. Get image filename
        String imageName = student.getProfileImageName();

        // 3. Check image exists
        if (imageName == null || imageName.isBlank()) {
            throw new ResourceNotFoundException("Profile image not found for student : " + studentId);
        }

        // 4. Delete physical file
        fileStorageService.delete(imageName);

        // 5. Remove filename from database
        student.setProfileImageName(null);

        studentRepository.save(student);

        log.info("Profile image deleted successfully for student id: {}", studentId);
    }

    // HELPER METHODS===================================================================================================

    // Helper method to find student
    public Student findStudentById(Long id) {

        log.debug("Searching for student with id: {}", id);

        return studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Student not found with id: {}", id);

                    return new ResourceNotFoundException("Student not found : " + id);
                });
    }

    // Helper method to find department
    private Department findDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found : " + id));
    }

    // Helper method: DTO → Entity
    private Student dtoToEntity(StudentRequestDto studentRequestDto) {
        log.debug("Converting StudentRequestDto to Student entity");
        return Student.builder()
                .firstName(studentRequestDto.getFirstName())
                .lastName(studentRequestDto.getLastName())
                .email(studentRequestDto.getEmail())
                .phoneNumber(studentRequestDto.getPhoneNumber())
                .dateOfBirth(studentRequestDto.getDateOfBirth())
                .build();
    }

    // Helper method: Entity → DTO
    private StudentResponseDto entityToDto(Student student) {

        log.debug("Converting Student entity to StudentResponseDto. id: {}", student.getId());

        String profileImageUrl = null;

        if (student.getProfileImageName() != null && !student.getProfileImageName().isBlank()) {
            profileImageUrl = "/api/v1/students/" + student.getId() + "/profile-image";
        }

        return StudentResponseDto.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phoneNumber(student.getPhoneNumber())
                .dateOfBirth(student.getDateOfBirth())
                .profileImageUrl(profileImageUrl)
                .build();
    }



}









/*
-> Without Builder, you can use a constructor or setters.

    private StudentResponseDto entityToDto(Student student) {
        StudentResponseDto dto = new StudentResponseDto();
        dto.setId(student.getId());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setEmail(student.getEmail());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setDateOfBirth(student.getDateOfBirth());
        return dto;
    }
*/

