package com.examseat.dto;

import com.examseat.entity.Role;
import java.util.UUID;

public class UserProfileDto {

    private UUID id;
    private String username;
    private String displayName;
    private String email;
    private Role role;
    private Boolean active;

    public UserProfileDto() {
    }

    public UserProfileDto(UUID id, String username, String displayName, String email, Role role, Boolean active) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
