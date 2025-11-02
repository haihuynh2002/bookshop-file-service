-- Create file metadata table
CREATE TABLE file (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255),
    file_path TEXT NOT NULL,
    size BIGINT NOT NULL CHECK (size >= 0),
    type VARCHAR(255) NOT NULL,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    CONSTRAINT unique_filename UNIQUE (filename)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_file_owner_id ON file(owner_id);
CREATE INDEX IF NOT EXISTS idx_file_book_original ON file(owner_id, original_filename);