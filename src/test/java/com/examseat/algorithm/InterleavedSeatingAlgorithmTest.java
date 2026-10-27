package com.examseat.algorithm;

import com.examseat.entity.*;
import com.examseat.exception.InsufficientCapacityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InterleavedSeatingAlgorithmTest {

    private InterleavedSeatingAlgorithm algorithm;
    private Exam testExam;

    @BeforeEach
    void setUp() {
        algorithm = new InterleavedSeatingAlgorithm();
        testExam = new Exam("Semester Exam", LocalDate.now().plusDays(5), Session.FN,
                LocalTime.of(9, 30), LocalTime.of(12, 30), LocalTime.of(9, 0), "Instructions", "admin");
        testExam.setId(UUID.randomUUID());
    }

    private Hall createHall(String name, int rows, int benchesPerRow, int seatsPerBench) {
        Hall hall = new Hall(name, "Block A", "1st Floor", rows, benchesPerRow, seatsPerBench);
        hall.setId(UUID.randomUUID());
        return hall;
    }

    private List<Seat> generateSeats(Hall hall) {
        List<Seat> seats = new ArrayList<>();
        for (int r = 1; r <= hall.getRows(); r++) {
            for (int b = 1; b <= hall.getBenchesPerRow(); b++) {
                for (int p = 1; p <= hall.getSeatsPerBench(); p++) {
                    Seat seat = new Seat(hall, r, b, p);
                    seat.setId(UUID.randomUUID());
                    seats.add(seat);
                }
            }
        }
        return seats;
    }

    private Student createStudent(String rollNo, String name, String dept, int sem) {
        Student student = new Student(rollNo, name, dept, sem, rollNo.toLowerCase() + "@test.edu");
        student.setId(UUID.randomUUID());
        return student;
    }

    @Test
    @DisplayName("Test 1: Round-robin distribution and department alternation on two-seat benches")
    void testRoundRobinAndAlternationOnTwoSeatBenches() {
        // Create a hall with 2-seat benches (4 rows x 3 benches = 12 benches = 24 seats)
        Hall hall = createHall("Hall 101", 4, 3, 2);
        List<Seat> seats = generateSeats(hall);

        // Unequal departments: CS: 5, EC: 3, ME: 2 (Total: 10 students)
        List<Student> students = new ArrayList<>();
        students.add(createStudent("CS001", "CS Student 1", "Computer Science", 5));
        students.add(createStudent("CS002", "CS Student 2", "Computer Science", 5));
        students.add(createStudent("CS003", "CS Student 3", "Computer Science", 5));
        students.add(createStudent("CS004", "CS Student 4", "Computer Science", 5));
        students.add(createStudent("CS005", "CS Student 5", "Computer Science", 5));

        students.add(createStudent("EC001", "EC Student 1", "Electronics", 5));
        students.add(createStudent("EC002", "EC Student 2", "Electronics", 5));
        students.add(createStudent("EC003", "EC Student 3", "Electronics", 5));

        students.add(createStudent("ME001", "ME Student 1", "Mechanical", 5));
        students.add(createStudent("ME002", "ME Student 2", "Mechanical", 5));

        InterleavedSeatingAlgorithm.AllocationResult result = algorithm.allocateSeats(testExam, students, seats, "test_staff");

        assertNotNull(result);
        assertEquals(10, result.getAllocatedCount());
        assertEquals(0, result.getUnassignedCount());
        assertEquals(24, result.getTotalAvailableSeats());
        assertEquals(10, result.getAllocations().size());

        // Verify bench-level alternation:
        // Group allocations by bench
        Map<String, List<Allocation>> benchAllocations = new HashMap<>();
        for (Allocation a : result.getAllocations()) {
            String bKey = a.getSeat().getRowNumber() + "_" + a.getSeat().getBenchNumber();
            benchAllocations.computeIfAbsent(bKey, k -> new ArrayList<>()).add(a);
        }

        // For every bench that has 2 students, verify they are from different departments whenever possible
        int benchesWithTwoStudents = 0;
        int alternatingBenches = 0;
        for (List<Allocation> bAllocs : benchAllocations.values()) {
            if (bAllocs.size() == 2) {
                benchesWithTwoStudents++;
                String dept1 = bAllocs.get(0).getStudent().getDepartment();
                String dept2 = bAllocs.get(1).getStudent().getDepartment();
                if (!dept1.equalsIgnoreCase(dept2)) {
                    alternatingBenches++;
                }
            }
        }

        assertTrue(benchesWithTwoStudents > 0, "Should have benches with 2 students");
        assertTrue(alternatingBenches > 0, "Should successfully alternate departments on benches");
    }

    @Test
    @DisplayName("Test 2: Allocation uniqueness - each student has exactly 1 seat, each seat has 1 student")
    void testAllocationUniqueness() {
        Hall hall = createHall("Hall 102", 5, 4, 2); // 40 seats
        List<Seat> seats = generateSeats(hall);

        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String dept = (i % 2 == 0) ? "Computer Science" : "Mechanical";
            students.add(createStudent("ROLL" + i, "Student " + i, dept, 5));
        }

        InterleavedSeatingAlgorithm.AllocationResult result = algorithm.allocateSeats(testExam, students, seats, "test_admin");

        Set<UUID> allocatedStudents = new HashSet<>();
        Set<UUID> allocatedSeats = new HashSet<>();

        for (Allocation a : result.getAllocations()) {
            assertTrue(allocatedStudents.add(a.getStudent().getId()), "Duplicate student allocation detected: " + a.getStudent().getRollNo());
            assertTrue(allocatedSeats.add(a.getSeat().getId()), "Duplicate seat allocation detected: " + a.getSeat().getId());
        }

        assertEquals(20, allocatedStudents.size());
        assertEquals(20, allocatedSeats.size());
    }

    @Test
    @DisplayName("Test 3: Insufficient capacity handling - throws InsufficientCapacityException with details")
    void testInsufficientCapacityHandling() {
        // Hall capacity: 2 rows x 2 benches x 2 seats = 8 seats
        Hall hall = createHall("Small Hall", 2, 2, 2);
        List<Seat> seats = generateSeats(hall);

        // 12 students for 8 seats -> excess: 4
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            students.add(createStudent("ROLL" + i, "Student " + i, "Computer Science", 5));
        }

        InsufficientCapacityException ex = assertThrows(InsufficientCapacityException.class, () ->
                algorithm.allocateSeats(testExam, students, seats, "admin"));

        assertEquals(12, ex.getRequiredSeats());
        assertEquals(8, ex.getAvailableSeats());
        assertEquals(4, ex.getExcessStudents());
        assertNotNull(ex.getRecommendedAction());
    }

    @Test
    @DisplayName("Test 4: Deterministic allocation - identical input yields identical seating order")
    void testDeterministicAllocation() {
        Hall hall = createHall("Exam Hall A", 3, 3, 2); // 18 seats
        List<Seat> seats = generateSeats(hall);

        List<Student> students = new ArrayList<>();
        students.add(createStudent("CS10", "Zack", "Computer Science", 5));
        students.add(createStudent("CS01", "Alice", "Computer Science", 5));
        students.add(createStudent("EC05", "Brian", "Electronics", 5));
        students.add(createStudent("EC01", "Catherine", "Electronics", 5));

        InterleavedSeatingAlgorithm.AllocationResult run1 = algorithm.allocateSeats(testExam, students, seats, "admin");
        InterleavedSeatingAlgorithm.AllocationResult run2 = algorithm.allocateSeats(testExam, students, seats, "admin");

        assertEquals(run1.getAllocations().size(), run2.getAllocations().size());
        for (int i = 0; i < run1.getAllocations().size(); i++) {
            assertEquals(run1.getAllocations().get(i).getStudent().getRollNo(),
                         run2.getAllocations().get(i).getStudent().getRollNo());
            assertEquals(run1.getAllocations().get(i).getSeat().getId(),
                         run2.getAllocations().get(i).getSeat().getId());
        }
    }
}
