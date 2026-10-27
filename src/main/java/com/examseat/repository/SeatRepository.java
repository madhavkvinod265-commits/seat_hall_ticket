package com.examseat.repository;

import com.examseat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findByHallIdOrderByRowNumberAscBenchNumberAscPositionAsc(UUID hallId);

    @Query("SELECT s FROM Seat s JOIN s.hall h WHERE h.id IN :hallIds ORDER BY h.name ASC, s.rowNumber ASC, s.benchNumber ASC, s.position ASC")
    List<Seat> findByHallIdInOrderByHallNameAndCoordinates(@Param("hallIds") List<UUID> hallIds);

    @Modifying
    @Query("DELETE FROM Seat s WHERE s.hall.id = :hallId")
    void deleteByHallId(@Param("hallId") UUID hallId);

    long countByHallId(UUID hallId);
}
