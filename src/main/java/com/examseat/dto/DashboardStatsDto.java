package com.examseat.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardStatsDto {

    private long totalHalls;
    private int totalAvailableSeats;
    private long totalStudents;
    private long totalScheduledExams;
    private long completedSeatingPlans;
    private int unassignedStudentsInDrafts;
    private int totalOccupiedSeatsInUpcoming;
    private int totalVacantSeatsInUpcoming;

    private List<UpcomingExamDto> upcomingExams = new ArrayList<>();
    private Map<String, Long> departmentDistribution = new HashMap<>();
    private Map<String, Integer> hallUtilization = new HashMap<>();

    public DashboardStatsDto() {
    }

    public long getTotalHalls() {
        return totalHalls;
    }

    public void setTotalHalls(long totalHalls) {
        this.totalHalls = totalHalls;
    }

    public int getTotalAvailableSeats() {
        return totalAvailableSeats;
    }

    public void setTotalAvailableSeats(int totalAvailableSeats) {
        this.totalAvailableSeats = totalAvailableSeats;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalScheduledExams() {
        return totalScheduledExams;
    }

    public void setTotalScheduledExams(long totalScheduledExams) {
        this.totalScheduledExams = totalScheduledExams;
    }

    public long getCompletedSeatingPlans() {
        return completedSeatingPlans;
    }

    public void setCompletedSeatingPlans(long completedSeatingPlans) {
        this.completedSeatingPlans = completedSeatingPlans;
    }

    public int getUnassignedStudentsInDrafts() {
        return unassignedStudentsInDrafts;
    }

    public void setUnassignedStudentsInDrafts(int unassignedStudentsInDrafts) {
        this.unassignedStudentsInDrafts = unassignedStudentsInDrafts;
    }

    public int getTotalOccupiedSeatsInUpcoming() {
        return totalOccupiedSeatsInUpcoming;
    }

    public void setTotalOccupiedSeatsInUpcoming(int totalOccupiedSeatsInUpcoming) {
        this.totalOccupiedSeatsInUpcoming = totalOccupiedSeatsInUpcoming;
    }

    public int getTotalVacantSeatsInUpcoming() {
        return totalVacantSeatsInUpcoming;
    }

    public void setTotalVacantSeatsInUpcoming(int totalVacantSeatsInUpcoming) {
        this.totalVacantSeatsInUpcoming = totalVacantSeatsInUpcoming;
    }

    public List<UpcomingExamDto> getUpcomingExams() {
        return upcomingExams;
    }

    public void setUpcomingExams(List<UpcomingExamDto> upcomingExams) {
        this.upcomingExams = upcomingExams;
    }

    public Map<String, Long> getDepartmentDistribution() {
        return departmentDistribution;
    }

    public void setDepartmentDistribution(Map<String, Long> departmentDistribution) {
        this.departmentDistribution = departmentDistribution;
    }

    public Map<String, Integer> getHallUtilization() {
        return hallUtilization;
    }

    public void setHallUtilization(Map<String, Integer> hallUtilization) {
        this.hallUtilization = hallUtilization;
    }
}
