package com.example.mediclear.controller;

import com.example.mediclear.dto.ExtractionResult;
import com.example.mediclear.service.PDFExtractionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DocumentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PDFExtractionService pdfExtractionService;

    @Test
    void testExtractPDF_Success() throws Exception {
        ExtractionResult mockResult = ExtractionResult.builder()
                .fileName("test.pdf")
                .rawText("Extracted Text")
                .pageCount(1)
                .extractionStatus(ExtractionResult.ExtractionStatus.SUCCESS)
                .build();

        when(pdfExtractionService.extractText(any())).thenReturn(mockResult);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );

        mockMvc.perform(multipart("/api/documents/extract").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test.pdf"))
                .andExpect(jsonPath("$.extractionStatus").value("SUCCESS"));
    }

    @Test
    void testExtractPDF_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/documents/extract").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testExtractPDF_InvalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "dummy content".getBytes()
        );

        mockMvc.perform(multipart("/api/documents/extract").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testExtractPDF_FileTooLarge() throws Exception {
        // Simulating the exception thrown by Spring when file exceeds limit
        when(pdfExtractionService.extractText(any())).thenThrow(new MaxUploadSizeExceededException(1024L));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                new byte[1024]
        );

        mockMvc.perform(multipart("/api/documents/extract").file(file))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.message").value("File size exceeds the maximum limit of 10MB"));
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/documents/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("MediClear API is running!"));
    }
}
