package com.example.sms.service;

import com.example.sms.dto.CourseRequestDto;
import com.example.sms.dto.CourseResponseDto;

import java.util.List;


public interface CourseService {

    CourseResponseDto createCourse(CourseRequestDto dto);

    List<CourseResponseDto> getAllCourses();

    CourseResponseDto getCourseById(Long id);

    CourseResponseDto updateCourse(Long id, CourseRequestDto dto);

    void deleteCourse(Long id);
}