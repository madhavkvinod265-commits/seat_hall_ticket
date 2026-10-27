package com.examseat.dto;

import java.util.UUID;

public class StudentDto {

    private UUID id;
    private String rollNo;
    private String name;
    private String department;
    private Integer semester;
    private String email;
    private Boolean active;

    public StudentDto() {
    }

    public StudentDto(UUID id, String rollNo, String name, String department, Integer semester, String email, Boolean active) {
        this.id = id;
        this.rollNo = rollNo;
        this.name = name;
        this.department = department;
        this.semester = semester;
        this.email = email;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
