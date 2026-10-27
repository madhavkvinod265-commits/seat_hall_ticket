package com.examseat.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
    name = "exam_departments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_exam_dept_sem", columnNames = {"exam_id", "department", "semester"})
    }
)
public class ExamDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false)
    private Integer semester;

    public ExamDepartment() {
    }

    public ExamDepartment(Exam exam, String department, Integer semester) {
        this.exam = exam;
        this.department = department;
        this.semester = semester;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }
}
