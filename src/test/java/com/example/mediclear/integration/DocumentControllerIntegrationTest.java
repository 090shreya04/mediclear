package com.example.mediclear.integration;

import com.example.mediclear.dto.ExtractionResult;
import com.example.mediclear.service.PDFExtractionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PDFExtractionService pdfExtractionService;
    
    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/documents/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("MediClear API is running!"));
    }
    
    @Test
    void testExtractPDF_Success() throws Exception {
        ExtractionResult mockResult = ExtractionResult.builder()
                .fileName("test.pdf")
                .rawText("Sample text")
                .pageCount(1)
                .extractionStatus(ExtractionResult.ExtractionStatus.SUCCESS)
                .build();

        when(pdfExtractionService.extractText(any())).thenReturn(mockResult);

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "Sample PDF content".getBytes()
        );
        
        mockMvc.perform(multipart("/api/documents/extract")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test.pdf"))
                .andExpect(jsonPath("$.extractionStatus").value("SUCCESS"));
    }
    
    @Test
    void testGetAllDocuments() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
