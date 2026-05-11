package com.example.mediclear.service.impl;

import com.example.mediclear.dto.ExtractionResult;
import com.example.mediclear.exception.PDFProcessingException;
import com.example.mediclear.model.Document;
import com.example.mediclear.repository.DocumentRepository;
import com.example.mediclear.service.PDFExtractionService;
import com.example.mediclear.validator.FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class PDFExtractionServiceImpl implements PDFExtractionService {

    @Value("${app.upload.dir}")
    private String uploadDir;
    
    private final FileValidator fileValidator;
    private final DocumentRepository documentRepository;

    @Override
    public ExtractionResult extractText(MultipartFile file) {
        fileValidator.validate(file);
        
        log.info("Starting text extraction for file: {}", file.getOriginalFilename());

        // Ensure upload directory exists as an absolute path
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        File uploadDirectory = uploadPath.toFile();
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        // Generate unique file name and absolute file path
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File targetFile = uploadPath.resolve(fileName).toFile();
        String filePath = targetFile.getAbsolutePath();

        try {
            log.info("Saving file to: {}", filePath);
            file.transferTo(targetFile);
        } catch (IOException e) {
            log.error("Failed to save file: {}", fileName, e);
            throw new PDFProcessingException("Failed to save file", e);
        }
        
        // Create database record
        Document document = Document.builder()
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileSizeBytes(file.getSize())
                .status("PROCESSING")
                .build();
        
        document = documentRepository.save(document);
        
        try {
            // Extract text from the SAVED file
            String text = extractTextFromPDF(targetFile);
            int pageCount = getPageCount(targetFile);
            
            // Update document
            document.setExtractedText(text);
            document.setPageCount(pageCount);
            document.setStatus("COMPLETED");
            documentRepository.save(document);
            
            log.info("Successfully processed and saved document: {}", file.getOriginalFilename());

            return ExtractionResult.builder()
                    .fileName(file.getOriginalFilename())
                    .rawText(text)
                    .pageCount(pageCount)
                    .fileSizeBytes(file.getSize())
                    .extractionStatus(ExtractionResult.ExtractionStatus.SUCCESS)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during text extraction for file: {}", file.getOriginalFilename(), e);
            // Update document with error
            document.setStatus("FAILED");
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);
            
            throw new PDFProcessingException("Failed to extract text", e);
        }
    }
    
    private String extractTextFromPDF(File file) throws IOException {
        try (PDDocument pdDocument = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdDocument);
        }
    }
    
    private int getPageCount(File file) throws IOException {
        try (PDDocument pdDocument = Loader.loadPDF(file)) {
            return pdDocument.getNumberOfPages();
        }
    }
    
}
