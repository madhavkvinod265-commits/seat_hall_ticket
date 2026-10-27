package com.examseat.controller;

import com.examseat.dto.ApiResponse;
import com.examseat.dto.RegisterUserRequest;
import com.examseat.dto.UserProfileDto;
import com.examseat.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(authService.getAllUsers()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserProfileDto>> createUser(@Valid @RequestBody RegisterUserRequest request) {
        UserProfileDto created = authService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success("User created successfully", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        authService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }
}
