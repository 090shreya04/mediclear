package com.example.mediclear.service;

import com.example.mediclear.dto.ExtractionResult;
import org.springframework.web.multipart.MultipartFile;

public interface PDFExtractionService {
    ExtractionResult extractText(MultipartFile file);
}
