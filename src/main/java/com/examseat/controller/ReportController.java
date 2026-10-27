package com.examseat.controller;

import com.examseat.dto.ApiResponse;
import com.examseat.dto.InvigilatorReportDto;
import com.examseat.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/exams/{examId}/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/halls/{hallId}")
    public ResponseEntity<ApiResponse<InvigilatorReportDto>> getInvigilatorReportData(
            @PathVariable UUID examId,
            @PathVariable UUID hallId) {
        InvigilatorReportDto report = reportService.getInvigilatorReportData(examId, hallId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/halls/{hallId}/pdf")
    public ResponseEntity<byte[]> downloadInvigilatorReportPdf(
            @PathVariable UUID examId,
            @PathVariable UUID hallId) {
        byte[] pdfBytes = reportService.generateInvigilatorReportPdf(examId, hallId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"InvigilatorReport_Hall_" + hallId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/halls/{hallId}/csv")
    public ResponseEntity<byte[]> downloadInvigilatorReportCsv(
            @PathVariable UUID examId,
            @PathVariable UUID hallId) throws IOException {
        byte[] csvBytes = reportService.generateInvigilatorReportCsv(examId, hallId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"SeatingReport_Hall_" + hallId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
