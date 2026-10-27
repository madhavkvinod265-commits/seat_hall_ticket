package com.examseat.service;

import com.examseat.dto.*;
import com.examseat.entity.*;
import com.examseat.exception.ConflictException;
import com.examseat.exception.InvalidOperationException;
import com.examseat.exception.ResourceNotFoundException;
import com.examseat.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamDepartmentRepository examDepartmentRepository;
    private final ExamHallRepository examHallRepository;
    private final HallRepository hallRepository;
    private final StudentRepository studentRepository;
    private final AllocationRepository allocationRepository;

    public ExamService(ExamRepository examRepository,
                       ExamDepartmentRepository examDepartmentRepository,
                       ExamHallRepository examHallRepository,
                       HallRepository hallRepository,
                       StudentRepository studentRepository,
                       AllocationRepository allocationRepository) {
        this.examRepository = examRepository;
        this.examDepartmentRepository = examDepartmentRepository;
        this.examHallRepository = examHallRepository;
        this.hallRepository = hallRepository;
        this.studentRepository = studentRepository;
        this.allocationRepository = allocationRepository;
    }

    @Transactional(readOnly = true)
    public List<ExamDto> getAllExams() {
        return examRepository.findByOrderByExamDateAscStartTimeAsc().stream()
                .map(this::toDetailedDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExamDto getExamById(UUID id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with ID: " + id));
        return toDetailedDto(exam);
    }

    @Transactional
    public ExamDto createExam(ExamRequest request, String createdBy) {
        validateExamSchedule(request, null);

        Exam exam = new Exam(
                request.getName().trim(),
                request.getExamDate(),
                request.getSession(),
                request.getStartTime(),
                request.getEndTime(),
                request.getReportingTime(),
                request.getInstructions(),
                createdBy
        );
        exam.setStatus(ExamStatus.SCHEDULED);

        Exam savedExam = examRepository.save(exam);

        // Associate departments & semesters
        saveExamDepartments(savedExam, request.getDepartments());

        // Associate halls and verify hall overlap
        saveExamHalls(savedExam, request.getHallIds(), request.getExamDate(), request.getSession());

        return toDetailedDto(savedExam);
    }

    @Transactional
    public ExamDto updateExam(UUID id, ExamRequest request) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with ID: " + id));

        if (exam.getStatus() == ExamStatus.FINALIZED) {
            throw new InvalidOperationException("Cannot modify an examination with status 'FINALIZED'. To make changes, an administrator must reopen the seating plan first.");
        }

        validateExamSchedule(request, id);

        exam.setName(request.getName().trim());
        exam.setExamDate(request.getExamDate());
        exam.setSession(request.getSession());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setReportingTime(request.getReportingTime());
        exam.setInstructions(request.getInstructions());

        // Update departments
        examDepartmentRepository.deleteByExamId(id);
        saveExamDepartments(exam, request.getDepartments());

        // Update halls
        examHallRepository.deleteByExamId(id);
        saveExamHalls(exam, request.getHallIds(), request.getExamDate(), request.getSession());

        // If seating was generated previously, reset allocations to ensure consistency
        if (exam.getStatus() == ExamStatus.SEATING_GENERATED) {
            allocationRepository.deleteByExamId(id);
            exam.setStatus(ExamStatus.SCHEDULED);
        }

        Exam updated = examRepository.save(exam);
        return toDetailedDto(updated);
    }

    @Transactional
    public ExamDto cancelExam(UUID id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with ID: " + id));

        exam.setStatus(ExamStatus.CANCELLED);
        allocationRepository.deleteByExamId(id);
        Exam updated = examRepository.save(exam);
        return toDetailedDto(updated);
    }

    private void validateExamSchedule(ExamRequest request, UUID excludeExamId) {
        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new InvalidOperationException("Examination start time must be strictly before end time.");
        }

        if (request.getReportingTime().isAfter(request.getStartTime())) {
            throw new InvalidOperationException("Reporting time must be before or equal to examination start time.");
        }

        // Validate hall overlap: same hall cannot be assigned to another exam on the same date and session
        for (UUID hallId : request.getHallIds()) {
            boolean hasOverlap = examHallRepository.existsOverlappingExamHall(
                    hallId, request.getExamDate(), request.getSession(), excludeExamId);
            if (hasOverlap) {
                Hall hall = hallRepository.findById(hallId).orElse(null);
                String hallName = (hall != null) ? hall.getName() : hallId.toString();
                throw new ConflictException("Hall '" + hallName + "' is already scheduled for another examination on " +
                        request.getExamDate() + " during session " + request.getSession() + ".");
            }
        }
    }

    private void saveExamDepartments(Exam exam, List<ExamDepartmentDto> deptDtos) {
        List<ExamDepartment> depts = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (ExamDepartmentDto dto : deptDtos) {
            String key = dto.getDepartment().trim().toLowerCase() + "_" + dto.getSemester();
            if (!seen.contains(key)) {
                seen.add(key);
                depts.add(new ExamDepartment(exam, dto.getDepartment().trim(), dto.getSemester()));
            }
        }
        examDepartmentRepository.saveAll(depts);
    }

    private void saveExamHalls(Exam exam, List<UUID> hallIds, LocalDate examDate, Session session) {
        List<ExamHall> examHalls = new ArrayList<>();
        for (UUID hallId : hallIds) {
            Hall hall = hallRepository.findById(hallId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hall not found with ID: " + hallId));
            examHalls.add(new ExamHall(exam, hall));
        }
        examHallRepository.saveAll(examHalls);
    }

    @Transactional(readOnly = true)
    public ExamDto toDetailedDto(Exam exam) {
        ExamDto dto = new ExamDto();
        dto.setId(exam.getId());
        dto.setName(exam.getName());
        dto.setExamDate(exam.getExamDate());
        dto.setSession(exam.getSession());
        dto.setStartTime(exam.getStartTime());
        dto.setEndTime(exam.getEndTime());
        dto.setReportingTime(exam.getReportingTime());
        dto.setStatus(exam.getStatus());
        dto.setInstructions(exam.getInstructions());
        dto.setCreatedBy(exam.getCreatedBy());

        // Fetch departments
        List<ExamDepartment> depts = examDepartmentRepository.findByExamId(exam.getId());
        dto.setDepartments(depts.stream()
                .map(d -> new ExamDepartmentDto(d.getId(), d.getDepartment(), d.getSemester()))
                .collect(Collectors.toList()));

        // Fetch halls
        List<ExamHall> examHalls = examHallRepository.findByExamId(exam.getId());
        int totalCapacity = 0;
        List<ExamHallDto> hallDtos = new ArrayList<>();
        for (ExamHall eh : examHalls) {
            Hall h = eh.getHall();
            totalCapacity += h.getCapacity();
            hallDtos.add(new ExamHallDto(eh.getId(), h.getId(), h.getName(), h.getBuilding(), h.getFloor(), h.getCapacity()));
        }
        dto.setHalls(hallDtos);
        dto.setTotalCapacity(totalCapacity);

        // Calculate eligible students count
        int eligibleStudentsCount = 0;
        for (ExamDepartment d : depts) {
            List<Student> students = studentRepository.findByDepartmentAndSemesterAndActiveTrueOrderByRollNoAsc(
                    d.getDepartment(), d.getSemester());
            eligibleStudentsCount += students.size();
        }
        dto.setEligibleStudentsCount(eligibleStudentsCount);

        // Fetch allocated count
        long allocatedCount = allocationRepository.countByExamId(exam.getId());
        dto.setAllocatedStudentsCount((int) allocatedCount);
        dto.setSeatingGenerated(allocatedCount > 0);

        return dto;
    }
}
