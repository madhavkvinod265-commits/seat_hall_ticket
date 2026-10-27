package com.examseat.controller;

import com.examseat.dto.ApiResponse;
import com.examseat.dto.ExamDto;
import com.examseat.dto.ExamRequest;
import com.examseat.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExamDto>>> getAllExams() {
        return ResponseEntity.ok(ApiResponse.success(examService.getAllExams()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamDto>> getExamById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(examService.getExamById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExamDto>> createExam(@Valid @RequestBody ExamRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = (auth != null) ? auth.getName() : "admin";
        ExamDto created = examService.createExam(request, createdBy);
        return ResponseEntity.ok(ApiResponse.success("Examination schedule created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamDto>> updateExam(@PathVariable UUID id, @Valid @RequestBody ExamRequest request) {
        ExamDto updated = examService.updateExam(id, request);
        return ResponseEntity.ok(ApiResponse.success("Examination updated successfully", updated));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ExamDto>> cancelExam(@PathVariable UUID id) {
        ExamDto cancelled = examService.cancelExam(id);
        return ResponseEntity.ok(ApiResponse.success("Examination cancelled", cancelled));
    }
}
