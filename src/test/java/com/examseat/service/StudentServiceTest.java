package com.examseat.service;

import com.examseat.dto.CsvImportPreviewDto;
import com.examseat.dto.CsvImportResultDto;
import com.examseat.dto.StudentRequest;
import com.examseat.entity.Student;
import com.examseat.exception.ConflictException;
import com.examseat.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository);
    }

    @Test
    @DisplayName("Manual Student Registration: Duplicate roll number rejection")
    void testDuplicateRollNumberRejection() {
        StudentRequest req = new StudentRequest("CS001", "Ananya Raj", "Computer Science", 5, "ananya@test.edu", true);
        when(studentRepository.existsByRollNo("CS001")).thenReturn(true);

        assertThrows(ConflictException.class, () -> studentService.createStudent(req));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("CSV Import: Valid CSV with multiple records passes preview")
    void testValidCsvPreview() {
        String csvContent = "roll_no,name,department,semester\n" +
                "CS001,Ananya Raj,Computer Science,5\n" +
                "EC001,Arjun Nair,Electronics,5\n" +
                "ME001,Adithya Menon,Mechanical,5\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        when(studentRepository.existsByRollNo("CS001")).thenReturn(false);
        when(studentRepository.existsByRollNo("EC001")).thenReturn(false);
        when(studentRepository.existsByRollNo("ME001")).thenReturn(false);

        CsvImportPreviewDto preview = studentService.previewCsvImport(file);

        assertTrue(preview.getGlobalErrors().isEmpty());
        assertEquals(3, preview.getTotalRows());
        assertEquals(3, preview.getValidRowsCount());
        assertEquals(0, preview.getInvalidRowsCount());
        assertEquals(0, preview.getDuplicateRowsCount());
    }

    @Test
    @DisplayName("CSV Import: Missing required header rejects CSV")
    void testMissingHeaderRejection() {
        String invalidCsv = "roll_no,student_name,department\n" +
                "CS001,Ananya Raj,Computer Science\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", invalidCsv.getBytes(StandardCharsets.UTF_8));

        CsvImportPreviewDto preview = studentService.previewCsvImport(file);

        assertFalse(preview.getGlobalErrors().isEmpty());
        assertTrue(preview.getGlobalErrors().get(0).contains("Missing required columns"));
    }

    @Test
    @DisplayName("CSV Import: Detect duplicate roll numbers within CSV file and database")
    void testDuplicateInFileAndDbDetection() {
        String csvContent = "roll_no,name,department,semester\n" +
                "CS001,Ananya Raj,Computer Science,5\n" +
                "CS001,Ananya Duplicate,Computer Science,5\n" + // duplicate in file
                "CS002,Existing Student,Computer Science,5\n" + // duplicate in db
                "EC001,Valid Student,Electronics,15\n";          // invalid semester > 12

        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        when(studentRepository.existsByRollNo("CS001")).thenReturn(false);
        when(studentRepository.existsByRollNo("CS002")).thenReturn(true);
        when(studentRepository.existsByRollNo("EC001")).thenReturn(false);

        CsvImportPreviewDto preview = studentService.previewCsvImport(file);

        assertEquals(4, preview.getTotalRows());
        assertEquals(1, preview.getValidRowsCount()); // only CS001 first row is valid
        assertEquals(3, preview.getInvalidRowsCount());
        assertEquals(2, preview.getDuplicateRowsCount()); // 1 in file, 1 in db
    }

    @Test
    @DisplayName("CSV Import Commit: Saves valid rows and reports counts")
    void testCommitCsvImport() {
        String csvContent = "roll_no,name,department,semester\n" +
                "CS001,Ananya Raj,Computer Science,5\n" +
                "EC001,Arjun Nair,Electronics,5\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        when(studentRepository.existsByRollNo(anyString())).thenReturn(false);

        CsvImportResultDto result = studentService.commitCsvImport(file);

        assertEquals(2, result.getTotalProcessed());
        assertEquals(2, result.getInsertedCount());
        assertEquals(0, result.getSkippedCount());
        assertEquals(0, result.getRejectedCount());
        verify(studentRepository, times(1)).saveAll(anyList());
    }
}
