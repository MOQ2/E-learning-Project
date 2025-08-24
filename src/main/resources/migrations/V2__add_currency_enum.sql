-- Migration to add Currency enum type and update existing currency columns
-- This migration adds a new currency_type enum and converts existing currency columns

-- Create currency enum type
CREATE TYPE currency_type AS ENUM (
    'USD', 'EUR','GOD', 'ILS'
);

-- Update subscription_plans table
ALTER TABLE subscription_plans 
ALTER COLUMN currency TYPE currency_type USING currency::currency_type;

-- Update courses table  
ALTER TABLE courses 
ALTER COLUMN currency TYPE currency_type USING currency::currency_type;

-- Update payments table
ALTER TABLE payments 
ALTER COLUMN currency TYPE currency_type USING currency::currency_type;
