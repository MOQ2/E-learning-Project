-- Migration to simplify subscription system
-- This migration removes complex subscription tables and replaces with simplified structure

-- ================================
-- 1. DROP EXISTING COMPLEX TABLES
-- ================================

-- Drop dependent tables first (foreign key order)
DROP TABLE IF EXISTS plan_course_access CASCADE;
DROP TABLE IF EXISTS course_enrolments CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS subscriptions CASCADE;
DROP TABLE IF EXISTS subscription_plans CASCADE;

-- Drop related enum types
DROP TYPE IF EXISTS subscription_status CASCADE;
DROP TYPE IF EXISTS payment_status CASCADE;
DROP TYPE IF EXISTS payment_type CASCADE;
DROP TYPE IF EXISTS enrollment_status CASCADE;
DROP TYPE IF EXISTS enrollment_access_type CASCADE;

-- ================================
-- 2. CREATE SIMPLIFIED STRUCTURE
-- ================================

-- Course packages (bundles of courses)
CREATE TABLE packages (
    id SERIAL PRIMARY KEY,
    name VARCHAR(250) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    discount_percentage DECIMAL(5, 2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Courses included in packages
CREATE TABLE package_courses (
    id SERIAL PRIMARY KEY,
    package_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_package_courses_package
        FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE,
    CONSTRAINT fk_package_courses_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT unique_package_course 
        UNIQUE(package_id, course_id)
);

-- Promotion codes for discounts
CREATE TABLE promotion_codes (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    discount_percentage DECIMAL(5, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    max_uses INTEGER,
    current_uses INTEGER DEFAULT 0,
    valid_from TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMPTZ,
    applicable_to_courses BOOLEAN DEFAULT TRUE,
    applicable_to_packages BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_discount_valid CHECK (
        (discount_percentage > 0 AND discount_percentage <= 100) OR 
        (discount_amount > 0)
    )
);

-- Simplified payment types
CREATE TYPE simple_payment_type AS ENUM ('COURSE_PURCHASE', 'PACKAGE_PURCHASE', 'SUBSCRIPTION');
CREATE TYPE simple_payment_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');

-- Simplified payments table
CREATE TABLE simple_payments (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    course_id INTEGER,
    package_id INTEGER,
    
    -- Payment details
    payment_type simple_payment_type NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    original_amount DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    promotion_code_id INTEGER,
    
    -- Payment status
    status simple_payment_status DEFAULT 'PENDING',
    
    -- Subscription duration (only for subscription type) - in days
    subscription_duration_days INTEGER,
    
    -- Payment processing
    payment_method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    processed_at TIMESTAMPTZ,
    
    -- Audit fields
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Foreign keys
    CONSTRAINT fk_simple_payments_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_simple_payments_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL,
    CONSTRAINT fk_simple_payments_package
        FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE SET NULL,
    CONSTRAINT fk_simple_payments_promotion
        FOREIGN KEY (promotion_code_id) REFERENCES promotion_codes(id) ON DELETE SET NULL,
        
    -- Business logic constraints
    CONSTRAINT chk_payment_target CHECK (
        (payment_type = 'COURSE_PURCHASE' AND course_id IS NOT NULL) OR
        (payment_type = 'PACKAGE_PURCHASE' AND package_id IS NOT NULL) OR
        (payment_type = 'SUBSCRIPTION' AND subscription_duration_days IS NOT NULL)
    )
);

-- User access to courses (simplified enrollments)
CREATE TYPE access_type AS ENUM ('PURCHASED', 'PACKAGE_ACCESS', 'SUBSCRIPTION_ACCESS', 'FREE');
CREATE TYPE enrollment_status AS ENUM ('ACTIVE', 'EXPIRED', 'SUSPENDED');

CREATE TABLE user_course_access (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    payment_id INTEGER,
    
    -- Access details
    access_type access_type NOT NULL,
    status enrollment_status DEFAULT 'ACTIVE',
    
    -- Access period
    access_granted_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    access_expires_at TIMESTAMPTZ,
    
    -- Progress tracking
    last_accessed_at TIMESTAMPTZ,
    completion_percentage DECIMAL(5, 2) DEFAULT 0.00,
    completed_at TIMESTAMPTZ,
    
    -- Audit fields
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Foreign keys
    CONSTRAINT fk_user_access_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_user_access_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT,
    CONSTRAINT fk_user_access_payment
        FOREIGN KEY (payment_id) REFERENCES simple_payments(id) ON DELETE SET NULL,
        
    -- Unique constraint
    CONSTRAINT unique_user_course_access 
        UNIQUE(user_id, course_id)
);

-- ================================
-- 3. UPDATE COURSES TABLE
-- ================================

-- Remove access_model enum and add simple pricing
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'courses' AND column_name = 'access_model') THEN
        ALTER TABLE courses DROP COLUMN access_model;
    END IF;
END $$;

-- Add subscription pricing option
ALTER TABLE courses ADD COLUMN IF NOT EXISTS subscription_price_monthly DECIMAL(10, 2) DEFAULT 0.00;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS allows_subscription BOOLEAN DEFAULT FALSE;

-- Update the is_free calculation to include subscription option
ALTER TABLE courses DROP COLUMN IF EXISTS is_free;
ALTER TABLE courses ADD COLUMN is_free BOOLEAN GENERATED ALWAYS AS (
    one_time_price = 0.00 AND subscription_price_monthly = 0.00
) STORED;

-- ================================
-- 4. CREATE INDEXES FOR PERFORMANCE
-- ================================

-- Packages indexes
CREATE INDEX idx_packages_active ON packages(is_active, price);

-- Package courses indexes
CREATE INDEX idx_package_courses_package ON package_courses(package_id);
CREATE INDEX idx_package_courses_course ON package_courses(course_id);

-- Promotion codes indexes
CREATE INDEX idx_promotion_codes_code ON promotion_codes(code) WHERE is_active = true;
CREATE INDEX idx_promotion_codes_valid ON promotion_codes(valid_from, valid_until) WHERE is_active = true;
CREATE INDEX idx_promotion_codes_uses ON promotion_codes(current_uses, max_uses) WHERE is_active = true;

-- Simple payments indexes
CREATE INDEX idx_simple_payments_user ON simple_payments(user_id, status) WHERE is_active = true;
CREATE INDEX idx_simple_payments_course ON simple_payments(course_id) WHERE course_id IS NOT NULL;
CREATE INDEX idx_simple_payments_package ON simple_payments(package_id) WHERE package_id IS NOT NULL;
CREATE INDEX idx_simple_payments_type ON simple_payments(payment_type, status) WHERE is_active = true;
CREATE INDEX idx_simple_payments_processed ON simple_payments(processed_at) WHERE processed_at IS NOT NULL;

-- User course access indexes
CREATE INDEX idx_user_access_user ON user_course_access(user_id, status) WHERE is_active = true;
CREATE INDEX idx_user_access_course ON user_course_access(course_id, status) WHERE is_active = true;
CREATE INDEX idx_user_access_type ON user_course_access(access_type) WHERE is_active = true;
CREATE INDEX idx_user_access_expiry ON user_course_access(access_expires_at) WHERE access_expires_at IS NOT NULL;
CREATE INDEX idx_user_access_progress ON user_course_access(completion_percentage) WHERE is_active = true;
CREATE INDEX idx_user_access_last_accessed ON user_course_access(last_accessed_at);

-- Composite indexes for common queries
CREATE INDEX idx_user_active_courses ON user_course_access(user_id, status, access_expires_at) WHERE is_active = true;
CREATE INDEX idx_course_active_users ON user_course_access(course_id, status, access_granted_at) WHERE is_active = true;
CREATE INDEX idx_payments_revenue ON simple_payments(processed_at, amount, payment_type) WHERE status = 'COMPLETED';
