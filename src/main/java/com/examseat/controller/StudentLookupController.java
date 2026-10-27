package com.examseat.controller;

import com.examseat.dto.ApiResponse;
import com.examseat.dto.StudentLookupResultDto;
import com.examseat.service.LookupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lookup")
public class StudentLookupController {

    private final LookupService lookupService;

    public StudentLookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/seat")
    public ResponseEntity<ApiResponse<List<StudentLookupResultDto>>> lookupSeat(
            @RequestParam String rollNo,
            @RequestParam(required = false) UUID examId) {
        List<StudentLookupResultDto> results = lookupService.lookupSeat(rollNo, examId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
