package com.example.sms.dto;


import com.example.sms.entity.enums.EnrollmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponseDto {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private EnrollmentStatus status;
    private LocalDateTime requestedAt;
}
