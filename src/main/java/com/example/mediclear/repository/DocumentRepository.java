package com.example.mediclear.repository;

import com.example.mediclear.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByStatus(String status);
    List<Document> findTop10ByOrderByUploadedAtDesc();
}
