-- Migration to add Currency enum type and update existing currency columns
-- This migration adds a new currency_type enum and converts existing currency columns

-- Create currency enum type
CREATE TYPE currency_type AS ENUM (
    'USD', 'EUR', 'GBP', 'JPY', 'AUD', 'CAD', 'CHF', 'CNY', 
    'SEK', 'NZD', 'MXN', 'SGD', 'HKD', 'NOK', 'KRW', 'TRY', 
    'RUB', 'INR', 'BRL', 'ZAR', 'ILS'
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
