package com.examseat.dto;

import java.util.ArrayList;
import java.util.List;

public class BenchSeatingDto {

    private int rowNumber;
    private int benchNumber;
    private List<SeatAllocationDetailDto> seats = new ArrayList<>();

    public BenchSeatingDto() {
    }

    public BenchSeatingDto(int rowNumber, int benchNumber) {
        this.rowNumber = rowNumber;
        this.benchNumber = benchNumber;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getBenchNumber() {
        return benchNumber;
    }

    public void setBenchNumber(int benchNumber) {
        this.benchNumber = benchNumber;
    }

    public List<SeatAllocationDetailDto> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatAllocationDetailDto> seats) {
        this.seats = seats;
    }
}
