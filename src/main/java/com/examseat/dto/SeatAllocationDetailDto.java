package com.examseat.dto;

import java.util.UUID;

public class SeatAllocationDetailDto {

    private UUID seatId;
    private UUID hallId;
    private String hallName;
    private int rowNumber;
    private int benchNumber;
    private int position;

    private boolean occupied;
    private UUID studentId;
    private String rollNo;
    private String studentName;
    private String department;
    private Integer semester;

    public SeatAllocationDetailDto() {
    }

    public SeatAllocationDetailDto(UUID seatId, UUID hallId, String hallName, int rowNumber, int benchNumber, int position) {
        this.seatId = seatId;
        this.hallId = hallId;
        this.hallName = hallName;
        this.rowNumber = rowNumber;
        this.benchNumber = benchNumber;
        this.position = position;
        this.occupied = false;
    }

    public UUID getSeatId() {
        return seatId;
    }

    public void setSeatId(UUID seatId) {
        this.seatId = seatId;
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }
}
