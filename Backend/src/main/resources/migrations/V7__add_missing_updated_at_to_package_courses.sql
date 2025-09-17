-- Add missing updated_at column to package_courses table
-- This is needed for BaseEntity inheritance

ALTER TABLE package_courses 
ADD COLUMN updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;
