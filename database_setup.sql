-- ========================================
-- Banking Service Database Setup Script
-- ========================================
-- Run this script in MySQL to set up the database and test data

-- Step 1: Create Database
CREATE DATABASE IF NOT EXISTS banking_db;
USE banking_db;

-- Step 2: Create Tables
-- Note: Tables will be auto-created by JPA/Hibernate on first run
-- But here's the structure for reference:

-- Customer Accounts Table
CREATE TABLE IF NOT EXISTS customer_accounts (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    account_holder_name VARCHAR(100) NOT NULL,
    account_status VARCHAR(20) NOT NULL,
    bank_account_name VARCHAR(100) NOT NULL
);

-- Beneficiary Details Table
CREATE TABLE IF NOT EXISTS beneficiary_details (
    beneficiary_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    beneficiary_name VARCHAR(100) NOT NULL,
    beneficiary_account_number VARCHAR(50) NOT NULL,
    ifsc_code VARCHAR(20) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    beneficiary_status VARCHAR(20) NOT NULL,
    activation_time DATETIME,
    created_date DATETIME NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customer_accounts(customer_id)
);

-- Step 3: Insert Test Data

-- Insert Customer Accounts
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

-- Step 4: Verify Data (Optional - Run to check)
-- SELECT * FROM customer_accounts;

-- ========================================
-- Test Data Details:
-- ========================================
-- Customer 1: John Doe
--   - Account Number: ACC001
--   - Status: ACTIVE (can add beneficiaries)
--   - Use this customer for SUCCESS scenarios
--
-- Customer 2: Jane Smith
--   - Account Number: ACC002
--   - Status: FROZEN (cannot add beneficiaries)
--   - Use this customer to test FROZEN account rejection
--
-- Customer 3: Bob Johnson
--   - Account Number: ACC003
--   - Status: BLOCKED (cannot add beneficiaries)
--   - Use this customer to test BLOCKED account rejection
-- ========================================

-- Step 5: For Testing - Sample Beneficiary Insert (Optional)
-- This will be created by the API, but here's the format:
-- INSERT INTO beneficiary_details
-- (customer_id, beneficiary_name, beneficiary_account_number, ifsc_code, bank_name, branch_name,
--  nickname, beneficiary_status, activation_time, created_date)
-- VALUES
-- (1, 'Alice Brown', 'BENE001', 'YESB00088', 'YES Bank', 'Mumbai Branch', 'Alice', 'ACTIVE', NOW(), NOW());

-- ========================================
-- Cleanup Commands (Optional - Use if needed)
-- ========================================
-- Drop all data:
-- DELETE FROM beneficiary_details;
-- DELETE FROM customer_accounts;

-- Drop tables:
-- DROP TABLE IF EXISTS beneficiary_details;
-- DROP TABLE IF EXISTS customer_accounts;

-- Drop database (WARNING: This will delete everything):
-- DROP DATABASE IF EXISTS banking_db;

-- ========================================
-- End of Setup Script
-- ========================================

