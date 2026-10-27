package com.examseat.dto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SeatingGenerationResultDto {

    private UUID examId;
    private String examName;
    private int totalEligibleStudents;
    private int totalAvailableCapacity;
    private int totalAllocatedStudents;
    private int unassignedCount;
    private boolean finalized;
    private OffsetDateTime generatedAt;
    private String generatedBy;
    private String message;
    private List<HallSeatingGridDto> hallGrids = new ArrayList<>();

    public SeatingGenerationResultDto() {
        this.generatedAt = OffsetDateTime.now();
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

    public int getTotalEligibleStudents() {
        return totalEligibleStudents;
    }

    public void setTotalEligibleStudents(int totalEligibleStudents) {
        this.totalEligibleStudents = totalEligibleStudents;
    }

    public int getTotalAvailableCapacity() {
        return totalAvailableCapacity;
    }

    public void setTotalAvailableCapacity(int totalAvailableCapacity) {
        this.totalAvailableCapacity = totalAvailableCapacity;
    }

    public int getTotalAllocatedStudents() {
        return totalAllocatedStudents;
    }

    public void setTotalAllocatedStudents(int totalAllocatedStudents) {
        this.totalAllocatedStudents = totalAllocatedStudents;
    }

    public int getUnassignedCount() {
        return unassignedCount;
    }

    public void setUnassignedCount(int unassignedCount) {
        this.unassignedCount = unassignedCount;
    }

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(OffsetDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<HallSeatingGridDto> getHallGrids() {
        return hallGrids;
    }

    public void setHallGrids(List<HallSeatingGridDto> hallGrids) {
        this.hallGrids = hallGrids;
    }
}
