package com.examseat.service;

import com.examseat.dto.LoginRequest;
import com.examseat.dto.LoginResponse;
import com.examseat.dto.RegisterUserRequest;
import com.examseat.dto.UserProfileDto;
import com.examseat.entity.Profile;
import com.examseat.entity.Role;
import com.examseat.exception.ConflictException;
import com.examseat.exception.ResourceNotFoundException;
import com.examseat.exception.UnauthorizedException;
import com.examseat.repository.ProfileRepository;
import com.examseat.security.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Safely bootstrap default administrator account if no admin exists
     */
    @PostConstruct
    @Transactional
    public void bootstrapDefaultAdmin() {
        try {
            if (profileRepository.countByRole(Role.ADMINISTRATOR) == 0) {
                log.info("No administrator account detected. Bootstrapping default administrator...");
                Profile admin = new Profile(
                        "admin",
                        passwordEncoder.encode("Admin@123"),
                        "System Administrator",
                        "admin@examseat.edu",
                        Role.ADMINISTRATOR
                );
                profileRepository.save(admin);
                log.info("Default administrator created successfully (Username: admin).");
            }
        } catch (Exception e) {
            log.warn("Database not yet reachable during bootstrap: {}. Will be checked on demand.", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Profile profile = profileRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!Boolean.TRUE.equals(profile.getActive())) {
            throw new UnauthorizedException("Account is disabled. Please contact the administrator.");
        }

        if (profile.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), profile.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = jwtTokenProvider.generateToken(
                profile.getId(),
                profile.getUsername(),
                profile.getRole(),
                profile.getDisplayName()
        );

        return new LoginResponse(
                token,
                profile.getId(),
                profile.getUsername(),
                profile.getDisplayName(),
                profile.getEmail(),
                profile.getRole()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfileByUsername(String username) {
        Profile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return toDto(profile);
    }

    @Transactional(readOnly = true)
    public List<UserProfileDto> getAllUsers() {
        return profileRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileDto registerUser(RegisterUserRequest request) {
        if (profileRepository.existsByUsername(request.getUsername().trim())) {
            throw new ConflictException("Username '" + request.getUsername() + "' is already in use");
        }

        Profile profile = new Profile(
                request.getUsername().trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getDisplayName().trim(),
                request.getEmail() != null ? request.getEmail().trim() : null,
                request.getRole()
        );

        Profile saved = profileRepository.save(profile);
        return toDto(saved);
    }

    @Transactional
    public void deactivateUser(UUID id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        if (profile.getUsername().equalsIgnoreCase("admin")) {
            throw new ConflictException("Cannot deactivate primary administrator account");
        }
        profile.setActive(false);
        profileRepository.save(profile);
    }

    private UserProfileDto toDto(Profile profile) {
        return new UserProfileDto(
                profile.getId(),
                profile.getUsername(),
                profile.getDisplayName(),
                profile.getEmail(),
                profile.getRole(),
                profile.getActive()
        );
    }
}
