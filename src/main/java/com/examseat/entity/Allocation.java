package com.examseat.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "allocations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_exam_student", columnNames = {"exam_id", "student_id"}),
        @UniqueConstraint(name = "uq_exam_seat", columnNames = {"exam_id", "seat_id"})
    }
)
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "allocated_at")
    private OffsetDateTime allocatedAt;

    @Column(name = "allocated_by", length = 150)
    private String allocatedBy;

    public Allocation() {
    }

    public Allocation(Exam exam, Student student, Seat seat, String allocatedBy) {
        this.exam = exam;
        this.student = student;
        this.seat = seat;
        this.allocatedBy = allocatedBy;
    }

    @PrePersist
    protected void onCreate() {
        this.allocatedAt = OffsetDateTime.now();
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

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public OffsetDateTime getAllocatedAt() {
        return allocatedAt;
    }

    public void setAllocatedAt(OffsetDateTime allocatedAt) {
        this.allocatedAt = allocatedAt;
    }

    public String getAllocatedBy() {
        return allocatedBy;
    }

    public void setAllocatedBy(String allocatedBy) {
        this.allocatedBy = allocatedBy;
    }
}
