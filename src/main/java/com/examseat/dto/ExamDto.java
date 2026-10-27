package com.examseat.dto;

import com.examseat.entity.ExamStatus;
import com.examseat.entity.Session;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExamDto {

    private UUID id;
    private String name;
    private LocalDate examDate;
    private Session session;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime reportingTime;
    private ExamStatus status;
    private String instructions;
    private String createdBy;

    private List<ExamDepartmentDto> departments = new ArrayList<>();
    private List<ExamHallDto> halls = new ArrayList<>();

    private int totalCapacity;
    private int eligibleStudentsCount;
    private int allocatedStudentsCount;
    private boolean seatingGenerated;

    public ExamDto() {
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

    public LocalTime getReportingTime() {
        return reportingTime;
    }

    public void setReportingTime(LocalTime reportingTime) {
        this.reportingTime = reportingTime;
    }

    public ExamStatus getStatus() {
        return status;
    }

    public void setStatus(ExamStatus status) {
        this.status = status;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<ExamDepartmentDto> getDepartments() {
        return departments;
    }

    public void setDepartments(List<ExamDepartmentDto> departments) {
        this.departments = departments;
    }

    public List<ExamHallDto> getHalls() {
        return halls;
    }

    public void setHalls(List<ExamHallDto> halls) {
        this.halls = halls;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public int getEligibleStudentsCount() {
        return eligibleStudentsCount;
    }

    public void setEligibleStudentsCount(int eligibleStudentsCount) {
        this.eligibleStudentsCount = eligibleStudentsCount;
    }

    public int getAllocatedStudentsCount() {
        return allocatedStudentsCount;
    }

    public void setAllocatedStudentsCount(int allocatedStudentsCount) {
        this.allocatedStudentsCount = allocatedStudentsCount;
    }

    public boolean isSeatingGenerated() {
        return seatingGenerated;
    }

    public void setSeatingGenerated(boolean seatingGenerated) {
        this.seatingGenerated = seatingGenerated;
    }
}
