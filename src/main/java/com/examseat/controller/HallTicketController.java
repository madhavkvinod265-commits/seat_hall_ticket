package com.examseat.controller;

import com.examseat.dto.ApiResponse;
import com.examseat.dto.HallTicketDto;
import com.examseat.service.HallTicketService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exams/{examId}/tickets")
public class HallTicketController {

    private final HallTicketService hallTicketService;

    public HallTicketController(HallTicketService hallTicketService) {
        this.hallTicketService = hallTicketService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HallTicketDto>>> getTicketsForExam(@PathVariable UUID examId) {
        List<HallTicketDto> tickets = hallTicketService.getTicketsForExam(examId);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/{studentId}/pdf")
    public ResponseEntity<byte[]> downloadHallTicketPdf(@PathVariable UUID examId, @PathVariable UUID studentId) {
        byte[] pdfBytes = hallTicketService.generateHallTicketPdf(examId, studentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"HallTicket_" + studentId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/bulk")
    public ResponseEntity<byte[]> downloadBulkHallTicketsZip(@PathVariable UUID examId) throws IOException {
        byte[] zipBytes = hallTicketService.generateBulkHallTicketsZip(examId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"HallTickets_Exam_" + examId + ".zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipBytes);
    }
}
