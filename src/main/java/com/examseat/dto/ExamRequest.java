package com.examseat.dto;

import com.examseat.entity.Session;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class ExamRequest {

    @NotNull(message = "Exam name is required")
    private String name;

    @NotNull(message = "Exam date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate examDate;

    @NotNull(message = "Session (FN, AN, EV) is required")
    private Session session;

    @NotNull(message = "Start time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime endTime;

    @NotNull(message = "Reporting time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime reportingTime;

    private String instructions;

    @NotEmpty(message = "At least one department and semester cohort must be selected")
    private List<ExamDepartmentDto> departments;

    @NotEmpty(message = "At least one examination hall must be selected")
    private List<UUID> hallIds;

    public ExamRequest() {
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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<ExamDepartmentDto> getDepartments() {
        return departments;
    }

    public void setDepartments(List<ExamDepartmentDto> departments) {
        this.departments = departments;
    }

    public List<UUID> getHallIds() {
        return hallIds;
    }

    public void setHallIds(List<UUID> hallIds) {
        this.hallIds = hallIds;
    }
}
