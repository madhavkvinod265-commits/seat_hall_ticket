package com.examseat.security;

import com.examseat.entity.Profile;
import com.examseat.repository.ProfileRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ProfileRepository profileRepository;

    public CustomUserDetailsService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Profile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (!Boolean.TRUE.equals(profile.getActive())) {
            throw new UsernameNotFoundException("User account is inactive: " + username);
        }

        String roleWithPrefix = "ROLE_" + profile.getRole().name();
        return new User(
                profile.getUsername(),
                profile.getPasswordHash() != null ? profile.getPasswordHash() : "",
                Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix))
        );
    }
}
