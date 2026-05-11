package com.example.mediclear.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocumentUploadRequest {
    
    @NotNull(message = "File cannot be null")
    private MultipartFile file;
}
