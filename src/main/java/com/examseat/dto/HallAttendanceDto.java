package com.examseat.dto;

public class HallAttendanceDto {

    private int slNo;
    private int rowNumber;
    private int benchNumber;
    private int position;
    private String rollNo;
    private String studentName;
    private String department;
    private Integer semester;
    private String signature; // Placeholder for invigilator attendance signature

    public HallAttendanceDto() {
    }

    public HallAttendanceDto(int slNo, int rowNumber, int benchNumber, int position, String rollNo, String studentName, String department, Integer semester) {
        this.slNo = slNo;
        this.rowNumber = rowNumber;
        this.benchNumber = benchNumber;
        this.position = position;
        this.rollNo = rollNo;
        this.studentName = studentName;
        this.department = department;
        this.semester = semester;
    }

    public int getSlNo() {
        return slNo;
    }

    public void setSlNo(int slNo) {
        this.slNo = slNo;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getBenchNumber() {
        return benchNumber;
    }

    public void setBenchNumber(int benchNumber) {
        this.benchNumber = benchNumber;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
