package com.examseat.repository;

import com.examseat.entity.Exam;
import com.examseat.entity.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {

    List<Exam> findByOrderByExamDateAscStartTimeAsc();

    List<Exam> findByStatus(ExamStatus status);

    @Query("SELECT e FROM Exam e WHERE e.examDate >= :currentDate ORDER BY e.examDate ASC, e.startTime ASC")
    List<Exam> findUpcomingExams(@Param("currentDate") LocalDate currentDate);

    long countByStatus(ExamStatus status);
}
