package com.examseat.dto;

import java.util.UUID;

public class ExamHallDto {

    private UUID id;
    private UUID hallId;
    private String hallName;
    private String building;
    private String floor;
    private Integer capacity;

    public ExamHallDto() {
    }

    public ExamHallDto(UUID id, UUID hallId, String hallName, String building, String floor, Integer capacity) {
        this.id = id;
        this.hallId = hallId;
        this.hallName = hallName;
        this.building = building;
        this.floor = floor;
        this.capacity = capacity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getHallId() {
        return hallId;
    }

    public void setHallId(UUID hallId) {
        this.hallId = hallId;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
