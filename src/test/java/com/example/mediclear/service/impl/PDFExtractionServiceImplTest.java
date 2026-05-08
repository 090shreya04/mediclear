package com.example.mediclear.service.impl;

import com.example.mediclear.dto.ExtractionResult;
import com.example.mediclear.exception.PDFProcessingException;
import com.example.mediclear.model.Document;
import com.example.mediclear.repository.DocumentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PDFExtractionServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private PDFExtractionServiceImpl pdfExtractionService;

    @BeforeEach
    void setUp() {
        // Set the @Value field manually for the test
        ReflectionTestUtils.setField(pdfExtractionService, "uploadDir", "test-uploads");
    }

    @Test
    void extractText_Success() throws IOException {
        // Mock the repository save calls to return the entity
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Create a simple PDF in memory
        byte[] pdfBytes;
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Hello Mediclear");
                contentStream.endText();
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            pdfBytes = baos.toByteArray();
        }

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                pdfBytes
        );

        ExtractionResult result = pdfExtractionService.extractText(file);

        assertNotNull(result);
        assertEquals("test.pdf", result.getFileName());
        assertTrue(result.getRawText().contains("Hello Mediclear"));
        assertEquals(1, result.getPageCount());
        assertEquals(ExtractionResult.ExtractionStatus.SUCCESS, result.getExtractionStatus());

        // Verify repository interactions (initial save + update save)
        verify(documentRepository, atLeast(2)).save(any(Document.class));
    }

    @Test
    void extractText_InvalidFileType_ThrowsException() {
        MultipartFile file = new MockMultipartFile(
                "test.txt",
                "test.txt",
                "text/plain",
                "some text".getBytes()
        );

        assertThrows(PDFProcessingException.class, () -> pdfExtractionService.extractText(file));
    }

    @Test
    void extractText_FileTooLarge_ThrowsException() {
        byte[] largeBytes = new byte[11 * 1024 * 1024]; // 11MB
        MultipartFile file = new MockMultipartFile(
                "large.pdf",
                "large.pdf",
                "application/pdf",
                largeBytes
        );

        assertThrows(PDFProcessingException.class, () -> pdfExtractionService.extractText(file));
    }

    @Test
    void extractText_EmptyFile_ThrowsException() {
        MultipartFile file = new MockMultipartFile(
                "empty.pdf",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(PDFProcessingException.class, () -> pdfExtractionService.extractText(file));
    }
}
