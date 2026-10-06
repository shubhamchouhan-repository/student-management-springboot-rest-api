package com.example.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDto {

    private Long id;
    private String courseCode;
    private String title;
    private String description;
    private Integer credits;
    private DepartmentSummaryDto department;
    private Integer capacity;
    private Integer enrolledCount;
}