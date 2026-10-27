package com.examseat.controller;

import com.examseat.dto.*;
import com.examseat.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<StudentDto>>> getStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer semester,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "rollNo") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<StudentDto> paged = studentService.getStudents(search, department, semester, pageable);
        return ResponseEntity.ok(ApiResponse.success(paged));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDto>> getStudentById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudentById(id)));
    }

    @GetMapping("/roll/{rollNo}")
    public ResponseEntity<ApiResponse<StudentDto>> getStudentByRollNo(@PathVariable String rollNo) {
        return ResponseEntity.ok(ApiResponse.success(studentService.getStudentByRollNo(rollNo)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentDto>> createStudent(@Valid @RequestBody StudentRequest request) {
        StudentDto created = studentService.createStudent(request);
        return ResponseEntity.ok(ApiResponse.success("Student registered successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDto>> updateStudent(@PathVariable UUID id, @Valid @RequestBody StudentRequest request) {
        StudentDto updated = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable UUID id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student deactivated successfully", null));
    }

    @PostMapping("/import/preview")
    public ResponseEntity<ApiResponse<CsvImportPreviewDto>> previewCsvImport(@RequestParam("file") MultipartFile file) {
        CsvImportPreviewDto preview = studentService.previewCsvImport(file);
        return ResponseEntity.ok(ApiResponse.success("CSV import preview generated", preview));
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<CsvImportResultDto>> commitCsvImport(@RequestParam("file") MultipartFile file) {
        CsvImportResultDto result = studentService.commitCsvImport(file);
        return ResponseEntity.ok(ApiResponse.success("CSV import processed", result));
    }
}
