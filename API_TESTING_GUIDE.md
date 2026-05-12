# Banking Service - API Testing Guide

This guide will walk you through setting up and testing the Banking Service application step by step.

---

## STEP 1: Database Setup

Before running the application, set up MySQL database:

### 1.1 Create Database
```sql
CREATE DATABASE banking_db;
USE banking_db;
```

### 1.2 Create Tables (Will be auto-created by JPA, but here's the schema)

The application will automatically create tables based on entity mappings when you start it.
- `customer_accounts` table
- `beneficiary_details` table

---

## STEP 2: Start the Banking Service Application

1. Open terminal/command prompt
2. Navigate to the project directory:
   ```
   cd C:\Users\admin\OneDrive\Desktop\HarshStudy_Updated_Data\Banking_Banking_service\banking-service
   ```

3. Run the application:
   ```
   mvn spring-boot:run
   ```

4. Wait for startup message: "Started BankingServiceApplication in X seconds"

5. Application will run on: **http://localhost:8081**

---

## STEP 3: Insert Test Customer Account Data (Using MySQL)

The application needs a customer account to exist first. Insert test data:

```sql
INSERT INTO customer_accounts (customer_id, account_number, account_holder_name, account_status, bank_account_name) 
VALUES (1, 'ACC001', 'John Doe', 'ACTIVE', 'John Doe Savings Account');

INSERT INTO customer_accounts (customer_id, account_number, account_holder_name, account_status, bank_account_name) 
VALUES (2, 'ACC002', 'Jane Smith', 'FROZEN', 'Jane Smith Checking Account');

INSERT INTO customer_accounts (customer_id, account_number, account_holder_name, account_status, bank_account_name) 
VALUES (3, 'ACC003', 'Bob Johnson', 'BLOCKED', 'Bob Johnson Savings Account');
```

---

## STEP 4: Test API Sequence

### API 1: Add Beneficiary (POST)
**Endpoint:** `POST http://localhost:8081/api/beneficiary/add`

**When to Call:** First, to add a new beneficiary for a customer

**Request Body (AddBeneficiaryRequestDTO):**
```json
{
  "customerId": 1,
  "beneficiaryName": "Alice Brown",
  "beneficiaryAccountNumber": "BENE001",
  "confirmAccountNumber": "BENE001",
  "ifscCode": "YESB00088",
  "nickname": "Alice"
}
```

**Expected Response (On Success):**
```json
{
  "message": "Beneficiary added successfully. Will be activated in 1 minute.",
  "beneficiaryStatus": "PENDING_ACTIVATION",
  "activationTime": null
}
```

**Expected Response (On Failure - Account Mismatch):**
```json
{
  "timestamp": "2024-05-12T10:30:45",
  "status": 400,
  "error": "Beneficiary Operation Failed",
  "message": "Beneficiary account number and confirm account number must match",
  "path": "/api/beneficiary/add"
}
```

---

## STEP 5: Test Different Scenarios

### Scenario 1: Successful Beneficiary Addition (ACTIVE Account)

**Request:**
```json
{
  "customerId": 1,
  "beneficiaryName": "Alice Brown",
  "beneficiaryAccountNumber": "BENE001",
  "confirmAccountNumber": "BENE001",
  "ifscCode": "YESB00088",
  "nickname": "Alice"
}
```

**Description:** Customer 1 has ACTIVE account, so beneficiary will be added with PENDING_ACTIVATION status

**Result:** ✅ Success - Beneficiary saved and will become ACTIVE after 1 minute

---

### Scenario 2: Account Numbers Don't Match

**Request:**
```json
{
  "customerId": 1,
  "beneficiaryName": "Bob White",
  "beneficiaryAccountNumber": "BENE002",
  "confirmAccountNumber": "BENE002_WRONG",
  "ifscCode": "YESB00088",
  "nickname": "Bob"
}
```

**Result:** ❌ Error - "Beneficiary account number and confirm account number must match"

---

### Scenario 3: Frozen Account Rejection

**Request:**
```json
{
  "customerId": 2,
  "beneficiaryName": "Charlie Davis",
  "beneficiaryAccountNumber": "BENE003",
  "confirmAccountNumber": "BENE003",
  "ifscCode": "YESB00088",
  "nickname": "Charlie"
}
```

**Description:** Customer 2 (Jane Smith) has FROZEN account

**Result:** ❌ Error - "Beneficiary addition not allowed for FROZEN customer account"

---

### Scenario 4: Blocked Account Rejection

**Request:**
```json
{
  "customerId": 3,
  "beneficiaryName": "Diana Evans",
  "beneficiaryAccountNumber": "BENE004",
  "confirmAccountNumber": "BENE004",
  "ifscCode": "YESB00088",
  "nickname": "Diana"
}
```

**Description:** Customer 3 (Bob Johnson) has BLOCKED account

**Result:** ❌ Error - "Beneficiary addition not allowed for BLOCKED customer account"

---

### Scenario 5: Invalid IFSC Code

**Request:**
```json
{
  "customerId": 1,
  "beneficiaryName": "Eva Frank",
  "beneficiaryAccountNumber": "BENE005",
  "confirmAccountNumber": "BENE005",
  "ifscCode": "INVALID123",
  "nickname": "Eva"
}
```

**Description:** IFSC code doesn't exist or is invalid

**Result:** ❌ Error - "Invalid IFSC code: INVALID123"

---

### Scenario 6: Duplicate Beneficiary

