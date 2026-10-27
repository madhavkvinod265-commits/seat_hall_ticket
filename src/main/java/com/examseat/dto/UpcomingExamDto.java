package com.examseat.dto;

import com.examseat.entity.ExamStatus;
import com.examseat.entity.Session;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class UpcomingExamDto {

    private UUID id;
    private String name;
    private LocalDate examDate;
    private Session session;
    private LocalTime startTime;
    private LocalTime endTime;
    private ExamStatus status;
    private int eligibleStudentsCount;
    private int allocatedStudentsCount;
    private int totalCapacity;

    public UpcomingExamDto() {
    }

    public UpcomingExamDto(UUID id, String name, LocalDate examDate, Session session, LocalTime startTime, LocalTime endTime, ExamStatus status, int eligibleStudentsCount, int allocatedStudentsCount, int totalCapacity) {
        this.id = id;
        this.name = name;
        this.examDate = examDate;
        this.session = session;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.eligibleStudentsCount = eligibleStudentsCount;
        this.allocatedStudentsCount = allocatedStudentsCount;
        this.totalCapacity = totalCapacity;
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

    public ExamStatus getStatus() {
        return status;
    }

    public void setStatus(ExamStatus status) {
        this.status = status;
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

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }
}
