package com.example.sms.controller;

import com.example.sms.dto.CourseRequestDto;
import com.example.sms.dto.CourseResponseDto;
import com.example.sms.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Course", description = "Course CRUD")
public class CourseController {

    private final CourseService courseService;


    // CREATE
    @Operation(summary = "Create a course under a department")
    @PostMapping
    public ResponseEntity<CourseResponseDto> create(@Valid @RequestBody CourseRequestDto courseRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(courseRequestDto));
    }


    // GET ALL
    @Operation(summary = "List all courses (cached)")
    @GetMapping
    public ResponseEntity<List<CourseResponseDto>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // GET BY ID
    @Operation(summary = "Get a course by id (cached)")
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }


    // UPDATE
    @Operation(summary = "Update a course")
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDto> update(@PathVariable Long id, @Valid @RequestBody CourseRequestDto courseRequestDto) {
        return ResponseEntity.ok(courseService.updateCourse(id, courseRequestDto));
    }


    // DELETE
    @Operation(summary = "Delete a course")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}