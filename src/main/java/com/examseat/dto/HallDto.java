package com.examseat.dto;

import java.util.UUID;

public class HallDto {

    private UUID id;
    private String name;
    private String building;
    private String floor;
    private Integer rows;
    private Integer benchesPerRow;
    private Integer seatsPerBench;
    private Integer capacity;
    private Boolean active;

    public HallDto() {
    }

    public HallDto(UUID id, String name, String building, String floor, Integer rows, Integer benchesPerRow, Integer seatsPerBench, Integer capacity, Boolean active) {
        this.id = id;
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.rows = rows;
        this.benchesPerRow = benchesPerRow;
        this.seatsPerBench = seatsPerBench;
        this.capacity = capacity;
        this.active = active;
    }

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
    }

    public Integer getBenchesPerRow() {
        return benchesPerRow;
    }

    public void setBenchesPerRow(Integer benchesPerRow) {
        this.benchesPerRow = benchesPerRow;
    }

    public Integer getSeatsPerBench() {
        return seatsPerBench;
    }

    public void setSeatsPerBench(Integer seatsPerBench) {
        this.seatsPerBench = seatsPerBench;
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
}
