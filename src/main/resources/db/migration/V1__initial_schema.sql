CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    extracted_text TEXT,
    page_count INTEGER,
    file_size_bytes BIGINT,
    status VARCHAR(50) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    error_message TEXT
);

CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_uploaded_at ON documents(uploaded_at DESC);
