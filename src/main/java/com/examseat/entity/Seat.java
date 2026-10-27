package com.examseat.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "seats",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_hall_row_bench_position", columnNames = {"hall_id", "row_number", "bench_number", "position"})
    }
)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Column(name = "bench_number", nullable = false)
    private Integer benchNumber;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    public Seat() {
    }

    public Seat(Hall hall, Integer rowNumber, Integer benchNumber, Integer position) {
        this.hall = hall;
        this.rowNumber = rowNumber;
        this.benchNumber = benchNumber;
        this.position = position;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Hall getHall() {
        return hall;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Integer getBenchNumber() {
        return benchNumber;
    }

    public void setBenchNumber(Integer benchNumber) {
        this.benchNumber = benchNumber;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
