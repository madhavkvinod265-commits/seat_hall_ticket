package com.examseat.dto;

import java.util.UUID;

public class SeatDto {

    private UUID id;
    private UUID hallId;
    private String hallName;
    private Integer rowNumber;
    private Integer benchNumber;
    private Integer position;

    public SeatDto() {
    }

    public SeatDto(UUID id, UUID hallId, String hallName, Integer rowNumber, Integer benchNumber, Integer position) {
        this.id = id;
        this.hallId = hallId;
        this.hallName = hallName;
        this.rowNumber = rowNumber;
        this.benchNumber = benchNumber;
        this.position = position;
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
}
