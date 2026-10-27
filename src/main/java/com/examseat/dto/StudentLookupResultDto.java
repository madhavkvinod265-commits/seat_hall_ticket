package com.examseat.dto;

import com.examseat.entity.Session;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class StudentLookupResultDto {

    private UUID studentId;
    private String rollNo;
    private String studentName;
    private String department;
    private Integer semester;

    private UUID examId;
    private String examName;
    private LocalDate examDate;
    private Session session;
    private LocalTime reportingTime;
    private LocalTime startTime;
    private LocalTime endTime;

    private boolean allocated;
    private String hallName;
    private String building;
    private String floor;
    private Integer rowNumber;
    private Integer benchNumber;
    private Integer position;
    private String instructions;
    private boolean hallTicketAvailable;

    public StudentLookupResultDto() {
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

    public LocalTime getReportingTime() {
        return reportingTime;
    }

    public void setReportingTime(LocalTime reportingTime) {
        this.reportingTime = reportingTime;
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

    public boolean isAllocated() {
        return allocated;
    }

    public void setAllocated(boolean allocated) {
        this.allocated = allocated;
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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public boolean isHallTicketAvailable() {
        return hallTicketAvailable;
    }

    public void setHallTicketAvailable(boolean hallTicketAvailable) {
        this.hallTicketAvailable = hallTicketAvailable;
    }
}
