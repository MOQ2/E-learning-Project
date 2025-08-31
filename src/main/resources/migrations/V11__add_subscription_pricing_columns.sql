-- Migration to add subscription pricing columns for different durations
-- File: V11__add_subscription_pricing_columns.sql

-- Add subscription pricing columns to courses table
ALTER TABLE courses
ADD COLUMN subscription_price_3_months DECIMAL(10, 2),
ADD COLUMN subscription_price_6_months DECIMAL(10, 2);

-- Add subscription pricing columns to packages table
ALTER TABLE packages
ADD COLUMN subscription_price_monthly DECIMAL(10, 2),
ADD COLUMN subscription_price_3_months DECIMAL(10, 2),
ADD COLUMN subscription_price_6_months DECIMAL(10, 2),
ADD COLUMN allows_subscription BOOLEAN DEFAULT false;

-- Add comments for clarity
COMMENT ON COLUMN courses.subscription_price_3_months IS 'Price for 3-month subscription access to the course';
COMMENT ON COLUMN courses.subscription_price_6_months IS 'Price for 6-month subscription access to the course';
COMMENT ON COLUMN packages.subscription_price_monthly IS 'Price for 1-month subscription access to the package';
COMMENT ON COLUMN packages.subscription_price_3_months IS 'Price for 3-month subscription access to the package';
COMMENT ON COLUMN packages.subscription_price_6_months IS 'Price for 6-month subscription access to the package';
COMMENT ON COLUMN packages.allows_subscription IS 'Whether the package supports subscription-based access';
