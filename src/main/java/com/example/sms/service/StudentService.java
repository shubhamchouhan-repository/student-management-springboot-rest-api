package com.example.sms.service;

import com.example.sms.dto.StudentPatchRequestDto;
import com.example.sms.dto.StudentRequestDto;
import com.example.sms.dto.StudentResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

public interface StudentService {

    StudentResponseDto createStudent(StudentRequestDto studentRequestDto);

    StudentResponseDto getStudentById(Long id);

    Page<StudentResponseDto> getAllStudents(int page, int size, String sortBy, String direction);

    StudentResponseDto updateStudent(Long id, StudentRequestDto studentRequestDto);

    StudentResponseDto patchStudent(Long id, StudentPatchRequestDto studentPatchRequestDto);

    void deleteStudent(Long id);


    // ---- mapping: student, department & courses ---------------------------------------------------------------------

    StudentResponseDto assignDepartmentToStudent(Long studentId, Long departmentId);

    StudentResponseDto enrollStudentInCourse(Long studentId, Long courseId);

    StudentResponseDto unenrollStudentFromCourse(Long studentId, Long courseId);


    // ---- multipart: profile image -----------------------------------------------------------------------------------

    String uploadProfileImage(Long studentId, MultipartFile file);

    Resource getProfileImage(Long studentId);

    void deleteProfileImage(Long studentId);

}
