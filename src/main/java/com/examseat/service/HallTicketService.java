package com.examseat.service;

import com.examseat.dto.HallTicketDto;
import com.examseat.entity.*;
import com.examseat.exception.InvalidOperationException;
import com.examseat.exception.ResourceNotFoundException;
import com.examseat.repository.AllocationRepository;
import com.examseat.repository.ExamRepository;
import com.examseat.repository.HallTicketRepository;
import com.examseat.repository.StudentRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class HallTicketService {

    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;
    private final AllocationRepository allocationRepository;
    private final HallTicketRepository hallTicketRepository;

    @Value("${college.name:ExamSeat Institute of Technology & Management}")
    private String collegeName;

    @Value("${college.address:Knowledge Park Campus, Innovation Road, Metro City - 600001}")
    private String collegeAddress;

    public HallTicketService(ExamRepository examRepository,
                             StudentRepository studentRepository,
                             AllocationRepository allocationRepository,
                             HallTicketRepository hallTicketRepository) {
        this.examRepository = examRepository;
        this.studentRepository = studentRepository;
        this.allocationRepository = allocationRepository;
        this.hallTicketRepository = hallTicketRepository;
    }

    @Transactional
    public byte[] generateHallTicketPdf(UUID examId, UUID studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found: " + examId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        Allocation allocation = allocationRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new InvalidOperationException("No seating allocation found for student " +
                        student.getRollNo() + " in this examination."));

        // Generate or fetch HallTicket record
        String ticketNumber = "HT-" + student.getRollNo() + "-" + exam.getId().toString().substring(0, 8).toUpperCase();
        Optional<HallTicket> existingTicket = hallTicketRepository.findByExamIdAndStudentId(examId, studentId);
        if (existingTicket.isEmpty()) {
            HallTicket ticket = new HallTicket(ticketNumber, student, exam, "{\"status\":\"GENERATED\"}");
            hallTicketRepository.save(ticket);
        }

        return createPdfDocument(exam, student, allocation.getSeat(), ticketNumber);
    }

    @Transactional(readOnly = true)
    public List<HallTicketDto> getTicketsForExam(UUID examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found: " + examId));

        List<Allocation> allocations = allocationRepository.findByExamId(examId);
        List<HallTicketDto> ticketDtos = new ArrayList<>();

        for (Allocation a : allocations) {
            Student s = a.getStudent();
            Seat seat = a.getSeat();
            String ticketNumber = "HT-" + s.getRollNo() + "-" + exam.getId().toString().substring(0, 8).toUpperCase();

            HallTicketDto dto = new HallTicketDto();
            dto.setTicketNumber(ticketNumber);
            dto.setStudentId(s.getId());
            dto.setStudentName(s.getName());
            dto.setRollNo(s.getRollNo());
            dto.setDepartment(s.getDepartment());
            dto.setSemester(s.getSemester());

            dto.setExamId(exam.getId());
            dto.setExamName(exam.getName());
            dto.setExamDate(exam.getExamDate());
            dto.setSession(exam.getSession());
            dto.setReportingTime(exam.getReportingTime());
            dto.setStartTime(exam.getStartTime());
            dto.setEndTime(exam.getEndTime());
            dto.setInstructions(exam.getInstructions());

            dto.setHallName(seat.getHall().getName());
            dto.setBuilding(seat.getHall().getBuilding());
            dto.setFloor(seat.getHall().getFloor());
            dto.setRowNumber(seat.getRowNumber());
            dto.setBenchNumber(seat.getBenchNumber());
            dto.setPosition(seat.getPosition());

            ticketDtos.add(dto);
        }

        return ticketDtos;
    }

    @Transactional
    public byte[] generateBulkHallTicketsZip(UUID examId) throws IOException {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Examination not found: " + examId));

        List<Allocation> allocations = allocationRepository.findByExamId(examId);
        if (allocations.isEmpty()) {
            throw new InvalidOperationException("No allocations found for this examination.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Allocation a : allocations) {
                Student s = a.getStudent();
                byte[] pdfBytes = generateHallTicketPdf(examId, s.getId());
                ZipEntry entry = new ZipEntry("HallTicket_" + s.getRollNo() + ".pdf");
                zos.putNextEntry(entry);
                zos.write(pdfBytes);
                zos.closeEntry();
            }
        }

        return baos.toByteArray();
    }

    private byte[] createPdfDocument(Exam exam, Student student, Seat seat, String ticketNumber) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Colors
            Color navyPrimary = new Color(30, 58, 138); // #1e3a8a
            Color tealAccent = new Color(13, 148, 136);  // #0d9488
            Color lightGray = new Color(243, 244, 246);   // #f3f4f6
            Color textDark = new Color(17, 24, 39);      // #111827

            // Fonts
            Font fontCollege = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, navyPrimary);
            Font fontSubTitle = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.WHITE);
            Font fontSectionTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, navyPrimary);
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.DARK_GRAY);
            Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 10, textDark);
            Font fontValueBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, textDark);
            Font fontInstructions = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);

            // College Header Box
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);

            PdfPCell c1 = new PdfPCell(new Phrase(collegeName.toUpperCase(), fontCollege));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBorder(Rectangle.NO_BORDER);
            c1.setPaddingBottom(4);
            headerTable.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase(collegeAddress, fontSubTitle));
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            c2.setBorder(Rectangle.NO_BORDER);
            c2.setPaddingBottom(8);
            headerTable.addCell(c2);

            PdfPCell c3 = new PdfPCell(new Phrase("EXAMINATION HALL TICKET / ADMIT CARD", fontHeader));
            c3.setHorizontalAlignment(Element.ALIGN_CENTER);
            c3.setBackgroundColor(navyPrimary);
            c3.setPadding(6);
            c3.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(c3);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // Ticket meta table (Ticket ID & Date)
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{60, 40});

            PdfPCell ticketNoCell = new PdfPCell(new Phrase("Ticket ID: " + ticketNumber, fontValueBold));
            ticketNoCell.setBorder(Rectangle.NO_BORDER);
            metaTable.addCell(ticketNoCell);

            PdfPCell sessionCell = new PdfPCell(new Phrase("Academic Year: 2026-2027", fontLabel));
            sessionCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            sessionCell.setBorder(Rectangle.NO_BORDER);
            metaTable.addCell(sessionCell);

            document.add(metaTable);
            document.add(new Paragraph(" "));

            // Student Information & Seating Details Tables
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setWidths(new float[]{50, 50});

            // Column 1: Candidate Details
            PdfPTable candTable = new PdfPTable(2);
            candTable.setWidthPercentage(100);
            candTable.setWidths(new float[]{40, 60});

            PdfPCell candHeader = new PdfPCell(new Phrase("CANDIDATE INFORMATION", fontSectionTitle));
            candHeader.setColspan(2);
            candHeader.setBackgroundColor(lightGray);
            candHeader.setPadding(6);
            candTable.addCell(candHeader);

            addTableRow(candTable, "Roll Number:", student.getRollNo(), fontLabel, fontValueBold);
            addTableRow(candTable, "Student Name:", student.getName(), fontLabel, fontValue);
            addTableRow(candTable, "Department:", student.getDepartment(), fontLabel, fontValue);
            addTableRow(candTable, "Semester:", String.valueOf(student.getSemester()), fontLabel, fontValue);
            if (student.getEmail() != null) {
                addTableRow(candTable, "Email:", student.getEmail(), fontLabel, fontValue);
            }

            PdfPCell candCell = new PdfPCell(candTable);
            candCell.setPadding(4);
            candCell.setBorder(Rectangle.BOX);
            candCell.setBorderColor(navyPrimary);
            detailsTable.addCell(candCell);

            // Column 2: Seating & Venue Allocation
            PdfPTable venueTable = new PdfPTable(2);
            venueTable.setWidthPercentage(100);
            venueTable.setWidths(new float[]{40, 60});

            PdfPCell venueHeader = new PdfPCell(new Phrase("SEATING ALLOCATION", fontSectionTitle));
            venueHeader.setColspan(2);
            venueHeader.setBackgroundColor(lightGray);
            venueHeader.setPadding(6);
            venueTable.addCell(venueHeader);

            addTableRow(venueTable, "Hall Name:", seat.getHall().getName(), fontLabel, fontValueBold);
            addTableRow(venueTable, "Building & Floor:", seat.getHall().getBuilding() + " (" + seat.getHall().getFloor() + ")", fontLabel, fontValue);
            addTableRow(venueTable, "Row Number:", "Row " + seat.getRowNumber(), fontLabel, fontValueBold);
            addTableRow(venueTable, "Bench Number:", "Bench " + seat.getBenchNumber(), fontLabel, fontValueBold);
            addTableRow(venueTable, "Seat Position:", "Seat " + seat.getPosition(), fontLabel, fontValueBold);

            PdfPCell venueCell = new PdfPCell(venueTable);
            venueCell.setPadding(4);
            venueCell.setBorder(Rectangle.BOX);
            venueCell.setBorderColor(tealAccent);
            detailsTable.addCell(venueCell);

            document.add(detailsTable);
            document.add(new Paragraph(" "));

            // Examination Schedule Table
            PdfPTable examTable = new PdfPTable(4);
            examTable.setWidthPercentage(100);
            examTable.setWidths(new float[]{35, 25, 20, 20});

            PdfPCell exHeader = new PdfPCell(new Phrase("SCHEDULED EXAMINATION DETAILS", fontSectionTitle));
            exHeader.setColspan(4);
            exHeader.setBackgroundColor(lightGray);
            exHeader.setPadding(6);
            examTable.addCell(exHeader);

            examTable.addCell(createCell("Examination Name", fontLabel, true));
            examTable.addCell(createCell("Date", fontLabel, true));
            examTable.addCell(createCell("Session", fontLabel, true));
            examTable.addCell(createCell("Timings", fontLabel, true));

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
            examTable.addCell(createCell(exam.getName(), fontValueBold, false));
            examTable.addCell(createCell(exam.getExamDate().format(dtf), fontValue, false));
            examTable.addCell(createCell(exam.getSession().name() + " (Report: " + exam.getReportingTime() + ")", fontValue, false));
            examTable.addCell(createCell(exam.getStartTime() + " - " + exam.getEndTime(), fontValue, false));

            document.add(examTable);
            document.add(new Paragraph(" "));

            // Instructions Box
            PdfPTable instrTable = new PdfPTable(1);
            instrTable.setWidthPercentage(100);

            PdfPCell instrHeader = new PdfPCell(new Phrase("IMPORTANT CANDIDATE INSTRUCTIONS", fontSectionTitle));
            instrHeader.setBackgroundColor(lightGray);
            instrHeader.setPadding(5);
            instrTable.addCell(instrHeader);

            String instructionsText = (exam.getInstructions() != null && !exam.getInstructions().trim().isEmpty())
                    ? exam.getInstructions()
                    : "1. Candidates must present their physical College Identity Card along with this Hall Ticket.\n" +
                      "2. Reporting time is strictly enforced. No student will be permitted entry 15 minutes after examination begins.\n" +
                      "3. Possession of mobile phones, smart watches, digital devices, or unauthorized study material is strictly prohibited.\n" +
                      "4. Sit only in the designated Hall, Row, Bench, and Seat Position indicated on this ticket.";

            PdfPCell instrBody = new PdfPCell(new Phrase(instructionsText, fontInstructions));
            instrBody.setPadding(8);
            instrTable.addCell(instrBody);

            document.add(instrTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Signature Sign-off Box
            PdfPTable signTable = new PdfPTable(3);
            signTable.setWidthPercentage(100);
            signTable.setWidths(new float[]{33, 34, 33});

            PdfPCell s1 = new PdfPCell(new Phrase("\n\n___________________________\nCandidate's Signature", fontLabel));
            s1.setHorizontalAlignment(Element.ALIGN_CENTER);
            s1.setBorder(Rectangle.NO_BORDER);
            signTable.addCell(s1);

            PdfPCell s2 = new PdfPCell(new Phrase("\n\n___________________________\nInvigilator's Verification", fontLabel));
            s2.setHorizontalAlignment(Element.ALIGN_CENTER);
            s2.setBorder(Rectangle.NO_BORDER);
            signTable.addCell(s2);

            PdfPCell s3 = new PdfPCell(new Phrase("\n\n___________________________\nController of Examinations", fontLabel));
            s3.setHorizontalAlignment(Element.ALIGN_CENTER);
            s3.setBorder(Rectangle.NO_BORDER);
            signTable.addCell(s3);

            document.add(signTable);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate Hall Ticket PDF: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, labelFont));
        lCell.setPadding(4);
        lCell.setBorder(Rectangle.BOTTOM);
        lCell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(lCell);

        PdfPCell vCell = new PdfPCell(new Phrase(value, valueFont));
        vCell.setPadding(4);
        vCell.setBorder(Rectangle.BOTTOM);
        vCell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(vCell);
    }

    private PdfPCell createCell(String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        if (isHeader) {
            cell.setBackgroundColor(new Color(243, 244, 246));
        }
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }
}
