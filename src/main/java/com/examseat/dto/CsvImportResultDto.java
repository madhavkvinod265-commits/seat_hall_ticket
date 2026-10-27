package com.examseat.dto;

import java.util.ArrayList;
import java.util.List;

public class CsvImportResultDto {

    private int totalProcessed;
    private int insertedCount;
    private int skippedCount;
    private int rejectedCount;
    private List<String> errorMessages = new ArrayList<>();

    public CsvImportResultDto() {
    }

    public CsvImportResultDto(int totalProcessed, int insertedCount, int skippedCount, int rejectedCount) {
        this.totalProcessed = totalProcessed;
        this.insertedCount = insertedCount;
        this.skippedCount = skippedCount;
        this.rejectedCount = rejectedCount;
    }

    public int getTotalProcessed() {
        return totalProcessed;
    }

    public void setTotalProcessed(int totalProcessed) {
        this.totalProcessed = totalProcessed;
    }

    public int getInsertedCount() {
        return insertedCount;
    }

    public void setInsertedCount(int insertedCount) {
        this.insertedCount = insertedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }
}