**First Request (Success):**
```json
{
  "customerId": 1,
  "beneficiaryName": "Frank Green",
  "beneficiaryAccountNumber": "BENE006",
  "confirmAccountNumber": "BENE006",
  "ifscCode": "YESB00088",
  "nickname": "Frank"
}
```

**Second Request (Same Details) - Should Fail:**
```json
{
  "customerId": 1,
  "beneficiaryName": "Frank Green",
  "beneficiaryAccountNumber": "BENE006",
  "confirmAccountNumber": "BENE006",
  "ifscCode": "YESB00088",
  "nickname": "Frank"
}
```

**Result:** ❌ Error - "Duplicate beneficiary already exists for this customer"

---

### Scenario 7: More than 10 Active Beneficiaries

**Description:** When a customer already has 10 ACTIVE beneficiaries, adding more will fail.

**Result:** ❌ Error - "Customer cannot have more than 10 active beneficiaries"

---

## STEP 6: Fetch Active Beneficiaries

**API 2: Get Active Beneficiaries (GET)**

**Endpoint:** `GET http://localhost:8081/api/beneficiary/active/1`

**When to Call:** After adding beneficiaries and waiting for them to become ACTIVE (after 1 minute)

**Path Parameter:**
- `customerId`: 1 (Replace with actual customer ID)

**Expected Response (After Beneficiaries Become ACTIVE):**
```json
[
  {
    "beneficiaryId": 1,
    "customerId": 1,
    "beneficiaryName": "Alice Brown",
    "beneficiaryAccountNumber": "BENE001",
    "ifscCode": "YESB00088",
    "bankName": "YES Bank",
    "branchName": "Mumbai Branch",
    "nickname": "Alice",
    "beneficiaryStatus": "ACTIVE",
    "activationTime": "2024-05-12T10:31:45",
    "createdDate": "2024-05-12T10:30:45"
  }
]
```

---

## STEP 7: Timeline - When Beneficiary Becomes ACTIVE

1. **T=0:** Add beneficiary → Status = PENDING_ACTIVATION
2. **T=1 minute:** Scheduled task runs → Status changes to ACTIVE, activationTime is set
3. **T=1+ minute:** Call Get Active Beneficiaries API → Returns the ACTIVE beneficiary

---

## STEP 8: Using Postman or cURL

### Using cURL:

**Add Beneficiary:**
```bash
curl -X POST http://localhost:8081/api/beneficiary/add \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": 1,
    \"beneficiaryName\": \"Alice Brown\",
    \"beneficiaryAccountNumber\": \"BENE001\",
    \"confirmAccountNumber\": \"BENE001\",
    \"ifscCode\": \"YESB00088\",
    \"nickname\": \"Alice\"
  }"
```

**Get Active Beneficiaries:**
```bash
curl -X GET http://localhost:8081/api/beneficiary/active/1 \
  -H "Content-Type: application/json"
```

---

## STEP 9: Validation Rules Summary

| Validation | Rule | Response Code |
|-----------|------|---|
| Account Numbers Match | beneficiaryAccountNumber == confirmAccountNumber | 400 |
| Customer Exists | Customer ID must exist in DB | 400 |
| Account Status | Status should not be BLOCKED or FROZEN | 400 |
| IFSC Valid | Must validate with external API | 400 |
| Own Account Check | Beneficiary can't be own account | 400 |
| No Duplicates | Duplicate check by customerId + accountNumber + IFSC | 400 |
| Max Beneficiaries | Max 10 ACTIVE beneficiaries per customer | 400 |

---

## STEP 10: Complete Testing Workflow

```
1. Start Application
   ↓
2. Insert Test Customer Data into DB
   ↓
3. Call Add Beneficiary API (Scenario 1 - Success)
   ↓
4. Wait 1 minute (or check logs for activation)
   ↓
5. Call Get Active Beneficiaries API
   ↓
6. Verify beneficiary status is ACTIVE
   ↓
7. Test Error Scenarios (2-7) to verify validations
   ↓
8. All tests complete ✅
```

---

## Logs to Watch

When running the application, watch for these logs:

**Successful Add:**
```
2024-05-12 10:30:45 INFO  [BeneficiaryService] Beneficiary added successfully with ID: 1
```

**Activation:**
```
2024-05-12 10:31:45 INFO  [BeneficiaryService] Beneficiary activated: ID 1, Status: ACTIVE
```

**Error:**
```
2024-05-12 10:32:00 ERROR [BeneficiaryService] Validation failed: [error message]
```

---

## Troubleshooting

**Q: Getting connection refused error?**
- A: Ensure MySQL is running and database `banking_db` is created

**Q: IFSC validation failing?**
- A: Ensure the external IFSC validation service is running on `http://localhost:8080/api/validateifsc/{ifscCode}`

**Q: Beneficiary not becoming ACTIVE?**
- A: The scheduled task runs every 60 seconds. Wait at least 1 minute + scheduling interval.

**Q: Port 8081 already in use?**
- A: Change `server.port` in `application.properties` to another port (e.g., 8082)

---

## Summary

**First Call Order:**
1. ✅ **POST /api/beneficiary/add** (Add a beneficiary)
2. ✅ **GET /api/beneficiary/active/{customerId}** (Fetch active beneficiaries after 1 minute)

**Test with RequestDTO:**
- **AddBeneficiaryRequestDTO** for POST request
- No request body needed for GET request

That's it! Follow these steps to test the entire beneficiary management system.

