package com.example.mediclear.controller;

import com.example.mediclear.dto.ExtractionResult;
import com.example.mediclear.model.Document;
import com.example.mediclear.repository.DocumentRepository;
import com.example.mediclear.service.PDFExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Controller", description = "Healthcare PDF Processing API")
public class DocumentController {

    private final PDFExtractionService pdfExtractionService;
    private final DocumentRepository documentRepository;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Extract text from PDF", description = "Upload a PDF file to extract its content and metadata")
    public ResponseEntity<ExtractionResult> extractPDF(
            @Parameter(description = "PDF file to be processed")
            @RequestPart("file") MultipartFile file) {
        log.info("Incoming request to extract text from file: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            log.warn("Empty file received");
            return ResponseEntity.badRequest().build();
        }

        if (!"application/pdf".equals(file.getContentType())) {
            log.warn("Invalid file type received: {}", file.getContentType());
            return ResponseEntity.badRequest().build();
        }

        ExtractionResult result = pdfExtractionService.extractText(file);
        
        log.info("Response sent with status: 200 for file: {}", file.getOriginalFilename());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get all documents")
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        log.info("Fetching recent documents (top 10)");
        List<Document> documents = documentRepository.findTop10ByOrderByUploadedAtDesc();
        return ResponseEntity.ok(documents);
    }
    
    @Operation(summary = "Get document by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable Long id) {
        log.info("Fetching document with ID: {}", id);
        return documentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    @Operation(summary = "API Health Check", description = "Simple endpoint to check if the MediClear API is running")
    public String healthCheck() {
        log.info("Health check endpoint called");
        return "MediClear API is running!";
    }
}
