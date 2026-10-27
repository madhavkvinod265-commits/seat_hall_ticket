package com.examseat.service;

import com.examseat.dto.*;
import com.examseat.entity.Student;
import com.examseat.exception.ConflictException;
import com.examseat.exception.ResourceNotFoundException;
import com.examseat.repository.StudentRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<StudentDto> getStudents(String search, String department, Integer semester, Pageable pageable) {
        String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String trimmedDept = (department != null && !department.trim().isEmpty()) ? department.trim() : null;

        Page<Student> page = studentRepository.searchStudents(trimmedSearch, trimmedDept, semester, pageable);
        List<StudentDto> content = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentById(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        return toDto(student);
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentByRollNo(String rollNo) {
        Student student = studentRepository.findByRollNoIgnoreCase(rollNo.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with Roll Number: " + rollNo));
        return toDto(student);
    }

    @Transactional
    public StudentDto createStudent(StudentRequest request) {
        if (studentRepository.existsByRollNo(request.getRollNo().trim())) {
            throw new ConflictException("A student with Roll Number '" + request.getRollNo() + "' already exists");
        }

        Student student = new Student(
                request.getRollNo().trim().toUpperCase(),
                request.getName().trim(),
                request.getDepartment().trim(),
                request.getSemester(),
                request.getEmail() != null ? request.getEmail().trim() : null
        );
        student.setActive(request.getActive() != null ? request.getActive() : true);

        return toDto(studentRepository.save(student));
    }

    @Transactional
    public StudentDto updateStudent(UUID id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        String newRollNo = request.getRollNo().trim().toUpperCase();
        if (!student.getRollNo().equalsIgnoreCase(newRollNo) && studentRepository.existsByRollNo(newRollNo)) {
            throw new ConflictException("Roll Number '" + newRollNo + "' is already assigned to another student");
        }

        student.setRollNo(newRollNo);
        student.setName(request.getName().trim());
        student.setDepartment(request.getDepartment().trim());
        student.setSemester(request.getSemester());
        student.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        student.setActive(request.getActive() != null ? request.getActive() : true);

        return toDto(studentRepository.save(student));
    }

    @Transactional
    public void deleteStudent(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        // Soft delete / deactivate to protect academic history
        student.setActive(false);
        studentRepository.save(student);
    }

    /**
     * Previews CSV import before applying changes
     */
    public CsvImportPreviewDto previewCsvImport(MultipartFile file) {
        CsvImportPreviewDto preview = new CsvImportPreviewDto();
        List<StudentCsvRow> rows = new ArrayList<>();
        Set<String> seenRollNosInFile = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            CSVParser parser = csvFormat.parse(reader);
            Map<String, Integer> headerMap = parser.getHeaderMap();

            // Validate required headers
            if (!headerMap.containsKey("roll_no") || !headerMap.containsKey("name") ||
                !headerMap.containsKey("department") || !headerMap.containsKey("semester")) {
                preview.getGlobalErrors().add("Invalid CSV format. Missing required columns: roll_no, name, department, semester");
                return preview;
            }

            int validCount = 0;
            int invalidCount = 0;
            int duplicateCount = 0;
            int rowIdx = 1;

            for (CSVRecord record : parser) {
                rowIdx++;
                String rollNo = record.isSet("roll_no") ? record.get("roll_no").trim().toUpperCase() : "";
                String name = record.isSet("name") ? record.get("name").trim() : "";
                String dept = record.isSet("department") ? record.get("department").trim() : "";
                String semStr = record.isSet("semester") ? record.get("semester").trim() : "";

                boolean valid = true;
                StringBuilder errors = new StringBuilder();

                if (rollNo.isEmpty()) {
                    valid = false;
                    errors.append("Missing roll number; ");
                } else if (seenRollNosInFile.contains(rollNo)) {
                    valid = false;
                    duplicateCount++;
                    errors.append("Duplicate roll number within CSV file; ");
                } else if (studentRepository.existsByRollNo(rollNo)) {
                    valid = false;
                    duplicateCount++;
                    errors.append("Roll number already exists in database; ");
                } else {
                    seenRollNosInFile.add(rollNo);
                }

                if (name.isEmpty()) {
                    valid = false;
                    errors.append("Missing student name; ");
                }
                if (dept.isEmpty()) {
                    valid = false;
                    errors.append("Missing department; ");
                }

                Integer sem = null;
                try {
                    sem = Integer.parseInt(semStr);
                    if (sem < 1 || sem > 12) {
                        valid = false;
                        errors.append("Semester must be between 1 and 12; ");
                    }
                } catch (NumberFormatException e) {
                    valid = false;
                    errors.append("Invalid semester number: ").append(semStr).append("; ");
                }

                if (valid) {
                    validCount++;
                } else {
                    invalidCount++;
                }

                rows.add(new StudentCsvRow(rowIdx, rollNo, name, dept, sem, valid, errors.toString()));
            }

            preview.setTotalRows(rows.size());
            preview.setValidRowsCount(validCount);
            preview.setInvalidRowsCount(invalidCount);
            preview.setDuplicateRowsCount(duplicateCount);
            preview.setPreviewRows(rows);

        } catch (Exception e) {
            preview.getGlobalErrors().add("Failed to parse CSV file: " + e.getMessage());
        }

        return preview;
    }

    /**
     * Executes CSV student import transactionally
     */
    @Transactional
    public CsvImportResultDto commitCsvImport(MultipartFile file) {
        CsvImportPreviewDto preview = previewCsvImport(file);
        if (!preview.getGlobalErrors().isEmpty()) {
            CsvImportResultDto result = new CsvImportResultDto(0, 0, 0, 0);
            result.setErrorMessages(preview.getGlobalErrors());
            return result;
        }

        List<Student> studentsToSave = new ArrayList<>();
        int inserted = 0;
        int skipped = 0;
        int rejected = 0;
        List<String> errors = new ArrayList<>();

        for (StudentCsvRow row : preview.getPreviewRows()) {
            if (row.isValid()) {
                Student student = new Student(row.getRollNo(), row.getName(), row.getDepartment(), row.getSemester(), null);
                studentsToSave.add(student);
                inserted++;
            } else {
                if (row.getErrorMessage().contains("already exists") || row.getErrorMessage().contains("Duplicate")) {
                    skipped++;
                } else {
                    rejected++;
                }
                errors.add("Row " + row.getRowNumber() + " (" + row.getRollNo() + "): " + row.getErrorMessage());
            }
        }

        if (!studentsToSave.isEmpty()) {
            studentRepository.saveAll(studentsToSave);
        }

        CsvImportResultDto result = new CsvImportResultDto(preview.getTotalRows(), inserted, skipped, rejected);
        result.setErrorMessages(errors);
        return result;
    }

    private StudentDto toDto(Student student) {
        return new StudentDto(
                student.getId(),
                student.getRollNo(),
                student.getName(),
                student.getDepartment(),
                student.getSemester(),
                student.getEmail(),
                student.getActive()
        );
    }
}
