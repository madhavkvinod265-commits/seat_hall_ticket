package com.examseat.service;

import com.examseat.dto.*;
import com.examseat.entity.*;
import com.examseat.exception.ResourceNotFoundException;
import com.examseat.repository.AllocationRepository;
import com.examseat.repository.ExamRepository;
import com.examseat.repository.HallRepository;
import com.examseat.repository.SeatRepository;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ExamRepository examRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final AllocationRepository allocationRepository;

    @Value("${college.name:ExamSeat Institute of Technology & Management}")
    private String collegeName;

    @Value("${college.address:Knowledge Park Campus, Innovation Road, Metro City - 600001}")
    private String collegeAddress;

    public ReportService(ExamRepository examRepository,
                         HallRepository hallRepository,
                         SeatRepository seatRepository,
                         AllocationRepository allocationRepository) {
        this.examRepository = examRepository;
        this.hallRepository = hallRepository;
        this.seatRepository = seatRepository;
        this.allocationRepository = allocationRepository;
    }

    @Transactional(readOnly = true)
    public InvigilatorReportDto getInvigilatorReportData(UUID examId, UUID hallId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found: " + hallId));

        List<Seat> seats = seatRepository.findByHallIdOrderByRowNumberAscBenchNumberAscPositionAsc(hallId);
        List<Allocation> allocations = allocationRepository.findByExamIdAndHallId(examId, hallId);

        Map<UUID, Allocation> allocBySeat = new HashMap<>();
        for (Allocation a : allocations) {
            allocBySeat.put(a.getSeat().getId(), a);
        }

        InvigilatorReportDto report = new InvigilatorReportDto();
        report.setCollegeName(collegeName);
        report.setCollegeAddress(collegeAddress);
        report.setExamId(exam.getId());
        report.setExamName(exam.getName());
        report.setExamDate(exam.getExamDate());
        report.setSession(exam.getSession());
        report.setStartTime(exam.getStartTime());
        report.setEndTime(exam.getEndTime());

        report.setHallId(hall.getId());
        report.setHallName(hall.getName());
        report.setBuilding(hall.getBuilding());
        report.setFloor(hall.getFloor());
        report.setTotalCapacity(hall.getCapacity());

        int slNo = 1;
        List<HallAttendanceDto> attendanceList = new ArrayList<>();
        Map<String, Integer> deptCounts = new TreeMap<>();
        List<String> vacantSeats = new ArrayList<>();

        for (Seat seat : seats) {
            Allocation a = allocBySeat.get(seat.getId());
            if (a != null) {
                Student s = a.getStudent();
                attendanceList.add(new HallAttendanceDto(
                        slNo++,
                        seat.getRowNumber(),
                        seat.getBenchNumber(),
                        seat.getPosition(),
                        s.getRollNo(),
                        s.getName(),
                        s.getDepartment(),
                        s.getSemester()
                ));
                deptCounts.put(s.getDepartment(), deptCounts.getOrDefault(s.getDepartment(), 0) + 1);
            } else {
                vacantSeats.add("Row " + seat.getRowNumber() + ", Bench " + seat.getBenchNumber() + ", Seat " + seat.getPosition());
            }
        }

        report.setOccupiedSeats(attendanceList.size());
        report.setEmptySeats(vacantSeats.size());
        report.setAttendanceList(attendanceList);

        List<DepartmentSummaryDto> deptSummary = deptCounts.entrySet().stream()
                .map(e -> new DepartmentSummaryDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        report.setDepartmentSummary(deptSummary);
        report.setVacantSeatsList(vacantSeats);

        return report;
    }

    @Transactional(readOnly = true)
    public byte[] generateInvigilatorReportPdf(UUID examId, UUID hallId) {
        InvigilatorReportDto report = getInvigilatorReportData(examId, hallId);

        Document document = new Document(PageSize.A4.rotate(), 25, 25, 25, 25);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Color navyPrimary = new Color(30, 58, 138);
            Color lightGray = new Color(243, 244, 246);

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, navyPrimary);
            Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font fontTHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
            Font fontBody = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font fontBodyBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);

            // Title Table
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);

            PdfPCell c1 = new PdfPCell(new Phrase(report.getCollegeName().toUpperCase(), fontTitle));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBorder(Rectangle.NO_BORDER);
            header.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase("ROOM-WISE EXAMINATION SEATING & ATTENDANCE RECORD", fontHeader));
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            c2.setBackgroundColor(navyPrimary);
            c2.setPadding(4);
            header.addCell(c2);

            document.add(header);
            document.add(new Paragraph(" "));

            // Meta Info Grid
            PdfPTable meta = new PdfPTable(4);
            meta.setWidthPercentage(100);
            meta.setWidths(new float[]{25, 25, 25, 25});

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
            addMetaCell(meta, "Examination:", report.getExamName(), fontTHead, fontBodyBold);
            addMetaCell(meta, "Hall / Venue:", report.getHallName(), fontTHead, fontBodyBold);
            addMetaCell(meta, "Date & Session:", report.getExamDate().format(dtf) + " (" + report.getSession() + ")", fontTHead, fontBody);
            addMetaCell(meta, "Timings:", report.getStartTime() + " - " + report.getEndTime(), fontTHead, fontBody);
            addMetaCell(meta, "Total Capacity:", String.valueOf(report.getTotalCapacity()), fontTHead, fontBody);
            addMetaCell(meta, "Occupied Seats:", String.valueOf(report.getOccupiedSeats()), fontTHead, fontBodyBold);
            addMetaCell(meta, "Vacant Seats:", String.valueOf(report.getEmptySeats()), fontTHead, fontBody);
            addMetaCell(meta, "Building & Floor:", report.getBuilding() + " / " + report.getFloor(), fontTHead, fontBody);

            document.add(meta);
            document.add(new Paragraph(" "));

            // Attendance Table
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{5, 8, 8, 8, 14, 20, 17, 6, 14});

            String[] headers = {"Sl", "Row", "Bench", "Seat", "Roll Number", "Student Name", "Department", "Sem", "Signature"};
            for (String h : headers) {
                PdfPCell th = new PdfPCell(new Phrase(h, fontTHead));
                th.setBackgroundColor(lightGray);
                th.setPadding(4);
                th.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(th);
            }

            for (HallAttendanceDto att : report.getAttendanceList()) {
                table.addCell(createRowCell(String.valueOf(att.getSlNo()), fontBody, Element.ALIGN_CENTER));
                table.addCell(createRowCell("R" + att.getRowNumber(), fontBodyBold, Element.ALIGN_CENTER));
                table.addCell(createRowCell("B" + att.getBenchNumber(), fontBodyBold, Element.ALIGN_CENTER));
                table.addCell(createRowCell("S" + att.getPosition(), fontBodyBold, Element.ALIGN_CENTER));
                table.addCell(createRowCell(att.getRollNo(), fontBodyBold, Element.ALIGN_LEFT));
                table.addCell(createRowCell(att.getStudentName(), fontBody, Element.ALIGN_LEFT));
                table.addCell(createRowCell(att.getDepartment(), fontBody, Element.ALIGN_LEFT));
                table.addCell(createRowCell(String.valueOf(att.getSemester()), fontBody, Element.ALIGN_CENTER));
                table.addCell(createRowCell("", fontBody, Element.ALIGN_CENTER)); // Signature line placeholder
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // Sign-off
            PdfPTable footer = new PdfPTable(3);
            footer.setWidthPercentage(100);
            footer.setWidths(new float[]{33, 34, 33});

            PdfPCell f1 = new PdfPCell(new Phrase("Total Present: ________   Total Absent: ________", fontBodyBold));
            f1.setBorder(Rectangle.NO_BORDER);
            footer.addCell(f1);

            PdfPCell f2 = new PdfPCell(new Phrase("Invigilator Name & Signature: __________________", fontBodyBold));
            f2.setBorder(Rectangle.NO_BORDER);
            footer.addCell(f2);

            PdfPCell f3 = new PdfPCell(new Phrase("Exam Superintendent: __________________", fontBodyBold));
            f3.setBorder(Rectangle.NO_BORDER);
            f3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            footer.addCell(f3);

            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate Invigilator PDF report: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    @Transactional(readOnly = true)
    public byte[] generateInvigilatorReportCsv(UUID examId, UUID hallId) throws IOException {
        InvigilatorReportDto report = getInvigilatorReportData(examId, hallId);

        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder()
                .setHeader("Sl_No", "Hall", "Row", "Bench", "Seat_Position", "Roll_No", "Student_Name", "Department", "Semester", "Attendance_Status")
                .build())) {

            for (HallAttendanceDto att : report.getAttendanceList()) {
                printer.printRecord(
                        att.getSlNo(),
                        report.getHallName(),
                        att.getRowNumber(),
                        att.getBenchNumber(),
                        att.getPosition(),
                        att.getRollNo(),
                        att.getStudentName(),
                        att.getDepartment(),
                        att.getSemester(),
                        ""
                );
            }
        }

        return sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private void addMetaCell(PdfPTable table, String label, String val, Font labelFont, Font valFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(3);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(Color.LIGHT_GRAY);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", labelFont));
        p.add(new Chunk(val, valFont));
        cell.addElement(p);
        table.addCell(cell);
    }

    private PdfPCell createRowCell(String val, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(val, font));
        cell.setPadding(3);
        cell.setHorizontalAlignment(alignment);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }
}
