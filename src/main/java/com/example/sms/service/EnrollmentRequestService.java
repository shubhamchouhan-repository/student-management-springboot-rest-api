package com.example.sms.service;

import com.example.sms.dto.EnrollmentRequestDto;
import com.example.sms.dto.EnrollmentResponseDto;

import java.util.List;

public interface EnrollmentRequestService {

    EnrollmentResponseDto apply(EnrollmentRequestDto request);

    EnrollmentResponseDto getById(Long id);

    List<EnrollmentResponseDto> getAll();

    EnrollmentResponseDto approve(Long id);

    EnrollmentResponseDto reject(Long id);
}