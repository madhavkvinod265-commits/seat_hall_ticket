package com.examseat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HallRequest {

    @NotBlank(message = "Hall name is required")
    private String name;

    @NotBlank(message = "Building name is required")
    private String building;

    @NotBlank(message = "Floor is required")
    private String floor;

    @NotNull(message = "Number of rows is required")
    @Min(value = 1, message = "Rows must be at least 1")
    private Integer rows;

    @NotNull(message = "Benches per row is required")
    @Min(value = 1, message = "Benches per row must be at least 1")
    private Integer benchesPerRow;

    @NotNull(message = "Seats per bench is required")
    @Min(value = 1, message = "Seats per bench must be at least 1")
    private Integer seatsPerBench;

    private Boolean active = true;

    public HallRequest() {
    }

    public HallRequest(String name, String building, String floor, Integer rows, Integer benchesPerRow, Integer seatsPerBench, Boolean active) {
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.rows = rows;
        this.benchesPerRow = benchesPerRow;
        this.seatsPerBench = seatsPerBench;
        this.active = active;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
