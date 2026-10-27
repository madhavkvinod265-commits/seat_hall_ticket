package com.examseat.dto;

import com.examseat.entity.Session;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InvigilatorReportDto {

    private String collegeName;
    private String collegeAddress;
    private UUID examId;
    private String examName;
    private LocalDate examDate;
    private Session session;
    private LocalTime startTime;
    private LocalTime endTime;

    private UUID hallId;
    private String hallName;
    private String building;
    private String floor;
    private int totalCapacity;
    private int occupiedSeats;
    private int emptySeats;

    private List<HallAttendanceDto> attendanceList = new ArrayList<>();
    private List<DepartmentSummaryDto> departmentSummary = new ArrayList<>();
    private List<String> vacantSeatsList = new ArrayList<>();

    public InvigilatorReportDto() {
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getCollegeAddress() {
        return collegeAddress;
    }

    public void setCollegeAddress(String collegeAddress) {
        this.collegeAddress = collegeAddress;
    }

    public UUID getExamId() {
        return examId;
    }

    public void setExamId(UUID examId) {
        this.examId = examId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
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

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public int getOccupiedSeats() {
        return occupiedSeats;
    }

    public void setOccupiedSeats(int occupiedSeats) {
        this.occupiedSeats = occupiedSeats;
    }

    public int getEmptySeats() {
        return emptySeats;
    }

    public void setEmptySeats(int emptySeats) {
        this.emptySeats = emptySeats;
    }

    public List<HallAttendanceDto> getAttendanceList() {
        return attendanceList;
    }

    public void setAttendanceList(List<HallAttendanceDto> attendanceList) {
        this.attendanceList = attendanceList;
    }

    public List<DepartmentSummaryDto> getDepartmentSummary() {
        return departmentSummary;
    }

    public void setDepartmentSummary(List<DepartmentSummaryDto> departmentSummary) {
        this.departmentSummary = departmentSummary;
    }

    public List<String> getVacantSeatsList() {
        return vacantSeatsList;
    }

    public void setVacantSeatsList(List<String> vacantSeatsList) {
        this.vacantSeatsList = vacantSeatsList;
    }
}
