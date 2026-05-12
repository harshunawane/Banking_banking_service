# 🎯 QUICK START CARD

## 🚨 You Got Database Error? 

### FIX IT NOW (3 Steps - 5 Minutes)

**Step 1:** MySQL - Run this
```sql
DROP DATABASE IF EXISTS banking_db;
CREATE DATABASE banking_db;
```

**Step 2:** Terminal
```bash
cd C:\Users\admin\OneDrive\Desktop\HarshStudy_Updated_Data\Banking_Banking_service\banking-service
mvn clean spring-boot:run
```

**Step 3:** MySQL - After app starts, run this
```sql
USE banking_db;

INSERT INTO customer_accounts (account_number, account_holder_name, account_status, bank_account_name) 
VALUES ('ACC001', 'John Doe', 'ACTIVE', 'John Doe Savings Account'),
       ('ACC002', 'Jane Smith', 'FROZEN', 'Jane Smith Checking Account'),
       ('ACC003', 'Bob Johnson', 'BLOCKED', 'Bob Johnson Savings Account');
```

✅ **Done! Error is fixed.**

---

## 📱 Test the APIs

### API #1: Add Beneficiary (Right After Fixing)

**URL:** POST `http://localhost:8081/api/beneficiary/add`

**Body:**
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

**Response (Success):**
```json
{
  "message": "Beneficiary added successfully. Will be activated in 1 minute.",
  "beneficiaryStatus": "PENDING_ACTIVATION",
  "activationTime": null
}
```

### API #2: Get Active Beneficiaries (After 1 Minute)

**URL:** GET `http://localhost:8081/api/beneficiary/active/1`

**Response (Success):**
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
    "activationTime": "2026-05-12T10:31:45",
    "createdDate": "2026-05-12T10:30:45"
  }
]
```

---

## 🔍 Verify Everything Works

**Check 1:** Can you see 3 customers in database?
```sql
SELECT * FROM customer_accounts;
```
✅ Should show 3 rows

**Check 2:** Did POST return 200?
✅ Should return success response

**Check 3:** After 1 minute, is beneficiary ACTIVE?
```sql
SELECT * FROM beneficiary_details WHERE beneficiary_status = 'ACTIVE';
```
✅ Should show the beneficiary

---

## ⚠️ Common Errors & Fixes

| Error | Fix |
|-------|-----|
| `Field 'beneficiary_id' doesn't have` | Follow 3 steps above |
| `Unknown database 'banking_db'` | Run: `CREATE DATABASE banking_db;` |
| `Port 8081 already in use` | Change port in application.properties |
| `Connection refused` | Ensure MySQL is running |
| `IFSC validation fails` | Make sure IFSC API is running on :8080 |

---

## 📖 Need More Help?

**Setup Issues?**
→ Read: `FIX_DATABASE_ERROR_NOW.md`

**API Questions?**
→ Read: `QUICK_REFERENCE.md`

**Full Testing?**
→ Read: `API_TESTING_GUIDE.md`

**Everything else?**
→ Read: `README.md`

---

## ✅ Success Checklist

- [ ] MySQL running
- [ ] Database `banking_db` created
- [ ] Application started (shows "Started BankingServiceApplication")
- [ ] 3 customer records inserted
- [ ] POST API returns 200
- [ ] Beneficiary created with PENDING_ACTIVATION
- [ ] Waited 1+ minute
- [ ] GET API returns list with ACTIVE status
- [ ] All tests passed ✅

---

## 🚀 You're Ready!

**Status: ✅ WORKING**

**Next:** Test all error scenarios in `API_TESTING_GUIDE.md`

---

**Need in-depth help?** Read: `COMPLETION_SUMMARY.md`

**Questions?** Check: `README.md` for file index

