package com.examseat.repository;

import com.examseat.entity.ExamHall;
import com.examseat.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExamHallRepository extends JpaRepository<ExamHall, UUID> {

    List<ExamHall> findByExamId(UUID examId);

    List<ExamHall> findByHallId(UUID hallId);

    @Modifying
    @Query("DELETE FROM ExamHall eh WHERE eh.exam.id = :examId")
    void deleteByExamId(@Param("examId") UUID examId);

    @Query("SELECT COUNT(eh) > 0 FROM ExamHall eh JOIN eh.exam e " +
           "WHERE eh.hall.id = :hallId " +
           "AND e.examDate = :examDate " +
           "AND e.session = :session " +
           "AND e.status != com.examseat.entity.ExamStatus.CANCELLED " +
           "AND (:excludeExamId IS NULL OR e.id != :excludeExamId)")
    boolean existsOverlappingExamHall(@Param("hallId") UUID hallId,
                                      @Param("examDate") LocalDate examDate,
                                      @Param("session") Session session,
                                      @Param("excludeExamId") UUID excludeExamId);
}
