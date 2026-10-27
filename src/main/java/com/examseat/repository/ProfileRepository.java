package com.examseat.repository;

import com.examseat.entity.Profile;
import com.examseat.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUsername(String username);
    boolean existsByUsername(String username);
    List<Profile> findByRole(Role role);
    long countByRole(Role role);
}
