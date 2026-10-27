package com.examseat.controller;

import com.examseat.dto.*;
import com.examseat.service.SeatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exams/{id}/seating")
public class SeatingController {

    private final SeatingService seatingService;

    public SeatingController(SeatingService seatingService) {
        this.seatingService = seatingService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<SeatingGenerationResultDto>> generateSeatingPlan(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String allocatedBy = (auth != null) ? auth.getName() : "staff";
        SeatingGenerationResultDto result = seatingService.generateSeatingPlan(id, allocatedBy);
        return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
    }

    @RequestMapping(value = "/verify", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<ApiResponse<SeatingVerificationDto>> verifySeatingPlan(@PathVariable UUID id) {
        SeatingVerificationDto verification = seatingService.verifySeatingPlan(id);
        return ResponseEntity.ok(ApiResponse.success(verification));
    }

    @PostMapping("/finalize")
    public ResponseEntity<ApiResponse<ExamDto>> finalizeSeatingPlan(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String finalizedBy = (auth != null) ? auth.getName() : "admin";
        ExamDto finalized = seatingService.finalizeSeatingPlan(id, finalizedBy);
        return ResponseEntity.ok(ApiResponse.success("Seating plan finalized and locked successfully", finalized));
    }

    @PostMapping("/reopen")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ExamDto>> reopenSeatingPlan(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String admin = (auth != null) ? auth.getName() : "admin";
        ExamDto reopened = seatingService.reopenSeatingPlan(id, admin);
        return ResponseEntity.ok(ApiResponse.success("Seating plan unlocked for modifications", reopened));
    }

    @GetMapping("/halls/{hallId}")
    public ResponseEntity<ApiResponse<HallSeatingGridDto>> getHallSeatingGrid(@PathVariable UUID id, @PathVariable UUID hallId) {
        HallSeatingGridDto grid = seatingService.getHallSeatingGrid(id, hallId);
        return ResponseEntity.ok(ApiResponse.success(grid));
    }
}
