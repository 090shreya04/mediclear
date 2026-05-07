package com.example.mediclear.service.impl;

import com.example.mediclear.dto.ExtractionResult;
import com.example.mediclear.exception.PDFProcessingException;
import com.example.mediclear.service.PDFExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Service
public class PDFExtractionServiceImpl implements PDFExtractionService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    @Override
    public ExtractionResult extractText(MultipartFile file) {
        validateFile(file);

        log.info("Starting text extraction for file: {}", file.getOriginalFilename());

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            int pageCount = document.getNumberOfPages();

            log.info("Successfully extracted text from {}. Page count: {}", file.getOriginalFilename(), pageCount);

            return ExtractionResult.builder()
                    .fileName(file.getOriginalFilename())
                    .rawText(text)
                    .pageCount(pageCount)
                    .fileSizeBytes(file.getSize())
                    .extractionStatus(ExtractionResult.ExtractionStatus.SUCCESS)
                    .build();

        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", file.getOriginalFilename(), e);
            throw new PDFProcessingException("Error processing PDF file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new PDFProcessingException("File is empty or null");
        }

        if (!Objects.equals(file.getContentType(), PDF_CONTENT_TYPE)) {
            log.warn("Invalid file type: {}", file.getContentType());
            throw new PDFProcessingException("Only PDF files are supported");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File size exceeds limit: {} bytes", file.getSize());
            throw new PDFProcessingException("File size exceeds the maximum limit of 10MB");
        }
    }
}
