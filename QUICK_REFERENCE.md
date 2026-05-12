# Quick Reference - Banking Service API Endpoints

## Overview
This banking service has 2 main APIs for managing beneficiaries:

---

## API #1: Add Beneficiary
**First API to Call**

| Property | Value |
|----------|-------|
| **Method** | POST |
| **URL** | http://localhost:8081/api/beneficiary/add |
| **Purpose** | Add a new beneficiary for a customer |
| **Status After Success** | PENDING_ACTIVATION |

### Request DTO (AddBeneficiaryRequestDTO)
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

### Response DTO (AddBeneficiaryResponseDTO)
```json
{
  "message": "Beneficiary added successfully. Will be activated in 1 minute.",
  "beneficiaryStatus": "PENDING_ACTIVATION",
  "activationTime": null
}
```

### Validation Checks Performed:
1. ✅ Account numbers must match (beneficiaryAccountNumber == confirmAccountNumber)
2. ✅ Customer account must exist
3. ✅ Customer account status must be ACTIVE (not BLOCKED or FROZEN)
4. ✅ IFSC code validation with external service
5. ✅ Beneficiary account cannot be customer's own account
6. ✅ No duplicate beneficiary allowed
7. ✅ Maximum 10 active beneficiaries per customer

### Error Response Example:
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

## API #2: Get Active Beneficiaries
**Second API to Call (after 1 minute)**

| Property | Value |
|----------|-------|
| **Method** | GET |
| **URL** | http://localhost:8081/api/beneficiary/active/{customerId} |
| **Purpose** | Fetch all ACTIVE beneficiaries for a customer |
| **Path Parameter** | customerId (e.g., 1) |

### Request
```
GET http://localhost:8081/api/beneficiary/active/1
No request body needed
```

### Response (List of BeneficiaryDetails)
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

## Test Data Setup

### Insert Customer Accounts First:
```sql
INSERT INTO customer_accounts (customer_id, account_number, account_holder_name, account_status, bank_account_name) 
VALUES 
(1, 'ACC001', 'John Doe', 'ACTIVE', 'John Doe Savings Account'),
(2, 'ACC002', 'Jane Smith', 'FROZEN', 'Jane Smith Checking Account'),
(3, 'ACC003', 'Bob Johnson', 'BLOCKED', 'Bob Johnson Savings Account');
```

---

## Step-by-Step Execution

### Step 1: Start Application
```bash
cd C:\Users\admin\OneDrive\Desktop\HarshStudy_Updated_Data\Banking_Banking_service\banking-service
mvn spring-boot:run
```

### Step 2: Insert Test Data
Execute the SQL INSERT statements above in MySQL

### Step 3: Call Add Beneficiary API
```bash
POST http://localhost:8081/api/beneficiary/add
Body: AddBeneficiaryRequestDTO (see above)
```

### Step 4: Wait 1 Minute
Beneficiary status will automatically change from PENDING_ACTIVATION to ACTIVE

### Step 5: Call Get Active Beneficiaries API
```bash
GET http://localhost:8081/api/beneficiary/active/1
```

### Step 6: Verify Response
You should see the beneficiary with status ACTIVE

---

## Common Errors & Solutions

| Error Message | Cause | Solution |
|---------------|-------|----------|
| Account numbers must match | confirmAccountNumber ≠ beneficiaryAccountNumber | Ensure both account numbers are identical |
| Customer account not found | customerId doesn't exist in DB | Insert customer account first |
| Not allowed for FROZEN account | Account is FROZEN | Use customer with ACTIVE status |
| Not allowed for BLOCKED account | Account is BLOCKED | Use customer with ACTIVE status |
| Invalid IFSC code | IFSC validation failed | Use valid IFSC like YESB00088 |
| Duplicate beneficiary exists | Same beneficiary added before | Use different account number or IFSC |
| More than 10 beneficiaries | Already have 10 active | Delete or deactivate existing ones |
| Connection refused | Application not running | Start application with `mvn spring-boot:run` |

---

## Key Points

- **First API:** POST /api/beneficiary/add (adds beneficiary)
- **Second API:** GET /api/beneficiary/active/{customerId} (fetches active ones)
- **Activation Time:** 1 minute after adding
- **Database:** MySQL with banking_db
- **Port:** 8081
- **External API:** IFSC validation service on port 8080

---

## Testing Tips

- Use different customerId values to test multiple customers
- Wait exactly 1 minute between adding beneficiary and fetching active list
- Try error scenarios to understand validation
- Check application logs for detailed error information
- Use Postman or cURL for testing APIs

