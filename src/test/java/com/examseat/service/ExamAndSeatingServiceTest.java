package com.examseat.service;

import com.examseat.algorithm.InterleavedSeatingAlgorithm;
import com.examseat.dto.ExamDepartmentDto;
import com.examseat.dto.ExamDto;
import com.examseat.dto.ExamRequest;
import com.examseat.dto.SeatingGenerationResultDto;
import com.examseat.entity.*;
import com.examseat.exception.ConflictException;
import com.examseat.exception.InsufficientCapacityException;
import com.examseat.exception.InvalidOperationException;
import com.examseat.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamAndSeatingServiceTest {

    @Mock
    private ExamRepository examRepository;
    @Mock
    private ExamDepartmentRepository examDepartmentRepository;
    @Mock
    private ExamHallRepository examHallRepository;
    @Mock
    private HallRepository hallRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private AllocationRepository allocationRepository;
    @Mock
    private HallTicketRepository hallTicketRepository;

    private ExamService examService;
    private SeatingService seatingService;
    private InterleavedSeatingAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new InterleavedSeatingAlgorithm();
        examService = new ExamService(examRepository, examDepartmentRepository, examHallRepository,
                hallRepository, studentRepository, allocationRepository);
        seatingService = new SeatingService(examRepository, examDepartmentRepository, examHallRepository,
                studentRepository, seatRepository, allocationRepository, hallTicketRepository, algorithm);
    }

    @Test
    @DisplayName("Exam Schedule: Prevents start time after end time")
    void testExamInvalidTimings() {
        ExamRequest req = new ExamRequest();
        req.setName("Midterms");
        req.setExamDate(LocalDate.now().plusDays(2));
        req.setSession(Session.FN);
        req.setStartTime(LocalTime.of(12, 0));
        req.setEndTime(LocalTime.of(10, 0)); // invalid
        req.setReportingTime(LocalTime.of(9, 30));
        req.setDepartments(List.of(new ExamDepartmentDto("CS", 5)));
        req.setHallIds(List.of(UUID.randomUUID()));

        assertThrows(InvalidOperationException.class, () -> examService.createExam(req, "admin"));
    }

    @Test
    @DisplayName("Exam Overlap: Prevents overlapping examinations in same hall on same date and session")
    void testPreventOverlappingHallUsage() {
        UUID hallId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(3);

        ExamRequest req = new ExamRequest();
        req.setName("Physics 101");
        req.setExamDate(date);
        req.setSession(Session.FN);
        req.setStartTime(LocalTime.of(9, 30));
        req.setEndTime(LocalTime.of(12, 30));
        req.setReportingTime(LocalTime.of(9, 0));
        req.setDepartments(List.of(new ExamDepartmentDto("CS", 5)));
        req.setHallIds(List.of(hallId));

        when(examHallRepository.existsOverlappingExamHall(hallId, date, Session.FN, null)).thenReturn(true);
        Hall mockHall = new Hall("Auditorium", "Main", "1", 5, 4, 2);
        when(hallRepository.findById(hallId)).thenReturn(Optional.of(mockHall));

        ConflictException ex = assertThrows(ConflictException.class, () -> examService.createExam(req, "admin"));
        assertTrue(ex.getMessage().contains("already scheduled"));
    }

    @Test
    @DisplayName("Seating Generation: Throws InsufficientCapacityException and rolls back when capacity is exceeded")
    void testSeatingInsufficientCapacityRollback() {
        UUID examId = UUID.randomUUID();
        Exam exam = new Exam("Semester Exam", LocalDate.now().plusDays(4), Session.FN,
                LocalTime.of(9, 30), LocalTime.of(12, 30), LocalTime.of(9, 0), "Rules", "admin");
        exam.setId(examId);

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

        ExamDepartment ed = new ExamDepartment(exam, "Computer Science", 5);
        when(examDepartmentRepository.findByExamId(examId)).thenReturn(List.of(ed));

        // 10 eligible students
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Student s = new Student("CS00" + i, "Student " + i, "Computer Science", 5, null);
            s.setId(UUID.randomUUID());
            students.add(s);
        }
        when(studentRepository.findByDepartmentAndSemesterAndActiveTrueOrderByRollNoAsc("Computer Science", 5))
                .thenReturn(students);

        // Only 1 hall with 4 seats (2 x 1 x 2)
        UUID hallId = UUID.randomUUID();
        Hall hall = new Hall("Tiny Room", "Block C", "1", 2, 1, 2);
        hall.setId(hallId);
        ExamHall eh = new ExamHall(exam, hall);
        when(examHallRepository.findByExamId(examId)).thenReturn(List.of(eh));

        List<Seat> seats = new ArrayList<>();
        for (int r = 1; r <= 2; r++) {
            for (int p = 1; p <= 2; p++) {
                Seat seat = new Seat(hall, r, 1, p);
                seat.setId(UUID.randomUUID());
                seats.add(seat);
            }
        }
        when(seatRepository.findByHallIdInOrderByHallNameAndCoordinates(List.of(hallId))).thenReturn(seats);

        InsufficientCapacityException ex = assertThrows(InsufficientCapacityException.class, () ->
                seatingService.generateSeatingPlan(examId, "staff"));

        assertEquals(10, ex.getRequiredSeats());
        assertEquals(4, ex.getAvailableSeats());
        assertEquals(6, ex.getExcessStudents());
        verify(allocationRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Exam Finalization: Locks arrangement and prevents modifications")
    void testExamFinalizationLock() {
        UUID examId = UUID.randomUUID();
        Exam exam = new Exam("Finals", LocalDate.now().plusDays(4), Session.AN,
                LocalTime.of(14, 0), LocalTime.of(17, 0), LocalTime.of(13, 30), "Rules", "admin");
        exam.setId(examId);
        exam.setStatus(ExamStatus.FINALIZED);

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));

        // Cannot update finalized exam
        ExamRequest updateReq = new ExamRequest();
        assertThrows(InvalidOperationException.class, () -> examService.updateExam(examId, updateReq));

        // Cannot regenerate seating for finalized exam directly without reopening
        assertThrows(InvalidOperationException.class, () -> seatingService.generateSeatingPlan(examId, "staff"));
    }

    @Test
    @DisplayName("Admin Reopen: Unlocks finalized exam allowing safe audited regeneration")
    void testAdminReopenWorkflow() {
        UUID examId = UUID.randomUUID();
        Exam exam = new Exam("Finals", LocalDate.now().plusDays(4), Session.AN,
                LocalTime.of(14, 0), LocalTime.of(17, 0), LocalTime.of(13, 30), "Rules", "admin");
        exam.setId(examId);
        exam.setStatus(ExamStatus.FINALIZED);

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExamDto reopened = seatingService.reopenSeatingPlan(examId, "admin");

        assertNotNull(reopened);
        assertEquals(ExamStatus.SEATING_GENERATED, reopened.getStatus());
        verify(examRepository, times(1)).save(exam);
    }
}
