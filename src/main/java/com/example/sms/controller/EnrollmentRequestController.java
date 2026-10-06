package com.example.sms.controller;


import com.example.sms.dto.EnrollmentRequestDto;
import com.example.sms.dto.EnrollmentResponseDto;
import com.example.sms.service.EnrollmentRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollment-requests")
@RequiredArgsConstructor
@Tag(name = "Enrollment Request", description = "Student applies for a course; department approves or rejects")
public class EnrollmentRequestController {

    private final EnrollmentRequestService enrollmentRequestService;

    @Operation(summary = "Student applies to enroll in a course")
    @PostMapping
    public ResponseEntity<EnrollmentResponseDto> apply(@Valid @RequestBody EnrollmentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentRequestService.apply(request));
    }

    @Operation(summary = "List all enrollment requests")
    @GetMapping
    public ResponseEntity<List<EnrollmentResponseDto>> getAll() {
        return ResponseEntity.ok(enrollmentRequestService.getAll());
    }

    @Operation(summary = "Get one enrollment request by id")
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentRequestService.getById(id));
    }

    @Operation(summary = "Approve a request: increments course.enrolledCount, enrolls the student, "
            + "marks the request approved, and logs a notification — 4 saves in 1 transaction")
    @PostMapping("/{id}/approve")
    public ResponseEntity<EnrollmentResponseDto> approve(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentRequestService.approve(id));
    }

    @Operation(summary = "Reject a request: marks it rejected and logs a notification — 2 saves")
    @PostMapping("/{id}/reject")
    public ResponseEntity<EnrollmentResponseDto> reject(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentRequestService.reject(id));
    }
}
