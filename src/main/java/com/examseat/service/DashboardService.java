package com.examseat.service;

import com.examseat.dto.DashboardStatsDto;
import com.examseat.dto.UpcomingExamDto;
import com.examseat.entity.*;
import com.examseat.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardService {

    private final HallRepository hallRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final ExamDepartmentRepository examDepartmentRepository;
    private final ExamHallRepository examHallRepository;
    private final AllocationRepository allocationRepository;

    public DashboardService(HallRepository hallRepository,
                            StudentRepository studentRepository,
                            ExamRepository examRepository,
                            ExamDepartmentRepository examDepartmentRepository,
                            ExamHallRepository examHallRepository,
                            AllocationRepository allocationRepository) {
        this.hallRepository = hallRepository;
        this.studentRepository = studentRepository;
        this.examRepository = examRepository;
        this.examDepartmentRepository = examDepartmentRepository;
        this.examHallRepository = examHallRepository;
        this.allocationRepository = allocationRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStatistics() {
        DashboardStatsDto stats = new DashboardStatsDto();

        // 1. Total Halls & Total Available Seats
        stats.setTotalHalls(hallRepository.countByActiveTrue());
        Integer totalCap = hallRepository.getTotalActiveCapacity();
        stats.setTotalAvailableSeats(totalCap != null ? totalCap : 0);

        // 2. Total Students
        stats.setTotalStudents(studentRepository.countByActiveTrue());

        // 3. Exam counts
        stats.setTotalScheduledExams(examRepository.countByStatus(ExamStatus.SCHEDULED));
        stats.setCompletedSeatingPlans(examRepository.countByStatus(ExamStatus.FINALIZED));

        // 4. Upcoming exams (today or future)
        LocalDate today = LocalDate.now();
        List<Exam> upcomingList = examRepository.findUpcomingExams(today);
        List<UpcomingExamDto> upcomingDtos = new ArrayList<>();

        int totalOccupiedInUpcoming = 0;
        int totalVacantInUpcoming = 0;
        int unassignedInDrafts = 0;

        for (Exam e : upcomingList) {
            List<ExamDepartment> depts = examDepartmentRepository.findByExamId(e.getId());
            int eligibleCount = 0;
            for (ExamDepartment d : depts) {
                eligibleCount += studentRepository.findByDepartmentAndSemesterAndActiveTrueOrderByRollNoAsc(
                        d.getDepartment(), d.getSemester()).size();
            }

            List<ExamHall> examHalls = examHallRepository.findByExamId(e.getId());
            int examCapacity = examHalls.stream().mapToInt(eh -> eh.getHall().getCapacity()).sum();
            int allocatedCount = (int) allocationRepository.countByExamId(e.getId());

            totalOccupiedInUpcoming += allocatedCount;
            totalVacantInUpcoming += Math.max(0, examCapacity - allocatedCount);

            if (e.getStatus() == ExamStatus.SCHEDULED || e.getStatus() == ExamStatus.DRAFT) {
                unassignedInDrafts += Math.max(0, eligibleCount - allocatedCount);
            }

            upcomingDtos.add(new UpcomingExamDto(
                    e.getId(),
                    e.getName(),
                    e.getExamDate(),
                    e.getSession(),
                    e.getStartTime(),
                    e.getEndTime(),
                    e.getStatus(),
                    eligibleCount,
                    allocatedCount,
                    examCapacity
            ));
        }

        stats.setUpcomingExams(upcomingDtos);
        stats.setTotalOccupiedSeatsInUpcoming(totalOccupiedInUpcoming);
        stats.setTotalVacantSeatsInUpcoming(totalVacantInUpcoming);
        stats.setUnassignedStudentsInDrafts(unassignedInDrafts);

        // 5. Department Distribution
        List<Object[]> deptCounts = studentRepository.countStudentsByDepartment();
        Map<String, Long> deptMap = new LinkedHashMap<>();
        for (Object[] row : deptCounts) {
            String dept = (String) row[0];
            Long count = (Long) row[1];
            deptMap.put(dept, count);
        }
        stats.setDepartmentDistribution(deptMap);

        // 6. Hall Utilization
        List<Hall> activeHalls = hallRepository.findByActiveTrueOrderByNameAsc();
        Map<String, Integer> hallCapacities = new LinkedHashMap<>();
        for (Hall h : activeHalls) {
            hallCapacities.put(h.getName(), h.getCapacity());
        }
        stats.setHallUtilization(hallCapacities);

        return stats;
    }
}
