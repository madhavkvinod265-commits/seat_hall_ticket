package com.examseat.algorithm;

import com.examseat.entity.Allocation;
import com.examseat.entity.Exam;
import com.examseat.entity.Seat;
import com.examseat.entity.Student;
import com.examseat.exception.InsufficientCapacityException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InterleavedSeatingAlgorithm {

    public static class AllocationResult {
        private final List<Allocation> allocations;
        private final int totalEligibleStudents;
        private final int totalAvailableSeats;
        private final int allocatedCount;
        private final int unassignedCount;
        private final Map<String, Integer> departmentCounts;

        public AllocationResult(List<Allocation> allocations, int totalEligibleStudents, int totalAvailableSeats,
                                int allocatedCount, int unassignedCount, Map<String, Integer> departmentCounts) {
            this.allocations = allocations;
            this.totalEligibleStudents = totalEligibleStudents;
            this.totalAvailableSeats = totalAvailableSeats;
            this.allocatedCount = allocatedCount;
            this.unassignedCount = unassignedCount;
            this.departmentCounts = departmentCounts;
        }

        public List<Allocation> getAllocations() {
            return allocations;
        }

        public int getTotalEligibleStudents() {
            return totalEligibleStudents;
        }

        public int getTotalAvailableSeats() {
            return totalAvailableSeats;
        }

        public int getAllocatedCount() {
            return allocatedCount;
        }

        public int getUnassignedCount() {
            return unassignedCount;
        }

        public Map<String, Integer> getDepartmentCounts() {
            return departmentCounts;
        }
    }

    /**
     * Executes the deterministic Interleaved Round-Robin Seating Algorithm.
     *
     * @param exam Eligible examination
     * @param eligibleStudents All eligible students matching exam departments & semesters
     * @param availableSeats All seats from selected exam halls
     * @param allocatedBy Username or identifier of the staff generating the arrangement
     * @return Deterministic AllocationResult with all allocations and metrics
     */
    public AllocationResult allocateSeats(Exam exam, List<Student> eligibleStudents, List<Seat> availableSeats, String allocatedBy) {
        int totalEligible = (eligibleStudents != null) ? eligibleStudents.size() : 0;
        int totalCapacity = (availableSeats != null) ? availableSeats.size() : 0;

        // Step 6 Check: Verify capacity before allocation
        if (totalEligible > totalCapacity) {
            int excess = totalEligible - totalCapacity;
            String advice = String.format("Add more examination halls to accommodate all %d students, or reduce eligible departments/semesters.", totalEligible);
            throw new InsufficientCapacityException(totalEligible, totalCapacity, excess, advice);
        }

        if (totalEligible == 0) {
            return new AllocationResult(Collections.emptyList(), 0, totalCapacity, 0, 0, Collections.emptyMap());
        }

        // Step 1: Deterministic sort of students by department ASC, then roll number ASC
        List<Student> sortedStudents = new ArrayList<>(eligibleStudents);
        sortedStudents.sort(Comparator
                .comparing(Student::getDepartment, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(Student::getRollNo, Comparator.nullsLast(String::compareToIgnoreCase)));

        // Step 2: Create department queues
        Map<String, Queue<Student>> departmentQueues = new LinkedHashMap<>();
        Map<String, Integer> deptCounts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Student student : sortedStudents) {
            String dept = student.getDepartment().trim();
            departmentQueues.computeIfAbsent(dept, k -> new LinkedList<>()).add(student);
            deptCounts.put(dept, deptCounts.getOrDefault(dept, 0) + 1);
        }

        // Sorted list of departments for deterministic round-robin cycling
        List<String> departmentList = new ArrayList<>(departmentQueues.keySet());
        Collections.sort(departmentList, String.CASE_INSENSITIVE_ORDER);

        // Step 4: Prepare available seats sorted deterministically:
        // Hall Name ASC, Row Number ASC, Bench Number ASC, Position ASC
        List<Seat> sortedSeats = new ArrayList<>(availableSeats);
        sortedSeats.sort(Comparator
                .comparing((Seat s) -> s.getHall().getName(), Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparingInt(Seat::getRowNumber)
                .thenComparingInt(Seat::getBenchNumber)
                .thenComparingInt(Seat::getPosition));

        // Group seats by bench for department alternation: Key = "hallId_row_bench"
        Map<String, List<Seat>> benchMap = new LinkedHashMap<>();
        for (Seat seat : sortedSeats) {
            String benchKey = seat.getHall().getId() + "_" + seat.getRowNumber() + "_" + seat.getBenchNumber();
            benchMap.computeIfAbsent(benchKey, k -> new ArrayList<>()).add(seat);
        }

        // Sort seats within each bench by position ASC
        for (List<Seat> bSeats : benchMap.values()) {
            bSeats.sort(Comparator.comparingInt(Seat::getPosition));
        }

        List<Allocation> allocations = new ArrayList<>(totalEligible);
        Set<UUID> allocatedStudentIds = new HashSet<>();
        Set<UUID> allocatedSeatIds = new HashSet<>();

        int deptIndex = 0;
        int remainingStudents = totalEligible;

        // Iterate bench by bench through all halls
        for (List<Seat> benchSeats : benchMap.values()) {
            if (remainingStudents == 0) {
                break;
            }

            String previousBenchDept = null;

            for (Seat seat : benchSeats) {
                if (remainingStudents == 0) {
                    break;
                }

                // Pick next student with department alternation preference
                Student selectedStudent = pickNextStudent(departmentQueues, departmentList, deptIndex, previousBenchDept);
                if (selectedStudent == null) {
                    break;
                }

                // Advance round robin dept index
                String studentDept = selectedStudent.getDepartment().trim();
                previousBenchDept = studentDept;
                int foundIndex = departmentList.indexOf(studentDept);
                if (foundIndex != -1) {
                    deptIndex = (foundIndex + 1) % departmentList.size();
                }

                // Create Allocation record
                Allocation allocation = new Allocation(exam, selectedStudent, seat, allocatedBy);
                allocations.add(allocation);

                allocatedStudentIds.add(selectedStudent.getId());
                allocatedSeatIds.add(seat.getId());
                remainingStudents--;
            }
        }

        // Step 6: Rigorous verification of allocation results
        verifyAllocationIntegrity(totalEligible, allocations, allocatedStudentIds, allocatedSeatIds);

        int allocatedCount = allocations.size();
        int unassignedCount = totalEligible - allocatedCount;

        return new AllocationResult(allocations, totalEligible, totalCapacity, allocatedCount, unassignedCount, deptCounts);
    }

    /**
     * Picks the next student using interleaved round-robin, giving strong preference to a department
     * different from previousDept on the same bench.
     */
    private Student pickNextStudent(Map<String, Queue<Student>> departmentQueues,
                                    List<String> departmentList,
                                    int startDeptIndex,
                                    String previousDept) {
        int deptCount = departmentList.size();

        // 1. Try to find a student from a department OTHER than previousDept (Round-Robin search)
        if (previousDept != null) {
            for (int i = 0; i < deptCount; i++) {
                int idx = (startDeptIndex + i) % deptCount;
                String dept = departmentList.get(idx);
                if (!dept.equalsIgnoreCase(previousDept)) {
                    Queue<Student> queue = departmentQueues.get(dept);
                    if (queue != null && !queue.isEmpty()) {
                        return queue.poll();
                    }
                }
            }
        }

        // 2. If no alternating department is available (e.g. only one department remains),
        // pick from any available department queue
        for (int i = 0; i < deptCount; i++) {
            int idx = (startDeptIndex + i) % deptCount;
            String dept = departmentList.get(idx);
            Queue<Student> queue = departmentQueues.get(dept);
            if (queue != null && !queue.isEmpty()) {
                return queue.poll();
            }
        }

        return null;
    }

    /**
     * Verifies that the allocations meet all consistency criteria:
     * - No duplicate students
     * - No duplicate seats
     * - Exact count matches eligible students
     */
    private void verifyAllocationIntegrity(int totalEligible, List<Allocation> allocations,
                                           Set<UUID> studentIds, Set<UUID> seatIds) {
        if (allocations.size() != totalEligible) {
            throw new IllegalStateException(String.format(
                    "Seating plan verification failed: Expected %d allocations, but created %d.",
                    totalEligible, allocations.size()));
        }

        if (studentIds.size() != allocations.size()) {
            throw new IllegalStateException("Duplicate student allocation detected during seating arrangement generation!");
        }

        if (seatIds.size() != allocations.size()) {
            throw new IllegalStateException("Duplicate seat allocation detected during seating arrangement generation!");
        }
    }
}
