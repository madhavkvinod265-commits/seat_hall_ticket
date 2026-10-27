package com.examseat.service;

import com.examseat.dto.HallDto;
import com.examseat.dto.HallLayoutDto;
import com.examseat.dto.HallRequest;
import com.examseat.dto.SeatDto;
import com.examseat.entity.Hall;
import com.examseat.entity.Seat;
import com.examseat.exception.ConflictException;
import com.examseat.exception.InvalidOperationException;
import com.examseat.exception.ResourceNotFoundException;
import com.examseat.repository.AllocationRepository;
import com.examseat.repository.HallRepository;
import com.examseat.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HallService {

    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final AllocationRepository allocationRepository;

    public HallService(HallRepository hallRepository,
                       SeatRepository seatRepository,
                       AllocationRepository allocationRepository) {
        this.hallRepository = hallRepository;
        this.seatRepository = seatRepository;
        this.allocationRepository = allocationRepository;
    }

    @Transactional(readOnly = true)
    public List<HallDto> getAllHalls() {
        return hallRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HallDto> getActiveHalls() {
        return hallRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HallDto getHallById(UUID id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination hall not found with ID: " + id));
        return toDto(hall);
    }

    @Transactional(readOnly = true)
    public HallLayoutDto getHallLayout(UUID id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination hall not found with ID: " + id));

        List<Seat> seats = seatRepository.findByHallIdOrderByRowNumberAscBenchNumberAscPositionAsc(id);
        List<SeatDto> seatDtos = seats.stream()
                .map(s -> new SeatDto(s.getId(), hall.getId(), hall.getName(), s.getRowNumber(), s.getBenchNumber(), s.getPosition()))
                .collect(Collectors.toList());

        return new HallLayoutDto(
                hall.getId(),
                hall.getName(),
                hall.getBuilding(),
                hall.getFloor(),
                hall.getRows(),
                hall.getBenchesPerRow(),
                hall.getSeatsPerBench(),
                hall.getCapacity(),
                seatDtos
        );
    }

    @Transactional
    public HallDto createHall(HallRequest request) {
        if (hallRepository.existsByName(request.getName().trim())) {
            throw new ConflictException("An examination hall with name '" + request.getName() + "' already exists");
        }

        Hall hall = new Hall(
                request.getName().trim(),
                request.getBuilding().trim(),
                request.getFloor().trim(),
                request.getRows(),
                request.getBenchesPerRow(),
                request.getSeatsPerBench()
        );
        hall.setActive(request.getActive() != null ? request.getActive() : true);

        Hall savedHall = hallRepository.save(hall);

        // Generate physical Seat records for the hall
        generateSeatsForHall(savedHall);

        return toDto(savedHall);
    }

    @Transactional
    public HallDto updateHall(UUID id, HallRequest request) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination hall not found with ID: " + id));

        // Check if dimension change is requested
        boolean dimensionsChanged = !hall.getRows().equals(request.getRows()) ||
                !hall.getBenchesPerRow().equals(request.getBenchesPerRow()) ||
                !hall.getSeatsPerBench().equals(request.getSeatsPerBench());

        if (dimensionsChanged) {
            // Check if hall is referenced by finalized allocations
            if (allocationRepository.existsFinalizedAllocationForHall(id)) {
                throw new InvalidOperationException("Cannot alter seating dimensions of hall '" + hall.getName() +
                        "' because finalized examination seating arrangements are actively mapped to it.");
            }
        }

        hall.setName(request.getName().trim());
        hall.setBuilding(request.getBuilding().trim());
        hall.setFloor(request.getFloor().trim());
        hall.setActive(request.getActive() != null ? request.getActive() : true);

        if (dimensionsChanged) {
            hall.setRows(request.getRows());
            hall.setBenchesPerRow(request.getBenchesPerRow());
            hall.setSeatsPerBench(request.getSeatsPerBench());
            hall.setCapacity(Hall.calculateCapacity(request.getRows(), request.getBenchesPerRow(), request.getSeatsPerBench()));

            // Regenerate seats
            seatRepository.deleteByHallId(id);
            generateSeatsForHall(hall);
        }

        Hall updated = hallRepository.save(hall);
        return toDto(updated);
    }

    @Transactional
    public void deleteHall(UUID id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination hall not found with ID: " + id));

        if (allocationRepository.existsFinalizedAllocationForHall(id)) {
            throw new InvalidOperationException("Cannot delete hall '" + hall.getName() +
                    "' because finalized examinations are linked to its seats.");
        }

        seatRepository.deleteByHallId(id);
        hallRepository.delete(hall);
    }

    private void generateSeatsForHall(Hall hall) {
        List<Seat> seats = new ArrayList<>(hall.getCapacity());
        for (int r = 1; r <= hall.getRows(); r++) {
            for (int b = 1; b <= hall.getBenchesPerRow(); b++) {
                for (int p = 1; p <= hall.getSeatsPerBench(); p++) {
                    seats.add(new Seat(hall, r, b, p));
                }
            }
        }
        seatRepository.saveAll(seats);
    }

    private HallDto toDto(Hall hall) {
        return new HallDto(
                hall.getId(),
                hall.getName(),
                hall.getBuilding(),
                hall.getFloor(),
                hall.getRows(),
                hall.getBenchesPerRow(),
                hall.getSeatsPerBench(),
                hall.getCapacity(),
                hall.getActive()
        );
    }
}
