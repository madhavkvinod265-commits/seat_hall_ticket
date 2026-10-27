package com.examseat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientCapacityException extends RuntimeException {

    private final int requiredSeats;
    private final int availableSeats;
    private final int excessStudents;
    private final String recommendedAction;

    public InsufficientCapacityException(int requiredSeats, int availableSeats, int excessStudents, String recommendedAction) {
        super(String.format("Insufficient seating capacity! Required: %d seats, Available: %d seats, Excess students: %d. %s",
                requiredSeats, availableSeats, excessStudents, recommendedAction));
        this.requiredSeats = requiredSeats;
        this.availableSeats = availableSeats;
        this.excessStudents = excessStudents;
        this.recommendedAction = recommendedAction;
    }

    public int getRequiredSeats() {
        return requiredSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public int getExcessStudents() {
        return excessStudents;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }
}
