package com.examseat.controller;

import com.examseat.dto.ApiResponse;
import com.examseat.dto.HallDto;
import com.examseat.dto.HallLayoutDto;
import com.examseat.dto.HallRequest;
import com.examseat.service.HallService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/halls")
public class HallController {

    private final HallService hallService;

    public HallController(HallService hallService) {
        this.hallService = hallService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HallDto>>> getAllHalls() {
        return ResponseEntity.ok(ApiResponse.success(hallService.getAllHalls()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<HallDto>>> getActiveHalls() {
        return ResponseEntity.ok(ApiResponse.success(hallService.getActiveHalls()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HallDto>> getHallById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(hallService.getHallById(id)));
    }

    @GetMapping("/{id}/layout")
    public ResponseEntity<ApiResponse<HallLayoutDto>> getHallLayout(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(hallService.getHallLayout(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HallDto>> createHall(@Valid @RequestBody HallRequest request) {
        HallDto created = hallService.createHall(request);
        return ResponseEntity.ok(ApiResponse.success("Examination hall created successfully with physical seats", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HallDto>> updateHall(@PathVariable UUID id, @Valid @RequestBody HallRequest request) {
        HallDto updated = hallService.updateHall(id, request);
        return ResponseEntity.ok(ApiResponse.success("Examination hall updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHall(@PathVariable UUID id) {
        hallService.deleteHall(id);
        return ResponseEntity.ok(ApiResponse.success("Examination hall deleted successfully", null));
    }
}
