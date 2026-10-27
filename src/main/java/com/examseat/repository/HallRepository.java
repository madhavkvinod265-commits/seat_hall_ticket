package com.examseat.repository;

import com.examseat.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HallRepository extends JpaRepository<Hall, UUID> {

    Optional<Hall> findByName(String name);

    boolean existsByName(String name);

    List<Hall> findByActiveTrueOrderByNameAsc();

    long countByActiveTrue();

    @Query("SELECT COALESCE(SUM(h.capacity), 0) FROM Hall h WHERE h.active = true")
    Integer getTotalActiveCapacity();
}
