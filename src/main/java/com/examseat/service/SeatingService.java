package com.examseat.service;

import com.examseat.algorithm.InterleavedSeatingAlgorithm;
import com.examseat.dto.*;
import com.examseat.entity.*;
import com.examseat.exception.ConflictException;
import com.examseat.exception.InvalidOperationException;
import com.examseat.exception.ResourceNotFoundException;
import com.examseat.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatingService {

    private final ExamRepository examRepository;
    private final ExamDepartmentRepository examDepartmentRepository;
    private final ExamHallRepository examHallRepository;
    private final StudentRepository studentRepository;
    private final SeatRepository seatRepository;
    private final AllocationRepository allocationRepository;
    private final HallTicketRepository hallTicketRepository;
    private final InterleavedSeatingAlgorithm algorithm;

    public SeatingService(ExamRepository examRepository,
                          ExamDepartmentRepository examDepartmentRepository,
                          ExamHallRepository examHallRepository,
                          StudentRepository studentRepository,
                          SeatRepository seatRepository,
                          AllocationRepository allocationRepository,
                          HallTicketRepository hallTicketRepository,
                          InterleavedSeatingAlgorithm algorithm) {
        this.examRepository = examRepository;
        this.examDepartmentRepository = examDepartmentRepository;
        this.examHallRepository = examHallRepository;
        this.studentRepository = studentRepository;
        this.seatRepository = seatRepository;
        this.allocationRepository = allocationRepository;
        this.hallTicketRepository = hallTicketRepository;
        this.algorithm = algorithm;
    }

    /**
     * Generates the deterministic interleaved seating plan transactionally
     */
    @Transactional
    public SeatingGenerationResultDto generateSeatingPlan(UUID examId, String allocatedBy) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with ID: " + examId));

        if (exam.getStatus() == ExamStatus.FINALIZED) {
            throw new InvalidOperationException("This examination seating plan has already been finalized. Reopen it with administrator privileges to regenerate.");
        }

        // 1. Gather all eligible students
        List<ExamDepartment> depts = examDepartmentRepository.findByExamId(examId);
        if (depts.isEmpty()) {
            throw new InvalidOperationException("No departments or semesters are configured for this examination.");
        }

        List<Student> eligibleStudents = new ArrayList<>();
        Set<UUID> seenStudentIds = new HashSet<>();
        for (ExamDepartment ed : depts) {
            List<Student> students = studentRepository.findByDepartmentAndSemesterAndActiveTrueOrderByRollNoAsc(
                    ed.getDepartment(), ed.getSemester());
            for (Student s : students) {
                if (!seenStudentIds.contains(s.getId())) {
                    seenStudentIds.add(s.getId());
                    eligibleStudents.add(s);
                }
            }
        }

        // 2. Gather all seats from selected exam halls
        List<ExamHall> examHalls = examHallRepository.findByExamId(examId);
        if (examHalls.isEmpty()) {
            throw new InvalidOperationException("No examination halls are assigned to this examination.");
        }

        List<UUID> hallIds = examHalls.stream().map(eh -> eh.getHall().getId()).collect(Collectors.toList());
        List<Seat> availableSeats = seatRepository.findByHallIdInOrderByHallNameAndCoordinates(hallIds);

        // 3. Clear existing draft allocations and tickets for this exam
        allocationRepository.deleteByExamId(examId);
        hallTicketRepository.deleteByExamId(examId);

        // 4. Run Interleaved Round-Robin Seating Algorithm
        InterleavedSeatingAlgorithm.AllocationResult result = algorithm.allocateSeats(
                exam, eligibleStudents, availableSeats, allocatedBy);

        // 5. Persist allocations in database
        allocationRepository.saveAll(result.getAllocations());

        // 6. Update examination status
        exam.setStatus(ExamStatus.SEATING_GENERATED);
        examRepository.save(exam);

        // 7. Build and return result DTO
        SeatingGenerationResultDto resultDto = new SeatingGenerationResultDto();
        resultDto.setExamId(exam.getId());
        resultDto.setExamName(exam.getName());
        resultDto.setTotalEligibleStudents(result.getTotalEligibleStudents());
        resultDto.setTotalAvailableCapacity(result.getTotalAvailableSeats());
        resultDto.setTotalAllocatedStudents(result.getAllocatedCount());
        resultDto.setUnassignedCount(result.getUnassignedCount());
        resultDto.setFinalized(false);
        resultDto.setGeneratedBy(allocatedBy);
        resultDto.setMessage(String.format("Successfully allocated all %d students across %d examination halls.",
                result.getAllocatedCount(), examHalls.size()));

        // Populate grids for each hall
        for (ExamHall eh : examHalls) {
            resultDto.getHallGrids().add(getHallSeatingGrid(examId, eh.getHall().getId()));
        }

        return resultDto;
    }

    /**
     * Rigorously verifies the generated seating plan against all academic integrity checks
     */
    @Transactional(readOnly = true)
    public SeatingVerificationDto verifySeatingPlan(UUID examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with ID: " + examId));

        SeatingVerificationDto verification = new SeatingVerificationDto();
        verification.setExamId(exam.getId());
        verification.setExamName(exam.getName());

        // Eligible students
        List<ExamDepartment> depts = examDepartmentRepository.findByExamId(examId);
        List<Student> eligibleStudents = new ArrayList<>();
        Set<UUID> seenStudentIds = new HashSet<>();
        for (ExamDepartment ed : depts) {
            List<Student> list = studentRepository.findByDepartmentAndSemesterAndActiveTrueOrderByRollNoAsc(
                    ed.getDepartment(), ed.getSemester());
            for (Student s : list) {
                if (!seenStudentIds.contains(s.getId())) {
                    seenStudentIds.add(s.getId());
                    eligibleStudents.add(s);
                }
            }
        }
        verification.setTotalEligibleStudents(eligibleStudents.size());

        // Total Hall Capacity
        List<ExamHall> examHalls = examHallRepository.findByExamId(examId);
        int totalCapacity = examHalls.stream().mapToInt(eh -> eh.getHall().getCapacity()).sum();
        verification.setTotalHallCapacity(totalCapacity);

        // Actual Allocations in database
        List<Allocation> allocations = allocationRepository.findByExamId(examId);
        verification.setTotalAllocatedStudents(allocations.size());
        verification.setUnassignedStudents(Math.max(0, eligibleStudents.size() - allocations.size()));

        // Check duplicate students
        Set<UUID> uniqueAllocatedStudents = new HashSet<>();
        int dupStudents = 0;
        for (Allocation a : allocations) {
            if (!uniqueAllocatedStudents.add(a.getStudent().getId())) {
                dupStudents++;
            }
        }
        verification.setDuplicateStudentAllocations(dupStudents);

        // Check duplicate seats
        Set<UUID> uniqueAllocatedSeats = new HashSet<>();
        int dupSeats = 0;
        for (Allocation a : allocations) {
            if (!uniqueAllocatedSeats.add(a.getSeat().getId())) {
                dupSeats++;
            }
        }
        verification.setDuplicateSeatAllocations(dupSeats);

        // Department distribution
        Map<String, Integer> deptDist = new HashMap<>();
        for (Allocation a : allocations) {
            String dept = a.getStudent().getDepartment();
            deptDist.put(dept, deptDist.getOrDefault(dept, 0) + 1);
        }
        verification.setDepartmentDistribution(deptDist);

        // Coordinates consistency
        boolean coordsValid = true;
        for (Allocation a : allocations) {
            Seat s = a.getSeat();
            if (s.getRowNumber() <= 0 || s.getBenchNumber() <= 0 || s.getPosition() <= 0) {
                coordsValid = false;
                break;
            }
        }
        verification.setCoordinatesConsistent(coordsValid);

        // Overall validity evaluation
        boolean valid = true;
        if (allocations.size() != eligibleStudents.size()) {
            valid = false;
            verification.getErrors().add("Mismatch between eligible students (" + eligibleStudents.size() +
                    ") and allocated seats (" + allocations.size() + ")");
        }
        if (dupStudents > 0) {
            valid = false;
            verification.getErrors().add(dupStudents + " duplicate student allocation(s) detected!");
        }
        if (dupSeats > 0) {
            valid = false;
            verification.getErrors().add(dupSeats + " duplicate seat allocation(s) detected!");
        }
        if (!coordsValid) {
            valid = false;
            verification.getErrors().add("Invalid seat coordinates detected in one or more allocations.");
        }

        if (totalCapacity < eligibleStudents.size()) {
            valid = false;
            verification.getErrors().add("Hall capacity (" + totalCapacity + ") is smaller than student count (" +
                    eligibleStudents.size() + ")");
        }

        verification.setValid(valid);
        return verification;
    }

    /**
     * Finalizes the seating plan, locking it from further alterations
     */
    @Transactional
    public ExamDto finalizeSeatingPlan(UUID examId, String finalizedBy) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with ID: " + examId));

        if (exam.getStatus() == ExamStatus.FINALIZED) {
            throw new InvalidOperationException("Seating plan is already finalized.");
        }

        long allocatedCount = allocationRepository.countByExamId(examId);
        if (allocatedCount == 0) {
            throw new InvalidOperationException("Cannot finalize: No seating arrangement has been generated for this examination.");
        }

        // Run verification before finalizing
        SeatingVerificationDto verification = verifySeatingPlan(examId);
        if (!verification.isValid()) {
            throw new InvalidOperationException("Cannot finalize seating plan: Verification failed with errors: " +
                    String.join(", ", verification.getErrors()));
        }

        exam.setStatus(ExamStatus.FINALIZED);
        Exam saved = examRepository.save(exam);

        return toExamDto(saved);
    }

    /**
     * Explicit workflow to reopen a finalized plan by an administrator
     */
    @Transactional
    public ExamDto reopenSeatingPlan(UUID examId, String adminUsername) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found with ID: " + examId));

        if (exam.getStatus() != ExamStatus.FINALIZED && exam.getStatus() != ExamStatus.SEATING_GENERATED) {
            throw new InvalidOperationException("Examination is not in a finalized state.");
        }

        // Reopen back to SEATING_GENERATED so staff can edit or regenerate
        exam.setStatus(ExamStatus.SEATING_GENERATED);
        Exam saved = examRepository.save(exam);

        return toExamDto(saved);
    }

    /**
     * Builds the visual seating grid representation for a specific hall in an exam
     */
    @Transactional(readOnly = true)
    public HallSeatingGridDto getHallSeatingGrid(UUID examId, UUID hallId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));
        
        List<Seat> seats = seatRepository.findByHallIdOrderByRowNumberAscBenchNumberAscPositionAsc(hallId);
        if (seats.isEmpty()) {
            throw new ResourceNotFoundException("No seats found for hall ID: " + hallId);
        }

        Hall hall = seats.get(0).getHall();
        List<Allocation> allocations = allocationRepository.findByExamIdAndHallId(examId, hallId);

        // Map seatId -> Allocation
        Map<UUID, Allocation> allocBySeat = new HashMap<>();
        for (Allocation a : allocations) {
            allocBySeat.put(a.getSeat().getId(), a);
        }

        HallSeatingGridDto grid = new HallSeatingGridDto(
                hall.getId(), hall.getName(), hall.getBuilding(), hall.getFloor(),
                hall.getRows(), hall.getBenchesPerRow(), hall.getSeatsPerBench(), hall.getCapacity()
        );

        // Group seats by bench: key = "row_bench"
        Map<String, BenchSeatingDto> benchMap = new LinkedHashMap<>();
        int occupied = 0;

        for (Seat seat : seats) {
            String bKey = seat.getRowNumber() + "_" + seat.getBenchNumber();
            BenchSeatingDto bench = benchMap.computeIfAbsent(bKey, k -> new BenchSeatingDto(seat.getRowNumber(), seat.getBenchNumber()));

            SeatAllocationDetailDto seatDto = new SeatAllocationDetailDto(
                    seat.getId(), hall.getId(), hall.getName(),
                    seat.getRowNumber(), seat.getBenchNumber(), seat.getPosition()
            );

            Allocation alloc = allocBySeat.get(seat.getId());
            if (alloc != null) {
                occupied++;
                seatDto.setOccupied(true);
                seatDto.setStudentId(alloc.getStudent().getId());
                seatDto.setRollNo(alloc.getStudent().getRollNo());
                seatDto.setStudentName(alloc.getStudent().getName());
                seatDto.setDepartment(alloc.getStudent().getDepartment());
                seatDto.setSemester(alloc.getStudent().getSemester());
            }

            bench.getSeats().add(seatDto);
        }

        grid.setOccupiedSeats(occupied);
        grid.setEmptySeats(hall.getCapacity() - occupied);
        grid.setBenches(new ArrayList<>(benchMap.values()));

        return grid;
    }

    private ExamDto toExamDto(Exam exam) {
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
        return dto;
    }
}
