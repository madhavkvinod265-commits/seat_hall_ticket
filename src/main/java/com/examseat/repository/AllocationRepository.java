package com.examseat.repository;

import com.examseat.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, UUID> {

    List<Allocation> findByExamId(UUID examId);

    @Query("SELECT a FROM Allocation a JOIN a.seat s WHERE a.exam.id = :examId AND s.hall.id = :hallId " +
           "ORDER BY s.rowNumber ASC, s.benchNumber ASC, s.position ASC")
    List<Allocation> findByExamIdAndHallId(@Param("examId") UUID examId, @Param("hallId") UUID hallId);

    Optional<Allocation> findByExamIdAndStudentId(UUID examId, UUID studentId);

    Optional<Allocation> findByExamIdAndSeatId(UUID examId, UUID seatId);

    @Query("SELECT a FROM Allocation a JOIN a.student s WHERE LOWER(s.rollNo) = LOWER(:rollNo) AND a.exam.id = :examId")
    Optional<Allocation> findByStudentRollNoAndExamId(@Param("rollNo") String rollNo, @Param("examId") UUID examId);

    @Query("SELECT a FROM Allocation a JOIN a.student s JOIN a.exam e WHERE LOWER(s.rollNo) = LOWER(:rollNo) " +
           "ORDER BY e.examDate ASC, e.startTime ASC")
    List<Allocation> findByStudentRollNo(@Param("rollNo") String rollNo);

    @Modifying
    @Query("DELETE FROM Allocation a WHERE a.exam.id = :examId")
    void deleteByExamId(@Param("examId") UUID examId);

    long countByExamId(UUID examId);

    @Query("SELECT COUNT(a) FROM Allocation a JOIN a.seat s WHERE a.exam.id = :examId AND s.hall.id = :hallId")
    long countByExamIdAndHallId(@Param("examId") UUID examId, @Param("hallId") UUID hallId);

    @Query("SELECT COUNT(a) > 0 FROM Allocation a WHERE a.seat.hall.id = :hallId AND a.exam.status = com.examseat.entity.ExamStatus.FINALIZED")
    boolean existsFinalizedAllocationForHall(@Param("hallId") UUID hallId);
}
