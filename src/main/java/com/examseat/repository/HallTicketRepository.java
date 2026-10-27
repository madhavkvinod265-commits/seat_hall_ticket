package com.examseat.repository;

import com.examseat.entity.HallTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HallTicketRepository extends JpaRepository<HallTicket, UUID> {

    Optional<HallTicket> findByExamIdAndStudentId(UUID examId, UUID studentId);

    Optional<HallTicket> findByTicketNumber(String ticketNumber);

    List<HallTicket> findByExamId(UUID examId);

    @Modifying
    @Query("DELETE FROM HallTicket ht WHERE ht.exam.id = :examId")
    void deleteByExamId(@Param("examId") UUID examId);

    long countByExamId(UUID examId);
}
