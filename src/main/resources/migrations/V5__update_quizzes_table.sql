-- Migration to update quizzes table to match the JPA entity
-- This migration adds missing columns and renames title to name

-- Rename title column to name to match the entity
ALTER TABLE quizzes RENAME COLUMN title TO name;

-- Add missing columns expected by the Quizz entity
ALTER TABLE quizzes ADD COLUMN description TEXT;
ALTER TABLE quizzes ADD COLUMN max_attempts INTEGER DEFAULT 3;
ALTER TABLE quizzes ADD COLUMN passing_score DECIMAL(5,2) DEFAULT 60.00;
ALTER TABLE quizzes ADD COLUMN time_limit_minutes INTEGER DEFAULT 60;

-- Remove total_score column as it's not used in the entity
ALTER TABLE quizzes DROP COLUMN total_score;
