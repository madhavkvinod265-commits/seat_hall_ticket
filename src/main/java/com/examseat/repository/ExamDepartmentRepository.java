package com.examseat.repository;

import com.examseat.entity.ExamDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamDepartmentRepository extends JpaRepository<ExamDepartment, UUID> {

    List<ExamDepartment> findByExamId(UUID examId);

    @Modifying
    @Query("DELETE FROM ExamDepartment ed WHERE ed.exam.id = :examId")
    void deleteByExamId(@Param("examId") UUID examId);
}
