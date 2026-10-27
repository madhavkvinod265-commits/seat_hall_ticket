package com.examseat.dto;

import java.util.UUID;

public class ExamDepartmentDto {

    private UUID id;
    private String department;
    private Integer semester;

    public ExamDepartmentDto() {
    }

    public ExamDepartmentDto(UUID id, String department, Integer semester) {
        this.id = id;
        this.department = department;
        this.semester = semester;
    }

    public ExamDepartmentDto(String department, Integer semester) {
        this.department = department;
        this.semester = semester;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
