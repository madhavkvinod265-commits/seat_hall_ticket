package com.examseat.dto;

public class DepartmentSummaryDto {

    private String department;
    private int studentCount;

    public DepartmentSummaryDto() {
    }

    public DepartmentSummaryDto(String department, int studentCount) {
        this.department = department;
        this.studentCount = studentCount;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }
}
