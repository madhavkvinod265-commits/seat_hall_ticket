package com.examseat.repository;

import com.examseat.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByRollNo(String rollNo);

    Optional<Student> findByRollNoIgnoreCase(String rollNo);

    boolean existsByRollNo(String rollNo);

    List<Student> findByDepartmentAndSemesterAndActiveTrueOrderByRollNoAsc(String department, Integer semester);

    List<Student> findByActiveTrueOrderByRollNoAsc();

    @Query("SELECT s FROM Student s WHERE (:dept IS NULL OR s.department = :dept) " +
           "AND (:sem IS NULL OR s.semester = :sem) " +
           "AND (CAST(:search AS string) IS NULL OR LOWER(s.rollNo) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<Student> searchStudents(@Param("search") String search,
                                @Param("dept") String department,
                                @Param("sem") Integer semester,
                                Pageable pageable);

    @Query("SELECT s.department, COUNT(s) FROM Student s WHERE s.active = true GROUP BY s.department")
    List<Object[]> countStudentsByDepartment();

    long countByActiveTrue();
}
