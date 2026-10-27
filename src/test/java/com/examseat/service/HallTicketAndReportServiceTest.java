package com.examseat.service;

import com.examseat.dto.InvigilatorReportDto;
import com.examseat.entity.*;
import com.examseat.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HallTicketAndReportServiceTest {

    @Mock
    private ExamRepository examRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AllocationRepository allocationRepository;
    @Mock
    private HallTicketRepository hallTicketRepository;
    @Mock
    private HallRepository hallRepository;
    @Mock
    private SeatRepository seatRepository;

    private HallTicketService hallTicketService;
    private ReportService reportService;

    private Exam testExam;
    private Student testStudent;
    private Hall testHall;
    private Seat testSeat;
    private Allocation testAllocation;

    @BeforeEach
    void setUp() {
        hallTicketService = new HallTicketService(examRepository, studentRepository, allocationRepository, hallTicketRepository);
        ReflectionTestUtils.setField(hallTicketService, "collegeName", "Test College of Engineering");
        ReflectionTestUtils.setField(hallTicketService, "collegeAddress", "Campus 1, Metro City");

        reportService = new ReportService(examRepository, hallRepository, seatRepository, allocationRepository);
        ReflectionTestUtils.setField(reportService, "collegeName", "Test College of Engineering");
        ReflectionTestUtils.setField(reportService, "collegeAddress", "Campus 1, Metro City");

        UUID examId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID hallId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();

        testExam = new Exam("Semester Exam Nov 2026", LocalDate.now().plusDays(5), Session.FN,
                LocalTime.of(9, 30), LocalTime.of(12, 30), LocalTime.of(9, 0), "Do not cheat.", "admin");
        testExam.setId(examId);

        testStudent = new Student("CS101", "Aravind Sharma", "Computer Science", 5, "aravind@test.edu");
        testStudent.setId(studentId);

        testHall = new Hall("Auditorium Hall 1", "Main Block", "Ground Floor", 4, 3, 2);
        testHall.setId(hallId);

        testSeat = new Seat(testHall, 1, 1, 1);
        testSeat.setId(seatId);

        testAllocation = new Allocation(testExam, testStudent, testSeat, "admin");
    }

    @Test
    @DisplayName("Hall Ticket Generation: Generates valid OpenPDF document bytes starting with %PDF-")
    void testGenerateHallTicketPdf() {
        when(examRepository.findById(testExam.getId())).thenReturn(Optional.of(testExam));
        when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));
        when(allocationRepository.findByExamIdAndStudentId(testExam.getId(), testStudent.getId()))
                .thenReturn(Optional.of(testAllocation));
        when(hallTicketRepository.findByExamIdAndStudentId(testExam.getId(), testStudent.getId()))
                .thenReturn(Optional.empty());

        byte[] pdfBytes = hallTicketService.generateHallTicketPdf(testExam.getId(), testStudent.getId());

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "PDF byte stream should contain content");

        // Verify PDF Magic Bytes "%PDF-"
        String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 5));
        assertEquals("%PDF-", header);
    }

    @Test
    @DisplayName("Invigilator Report Generation: Computes attendance list, vacant seats, and department breakdown")
    void testInvigilatorReportData() {
        when(examRepository.findById(testExam.getId())).thenReturn(Optional.of(testExam));
        when(hallRepository.findById(testHall.getId())).thenReturn(Optional.of(testHall));

        // 2 seats: Seat 1 is allocated, Seat 2 is vacant
        Seat seat2 = new Seat(testHall, 1, 1, 2);
        seat2.setId(UUID.randomUUID());
        when(seatRepository.findByHallIdOrderByRowNumberAscBenchNumberAscPositionAsc(testHall.getId()))
                .thenReturn(List.of(testSeat, seat2));

        when(allocationRepository.findByExamIdAndHallId(testExam.getId(), testHall.getId()))
                .thenReturn(List.of(testAllocation));

        InvigilatorReportDto report = reportService.getInvigilatorReportData(testExam.getId(), testHall.getId());

        assertNotNull(report);
        assertEquals("Auditorium Hall 1", report.getHallName());
        assertEquals(1, report.getOccupiedSeats());
        assertEquals(1, report.getEmptySeats());
        assertEquals(1, report.getAttendanceList().size());
        assertEquals("CS101", report.getAttendanceList().get(0).getRollNo());
        assertEquals(1, report.getDepartmentSummary().size());
        assertEquals("Computer Science", report.getDepartmentSummary().get(0).getDepartment());
        assertEquals(1, report.getVacantSeatsList().size());
    }

    @Test
    @DisplayName("Invigilator Report PDF & CSV: Generates valid PDF and CSV outputs")
    void testInvigilatorReportPdfAndCsv() throws IOException {
        when(examRepository.findById(testExam.getId())).thenReturn(Optional.of(testExam));
        when(hallRepository.findById(testHall.getId())).thenReturn(Optional.of(testHall));
        when(seatRepository.findByHallIdOrderByRowNumberAscBenchNumberAscPositionAsc(testHall.getId()))
                .thenReturn(List.of(testSeat));
        when(allocationRepository.findByExamIdAndHallId(testExam.getId(), testHall.getId()))
                .thenReturn(List.of(testAllocation));

        // PDF Generation
        byte[] pdfBytes = reportService.generateInvigilatorReportPdf(testExam.getId(), testHall.getId());
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
        assertEquals("%PDF-", new String(pdfBytes, 0, 5));

        // CSV Generation
        byte[] csvBytes = reportService.generateInvigilatorReportCsv(testExam.getId(), testHall.getId());
        assertNotNull(csvBytes);
        String csvString = new String(csvBytes);
        assertTrue(csvString.contains("Sl_No"));
        assertTrue(csvString.contains("CS101"));
        assertTrue(csvString.contains("Aravind Sharma"));
    }
}
