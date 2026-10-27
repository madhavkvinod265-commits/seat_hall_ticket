package com.examseat.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
    name = "exam_halls",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_exam_hall", columnNames = {"exam_id", "hall_id"})
    }
)
public class ExamHall {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    public ExamHall() {
    }

    public ExamHall(Exam exam, Hall hall) {
        this.exam = exam;
        this.hall = hall;
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

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }
}
