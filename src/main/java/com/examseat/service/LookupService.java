package com.examseat.service;

import com.examseat.dto.StudentLookupResultDto;
import com.examseat.entity.Allocation;
import com.examseat.entity.Exam;
import com.examseat.entity.ExamStatus;
import com.examseat.entity.Seat;
import com.examseat.entity.Student;
import com.examseat.exception.ResourceNotFoundException;
import com.examseat.repository.AllocationRepository;
import com.examseat.repository.ExamRepository;
import com.examseat.repository.HallTicketRepository;
import com.examseat.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LookupService {

    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final AllocationRepository allocationRepository;
    private final HallTicketRepository hallTicketRepository;

    public LookupService(StudentRepository studentRepository,
                         ExamRepository examRepository,
                         AllocationRepository allocationRepository,
                         HallTicketRepository hallTicketRepository) {
        this.studentRepository = studentRepository;
        this.examRepository = examRepository;
        this.allocationRepository = allocationRepository;
        this.hallTicketRepository = hallTicketRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentLookupResultDto> lookupSeat(String rollNo, UUID examId) {
        if (rollNo == null || rollNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Roll number is required for seat lookup.");
        }

        String trimmedRollNo = rollNo.trim().toUpperCase();
        Student student = studentRepository.findByRollNoIgnoreCase(trimmedRollNo)
                .orElseThrow(() -> new ResourceNotFoundException("No registered student found with roll number: " + rollNo));

        List<StudentLookupResultDto> results = new ArrayList<>();

        if (examId != null) {
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new ResourceNotFoundException("Examination not found with ID: " + examId));
            Optional<Allocation> allocOpt = allocationRepository.findByExamIdAndStudentId(examId, student.getId());
            results.add(mapToLookupResult(student, exam, allocOpt.orElse(null)));
        } else {
            // Find all allocations for this student
            List<Allocation> allocations = allocationRepository.findByStudentRollNo(trimmedRollNo);
            if (allocations.isEmpty()) {
                // If not allocated yet, check if there are scheduled exams matching this student's department & semester
                List<Exam> upcomingExams = examRepository.findUpcomingExams(java.time.LocalDate.now());
                for (Exam exam : upcomingExams) {
                    results.add(mapToLookupResult(student, exam, null));
                }
            } else {
                for (Allocation alloc : allocations) {
                    results.add(mapToLookupResult(student, alloc.getExam(), alloc));
                }
            }
        }

        return results;
    }

    private StudentLookupResultDto mapToLookupResult(Student student, Exam exam, Allocation allocation) {
        StudentLookupResultDto dto = new StudentLookupResultDto();
        dto.setStudentId(student.getId());
        dto.setRollNo(student.getRollNo());
        dto.setStudentName(student.getName());
        dto.setDepartment(student.getDepartment());
        dto.setSemester(student.getSemester());

        dto.setExamId(exam.getId());
        dto.setExamName(exam.getName());
        dto.setExamDate(exam.getExamDate());
        dto.setSession(exam.getSession());
        dto.setReportingTime(exam.getReportingTime());
        dto.setStartTime(exam.getStartTime());
        dto.setEndTime(exam.getEndTime());
        dto.setInstructions(exam.getInstructions());

        if (allocation != null) {
            dto.setAllocated(true);
            Seat seat = allocation.getSeat();
            dto.setHallName(seat.getHall().getName());
            dto.setBuilding(seat.getHall().getBuilding());
            dto.setFloor(seat.getHall().getFloor());
            dto.setRowNumber(seat.getRowNumber());
            dto.setBenchNumber(seat.getBenchNumber());
            dto.setPosition(seat.getPosition());
            // Available if seating generated or finalized
            dto.setHallTicketAvailable(exam.getStatus() == ExamStatus.SEATING_GENERATED || exam.getStatus() == ExamStatus.FINALIZED);
        } else {
            dto.setAllocated(false);
            dto.setHallTicketAvailable(false);
        }

        return dto;
    }
}
