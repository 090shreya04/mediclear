package com.example.mediclear.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExtractionResult {
    private String fileName;
    private String rawText;
    private int pageCount;
    private long fileSizeBytes;
    private ExtractionStatus extractionStatus;

    public enum ExtractionStatus {
        SUCCESS,
        FAILURE
    }
}
