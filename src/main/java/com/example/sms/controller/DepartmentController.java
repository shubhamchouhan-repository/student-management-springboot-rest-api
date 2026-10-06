package com.example.sms.controller;

import com.example.sms.dto.DepartmentRequestDto;
import com.example.sms.dto.DepartmentResponseDto;

import com.example.sms.service.DepartmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Department", description = "Department CRUD")
public class DepartmentController {

    private final DepartmentService departmentService;

    // CREATE
    @Operation(summary = "Create a department")
    @PostMapping
    public ResponseEntity<DepartmentResponseDto> create(@Valid @RequestBody DepartmentRequestDto departmentRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(departmentRequestDto));
    }

    // GET ALL
    @Operation(summary = "List all departments (cached)")
    @GetMapping
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }


    // GET BY ID
    @Operation(summary = "Get a department by id (cached)")
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }


    // UPDATE
    @Operation(summary = "Update a department")
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponseDto> update(
            @PathVariable Long id, @Valid @RequestBody DepartmentRequestDto departmentRequestDto) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, departmentRequestDto));
    }


    // DELETE
    @Operation(summary = "Delete a department (cascades to its courses)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}