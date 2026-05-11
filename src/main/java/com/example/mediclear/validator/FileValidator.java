package com.example.mediclear.validator;

import com.example.mediclear.exception.PDFProcessingException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Component
public class FileValidator {
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "application/pdf",
        "application/x-pdf"
    );
    
    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new PDFProcessingException("File cannot be empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new PDFProcessingException(
                String.format("File size exceeds maximum limit of %d MB", 
                             MAX_FILE_SIZE / (1024 * 1024))
            );
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new PDFProcessingException(
                "Invalid file type. Only PDF files are allowed"
            );
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new PDFProcessingException(
                "Invalid file extension. Only .pdf files are allowed"
            );
        }
    }
}
