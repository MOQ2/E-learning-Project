
-- Enable vector extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS vector;

-- Course embeddings table for RAG system
CREATE TABLE course_embeddings (
    course_id BIGINT PRIMARY KEY,
    embedding vector(384) NOT NULL,
    CONSTRAINT fk_course
        FOREIGN KEY(course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE
);

-- Chat history table for persistent memory in RAG chatbot
CREATE TABLE chat_history (
    id BIGSERIAL PRIMARY KEY,
    chat_id VARCHAR(255) UNIQUE NOT NULL,
    messages JSONB,
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_course_embeddings_course_id ON course_embeddings(course_id);
CREATE INDEX idx_course_embeddings_embedding ON course_embeddings USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_chat_history_chat_id ON chat_history(chat_id);
CREATE INDEX idx_chat_history_updated_at ON chat_history(updated_at);
