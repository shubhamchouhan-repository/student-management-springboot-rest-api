package com.example.sms.controller;

import com.example.sms.dto.StudentPatchRequestDto;
import com.example.sms.dto.StudentRequestDto;
import com.example.sms.dto.StudentResponseDto;
import com.example.sms.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;


@RestController
@RequestMapping("/api/v1/students")
@Slf4j
@Tag(name = "Student", description = "Student CRUD, search, and photo upload")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }


    // CREATE
    @Operation(summary = "Create a student")
    @ApiResponse(responseCode = "201", description = "Student created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "409", description = "Email already in use")
    @PostMapping()
    public ResponseEntity<StudentResponseDto> createStudent(@Valid @RequestBody StudentRequestDto studentRequestDto) {

        log.info("Received request to create student");

        StudentResponseDto response = studentService.createStudent(studentRequestDto);


        log.info("Student created successfully with id: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // GET ALL
    @GetMapping()
    public ResponseEntity<Page<StudentResponseDto>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(studentService.getAllStudents(page, size, sortBy, direction));
    }


    // GET BY ID
    @Operation(summary = "Get a student by id")
    @ApiResponse(responseCode = "404", description = "Student not found")
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudentById(@PathVariable Long id) {

        log.debug("Received request to fetch student with id: {}", id);

        StudentResponseDto response = studentService.getStudentById(id);

        log.info("Student fetched successfully with id: {}", id);

        return ResponseEntity.ok(response);
    }


    // UPDATE
    @Operation(summary = "Update a student")
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequestDto studentRequestDto) {

        log.info("Received request to update student with id: {}", id);

        StudentResponseDto response = studentService.updateStudent(id, studentRequestDto);

        log.info("Student updated successfully with id: {}", id);

        return ResponseEntity.ok(response);
    }


    // PATCH
    @PatchMapping("/{id}")
    public ResponseEntity<StudentResponseDto> patchStudent(@PathVariable Long id, @Valid @RequestBody StudentPatchRequestDto studentPatchRequestDto) {

        log.info("Received request to partially update student with id: {}", id);

        StudentResponseDto response = studentService.patchStudent(id, studentPatchRequestDto);

        log.info("Student partially updated successfully with id: {}", id);

        return ResponseEntity.ok(response);
    }


    // DELETE
    @Operation(summary = "Delete a student")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>  deleteStudent(@PathVariable Long id) {

        log.info("Received request to delete student with id: {}", id);

        studentService.deleteStudent(id);

        log.info("Student deleted successfully with id: {}", id);

        return ResponseEntity.noContent().build();
    }


    // =================================================================================================================
    // MAPPING: student, department & courses
    // =================================================================================================================

    @PutMapping("/{studentId}/assign-department/{departmentId}")
    public ResponseEntity<StudentResponseDto> assignDepartmentToStudent(@PathVariable Long studentId, @PathVariable Long departmentId) {
        return ResponseEntity.ok(studentService.assignDepartmentToStudent(studentId, departmentId));
    }


    @PostMapping("/{studentId}/enroll-courses/{courseId}")
    public ResponseEntity<StudentResponseDto> enrollStudentInCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        return ResponseEntity.ok(studentService.enrollStudentInCourse(studentId, courseId));
    }


    @DeleteMapping("/{studentId}/unenroll-courses/{courseId}")
    public ResponseEntity<StudentResponseDto> unenrollStudentFromCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        return ResponseEntity.ok(studentService.unenrollStudentFromCourse(studentId, courseId));
    }


    // =================================================================================================================
    // MULTIPART: PROFILE IMAGE
    // =================================================================================================================

    // UPLOAD / REPLACE PROFILE IMAGE
    @Operation(summary = "Upload or replace a student's photo",
            description = "multipart/form-data with a single field named 'file'. Max 5MB.")
    @PostMapping(value = "/{studentId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadProfileImage(@PathVariable Long studentId, @RequestParam("file") MultipartFile file) {

        log.info("Received request to upload profile image for student id: {}", studentId);

        String imageUrl = studentService.uploadProfileImage(studentId, file);

        log.info("Profile image uploaded successfully for student id: {}", studentId);

        return ResponseEntity.status(HttpStatus.CREATED).body(imageUrl);
    }


    // GET PROFILE IMAGE
    @GetMapping("/{studentId}/profile-image")
    public ResponseEntity<Resource> getProfileImage(@PathVariable Long studentId) {

        log.debug("Received request to get profile image for student id: {}", studentId);

        Resource resource = studentService.getProfileImage(studentId);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        try {
            String contentType = resource.getURL()
                            .openConnection()
                            .getContentType();

            if (contentType != null) {
                mediaType = MediaType.parseMediaType(contentType);
            }

        } catch (Exception e) {
            log.warn("Could not determine content type for profile image");
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                resource.getFilename() +
                                "\""
                )
                .body(resource);
    }


    // DELETE PROFILE IMAGE
    @DeleteMapping("/{studentId}/profile-image")
    public ResponseEntity<Void> deleteProfileImage(@PathVariable Long studentId) {

        log.info("Received request to delete profile image for student id: {}", studentId);

        studentService.deleteProfileImage(studentId);

        log.info("Profile image deleted successfully for student id: {}", studentId);

        return ResponseEntity.noContent().build();
    }

}
