package com.examseat.dto;

import java.util.List;
import java.util.UUID;

public class HallLayoutDto {

    private UUID hallId;
    private String hallName;
    private String building;
    private String floor;
    private int rows;
    private int benchesPerRow;
    private int seatsPerBench;
    private int capacity;
    private List<SeatDto> seats;

    public HallLayoutDto() {
    }

    public HallLayoutDto(UUID hallId, String hallName, String building, String floor, int rows, int benchesPerRow, int seatsPerBench, int capacity, List<SeatDto> seats) {
        this.hallId = hallId;
        this.hallName = hallName;
        this.building = building;
        this.floor = floor;
        this.rows = rows;
        this.benchesPerRow = benchesPerRow;
        this.seatsPerBench = seatsPerBench;
        this.capacity = capacity;
        this.seats = seats;
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

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getBenchesPerRow() {
        return benchesPerRow;
    }

    public void setBenchesPerRow(int benchesPerRow) {
        this.benchesPerRow = benchesPerRow;
    }

    public int getSeatsPerBench() {
        return seatsPerBench;
    }

    public void setSeatsPerBench(int seatsPerBench) {
        this.seatsPerBench = seatsPerBench;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<SeatDto> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatDto> seats) {
        this.seats = seats;
    }
}
