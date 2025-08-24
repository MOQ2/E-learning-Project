-- Add missing columns to simple_payments table to match SimplePayment entity

-- Add final_amount column
ALTER TABLE simple_payments 
ADD COLUMN final_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

-- Add subscription_duration_months column (rename from subscription_duration_days)
ALTER TABLE simple_payments 
ADD COLUMN subscription_duration_months INTEGER;

-- Remove the old subscription_duration_days column if it exists
ALTER TABLE simple_payments 
DROP COLUMN IF EXISTS subscription_duration_days;

-- Add Stripe payment integration columns
ALTER TABLE simple_payments 
ADD COLUMN stripe_payment_intent_id VARCHAR(255);

ALTER TABLE simple_payments 
ADD COLUMN stripe_session_id VARCHAR(255);

-- Remove old columns that don't match the entity
ALTER TABLE simple_payments 
DROP COLUMN IF EXISTS payment_method;

ALTER TABLE simple_payments 
DROP COLUMN IF EXISTS transaction_id;

ALTER TABLE simple_payments 
DROP COLUMN IF EXISTS processed_at;

ALTER TABLE simple_payments 
DROP COLUMN IF EXISTS is_active;

-- Rename original_amount to just amount and add payment_date
ALTER TABLE simple_payments 
DROP COLUMN IF EXISTS original_amount;

ALTER TABLE simple_payments 
ADD COLUMN payment_date TIMESTAMPTZ;
