-- ========================================
-- Database Cleanup and Reset Script
-- ========================================
-- Run this if you get database schema errors

-- Step 1: Drop existing database (WARNING: This deletes everything)
DROP DATABASE IF EXISTS banking_db;

-- Step 2: Create fresh database
CREATE DATABASE banking_db;
USE banking_db;

-- Step 3: Verify database was created
SHOW DATABASES LIKE 'banking_db';

-- ========================================
-- Now the application will auto-create tables
-- 1. Stop the application
-- 2. Run this script
-- 3. Start the application again
-- 4. Tables will be auto-created by Hibernate
-- 5. Run the INSERT statements below to add test data
-- ========================================

-- After application starts and creates tables, insert test data:

INSERT INTO customer_accounts
(customer_id, account_number, account_holder_name, account_status, bank_account_name)
VALUES
(1, 'ACC001', 'John Doe', 'ACTIVE', 'John Doe Savings Account');

INSERT INTO customer_accounts
(customer_id, account_number, account_holder_name, account_status, bank_account_name)
VALUES
(2, 'ACC002', 'Jane Smith', 'FROZEN', 'Jane Smith Checking Account');

INSERT INTO customer_accounts
(customer_id, account_number, account_holder_name, account_status, bank_account_name)
VALUES
(3, 'ACC003', 'Bob Johnson', 'BLOCKED', 'Bob Johnson Savings Account');

-- Verify data was inserted
SELECT * FROM customer_accounts;

