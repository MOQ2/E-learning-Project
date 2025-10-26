-- Fix user_course_access table to match UserCourseAccess entity

-- Drop columns that don't exist in the entity
ALTER TABLE user_course_access 
DROP COLUMN IF EXISTS status,
DROP COLUMN IF EXISTS access_granted_at,
DROP COLUMN IF EXISTS access_expires_at,
DROP COLUMN IF EXISTS last_accessed_at,
DROP COLUMN IF EXISTS completion_percentage,
DROP COLUMN IF EXISTS completed_at;

-- Add access_until column (instead of access_expires_at)
ALTER TABLE user_course_access 
ADD COLUMN IF NOT EXISTS access_until TIMESTAMPTZ;

-- Add is_active column
ALTER TABLE user_course_access 
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- Add package_id column for package access tracking
ALTER TABLE user_course_access 
ADD COLUMN IF NOT EXISTS package_id INTEGER;

-- Add foreign key constraint for package_id
ALTER TABLE user_course_access 
ADD CONSTRAINT fk_user_access_package
    FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE SET NULL;

-- Drop the unique constraint and recreate it to allow multiple accesses per user-course
ALTER TABLE user_course_access 
DROP CONSTRAINT IF EXISTS unique_user_course_access;
