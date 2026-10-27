package com.examseat.service;

import com.examseat.dto.HallDto;
import com.examseat.dto.HallRequest;
import com.examseat.entity.Hall;
import com.examseat.exception.ConflictException;
import com.examseat.exception.InvalidOperationException;
import com.examseat.repository.AllocationRepository;
import com.examseat.repository.HallRepository;
import com.examseat.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HallServiceTest {

    @Mock
    private HallRepository hallRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private AllocationRepository allocationRepository;

    private HallService hallService;

    @BeforeEach
    void setUp() {
        hallService = new HallService(hallRepository, seatRepository, allocationRepository);
    }

    @Test
    @DisplayName("Hall capacity calculation: rows * benchesPerRow * seatsPerBench")
    void testCapacityCalculation() {
        // 5 rows x 4 benches x 2 seats = 40
        assertEquals(40, Hall.calculateCapacity(5, 4, 2));

        // 6 rows x 5 benches x 2 seats = 60
        assertEquals(60, Hall.calculateCapacity(6, 5, 2));

        // Null checks
        assertEquals(0, Hall.calculateCapacity(null, 4, 2));
    }

    @Test
    @DisplayName("Automatic seat creation when a hall is created")
    void testAutomaticSeatCreation() {
        HallRequest request = new HallRequest("Main Hall", "Building A", "Ground Floor", 3, 2, 2, true);

        when(hallRepository.existsByName("Main Hall")).thenReturn(false);
        when(hallRepository.save(any(Hall.class))).thenAnswer(invocation -> {
            Hall h = invocation.getArgument(0);
            h.setId(UUID.randomUUID());
            return h;
        });

        HallDto result = hallService.createHall(request);

        assertNotNull(result);
        assertEquals("Main Hall", result.getName());
        assertEquals(12, result.getCapacity()); // 3 * 2 * 2 = 12

        // Verify seatRepository.saveAll was called with 12 seats
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(seatRepository, times(1)).saveAll(captor.capture());
        List savedSeats = captor.getValue();
        assertEquals(12, savedSeats.size());
    }

    @Test
    @DisplayName("Duplicate hall name rejection")
    void testDuplicateHallNameRejection() {
        HallRequest request = new HallRequest("Aryabhata", "Block A", "1st Floor", 5, 4, 2, true);
        when(hallRepository.existsByName("Aryabhata")).thenReturn(true);

        assertThrows(ConflictException.class, () -> hallService.createHall(request));
        verify(hallRepository, never()).save(any(Hall.class));
    }

    @Test
    @DisplayName("Prevent altering layout when finalized allocations exist")
    void testPreventLayoutChangeWhenFinalizedAllocationsExist() {
        UUID hallId = UUID.randomUUID();
        Hall existingHall = new Hall("Hall 1", "Block A", "1st Floor", 5, 4, 2);
        existingHall.setId(hallId);

        when(hallRepository.findById(hallId)).thenReturn(Optional.of(existingHall));
        when(allocationRepository.existsFinalizedAllocationForHall(hallId)).thenReturn(true);

        // Attempt to alter rows from 5 to 6
        HallRequest updateRequest = new HallRequest("Hall 1", "Block A", "1st Floor", 6, 4, 2, true);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                hallService.updateHall(hallId, updateRequest));

        assertTrue(ex.getMessage().contains("Cannot alter seating dimensions"));
    }
}
