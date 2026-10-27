package com.examseat.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SeatingVerificationDto {

    private UUID examId;
    private String examName;
    private int totalEligibleStudents;
    private int totalHallCapacity;
    private int totalAllocatedStudents;
    private int unassignedStudents;
    private int duplicateStudentAllocations;
    private int duplicateSeatAllocations;
    private boolean coordinatesConsistent;
    private boolean valid;
    private Map<String, Integer> departmentDistribution = new HashMap<>();
    private List<String> warnings = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public SeatingVerificationDto() {
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

    public int getTotalHallCapacity() {
        return totalHallCapacity;
    }

    public void setTotalHallCapacity(int totalHallCapacity) {
        this.totalHallCapacity = totalHallCapacity;
    }

    public int getTotalAllocatedStudents() {
        return totalAllocatedStudents;
    }

    public void setTotalAllocatedStudents(int totalAllocatedStudents) {
        this.totalAllocatedStudents = totalAllocatedStudents;
    }

    public int getUnassignedStudents() {
        return unassignedStudents;
    }

    public void setUnassignedStudents(int unassignedStudents) {
        this.unassignedStudents = unassignedStudents;
    }

    public int getDuplicateStudentAllocations() {
        return duplicateStudentAllocations;
    }

    public void setDuplicateStudentAllocations(int duplicateStudentAllocations) {
        this.duplicateStudentAllocations = duplicateStudentAllocations;
    }

    public int getDuplicateSeatAllocations() {
        return duplicateSeatAllocations;
    }

    public void setDuplicateSeatAllocations(int duplicateSeatAllocations) {
        this.duplicateSeatAllocations = duplicateSeatAllocations;
    }

    public boolean isCoordinatesConsistent() {
        return coordinatesConsistent;
    }

    public void setCoordinatesConsistent(boolean coordinatesConsistent) {
        this.coordinatesConsistent = coordinatesConsistent;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Map<String, Integer> getDepartmentDistribution() {
        return departmentDistribution;
    }

    public void setDepartmentDistribution(Map<String, Integer> departmentDistribution) {
        this.departmentDistribution = departmentDistribution;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
