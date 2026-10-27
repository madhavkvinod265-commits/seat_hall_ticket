package com.examseat.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "hall_tickets",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_ticket_student_exam", columnNames = {"student_id", "exam_id"})
    }
)
public class HallTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 100)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ticket_data", columnDefinition = "jsonb")
    private String ticketData;

    @Column(name = "generated_at")
    private OffsetDateTime generatedAt;

    public HallTicket() {
    }

    public HallTicket(String ticketNumber, Student student, Exam exam, String ticketData) {
        this.ticketNumber = ticketNumber;
        this.student = student;
        this.exam = exam;
        this.ticketData = ticketData;
    }

    @PrePersist
    protected void onCreate() {
        this.generatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public String getTicketData() {
        return ticketData;
    }

    public void setTicketData(String ticketData) {
        this.ticketData = ticketData;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(OffsetDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
