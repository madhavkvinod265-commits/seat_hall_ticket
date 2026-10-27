package com.examseat.dto;

public class StudentCsvRow {

    private int rowNumber;
    private String rollNo;
    private String name;
    private String department;
    private Integer semester;
    private boolean valid;
    private String errorMessage;

    public StudentCsvRow() {
    }

    public StudentCsvRow(int rowNumber, String rollNo, String name, String department, Integer semester, boolean valid, String errorMessage) {
        this.rowNumber = rowNumber;
        this.rollNo = rollNo;
        this.name = name;
        this.department = department;
        this.semester = semester;
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
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

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
