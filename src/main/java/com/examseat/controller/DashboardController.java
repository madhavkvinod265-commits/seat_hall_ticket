package com.examseat.controller;

import com.examseat.dto.ApiResponse;
import com.examseat.dto.DashboardStatsDto;
import com.examseat.dto.UpcomingExamDto;
import com.examseat.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
        DashboardStatsDto stats = dashboardService.getDashboardStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/upcoming-exams")
    public ResponseEntity<ApiResponse<List<UpcomingExamDto>>> getUpcomingExams() {
        DashboardStatsDto stats = dashboardService.getDashboardStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats.getUpcomingExams()));
    }
}
