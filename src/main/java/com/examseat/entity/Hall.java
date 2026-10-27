package com.examseat.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "halls")
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String building;

    @Column(nullable = false, length = 50)
    private String floor;

    @Column(name = "rows", nullable = false)
    private Integer rows;

    @Column(name = "benches_per_row", nullable = false)
    private Integer benchesPerRow;

    @Column(name = "seats_per_bench", nullable = false)
    private Integer seatsPerBench;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Seat> seats = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Hall() {
    }

    public Hall(String name, String building, String floor, Integer rows, Integer benchesPerRow, Integer seatsPerBench) {
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.rows = rows;
        this.benchesPerRow = benchesPerRow;
        this.seatsPerBench = seatsPerBench;
        this.capacity = calculateCapacity(rows, benchesPerRow, seatsPerBench);
        this.active = true;
    }

    public static int calculateCapacity(Integer rows, Integer benchesPerRow, Integer seatsPerBench) {
        if (rows == null || benchesPerRow == null || seatsPerBench == null) {
            return 0;
        }
        return rows * benchesPerRow * seatsPerBench;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        if (this.capacity == null || this.capacity == 0) {
            this.capacity = calculateCapacity(this.rows, this.benchesPerRow, this.seatsPerBench);
        }
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
        this.capacity = calculateCapacity(this.rows, this.benchesPerRow, this.seatsPerBench);
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
        this.capacity = calculateCapacity(this.rows, this.benchesPerRow, this.seatsPerBench);
    }

    public Integer getBenchesPerRow() {
        return benchesPerRow;
    }

    public void setBenchesPerRow(Integer benchesPerRow) {
        this.benchesPerRow = benchesPerRow;
        this.capacity = calculateCapacity(this.rows, this.benchesPerRow, this.seatsPerBench);
    }

    public Integer getSeatsPerBench() {
        return seatsPerBench;
    }

    public void setSeatsPerBench(Integer seatsPerBench) {
        this.seatsPerBench = seatsPerBench;
        this.capacity = calculateCapacity(this.rows, this.benchesPerRow, this.seatsPerBench);
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
