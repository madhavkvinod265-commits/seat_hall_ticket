package com.examseat.controller;

import com.examseat.dto.ApiResponse;
import com.examseat.dto.LoginRequest;
import com.examseat.dto.LoginResponse;
import com.examseat.dto.UserProfileDto;
import com.examseat.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }
        UserProfileDto profile = authService.getProfileByUsername(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile() {
        return getCurrentUser();
    }
}
