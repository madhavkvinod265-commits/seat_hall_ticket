package com.examseat.dto;

import java.util.ArrayList;
import java.util.List;

public class CsvImportPreviewDto {

    private int totalRows;
    private int validRowsCount;
    private int invalidRowsCount;
    private int duplicateRowsCount;
    private List<StudentCsvRow> previewRows = new ArrayList<>();
    private List<String> globalErrors = new ArrayList<>();

    public CsvImportPreviewDto() {
    }

    public CsvImportPreviewDto(int totalRows, int validRowsCount, int invalidRowsCount, int duplicateRowsCount, List<StudentCsvRow> previewRows) {
        this.totalRows = totalRows;
        this.validRowsCount = validRowsCount;
        this.invalidRowsCount = invalidRowsCount;
        this.duplicateRowsCount = duplicateRowsCount;
        this.previewRows = previewRows;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getValidRowsCount() {
        return validRowsCount;
    }

    public void setValidRowsCount(int validRowsCount) {
        this.validRowsCount = validRowsCount;
    }

    public int getInvalidRowsCount() {
        return invalidRowsCount;
    }

    public void setInvalidRowsCount(int invalidRowsCount) {
        this.invalidRowsCount = invalidRowsCount;
    }

    public int getDuplicateRowsCount() {
        return duplicateRowsCount;
    }

    public void setDuplicateRowsCount(int duplicateRowsCount) {
        this.duplicateRowsCount = duplicateRowsCount;
    }

    public List<StudentCsvRow> getPreviewRows() {
        return previewRows;
    }

    public void setPreviewRows(List<StudentCsvRow> previewRows) {
        this.previewRows = previewRows;
    }

    public List<String> getGlobalErrors() {
        return globalErrors;
    }

    public void setGlobalErrors(List<String> globalErrors) {
        this.globalErrors = globalErrors;
    }
}
