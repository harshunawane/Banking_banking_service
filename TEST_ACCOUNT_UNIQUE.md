# 🧪 QUICK TEST: Account Number Uniqueness

## ✅ Test Duplicate Account Number Prevention

### Step 1: Add First Beneficiary
```
POST http://localhost:8081/api/beneficiary/add

{
  "customerId": 101,
  "beneficiaryName": "First User",
  "beneficiaryAccountNumber": "UNIQUE001",
  "confirmAccountNumber": "UNIQUE001",
  "ifscCode": "YESB00088",
  "nickname": "First"
}
```

**Expected:** ✅ Success

### Step 2: Try Same Account Number with Different Customer
```
POST http://localhost:8081/api/beneficiary/add

{
  "customerId": 102,
  "beneficiaryName": "Second User",
  "beneficiaryAccountNumber": "UNIQUE001",  ← SAME ACCOUNT!
  "confirmAccountNumber": "UNIQUE001",
  "ifscCode": "YESB00088",
  "nickname": "Second"
}
```

**Expected Response:**
```json
{
  "timestamp": "2026-05-12T12:58:38.1845702",
  "status": 400,
  "error": "Beneficiary Operation Failed",
  "message": "Beneficiary account number is already in use by another customer",
  "path": "/api/beneficiary/add"
}
```

✅ **Perfect! Duplicate account number blocked!**

### Step 3: Verify in Database
```sql
SELECT beneficiary_account_number, customer_id, beneficiary_name
FROM beneficiary_details
WHERE beneficiary_account_number = 'UNIQUE001';
```

**Expected Result:**
```
beneficiary_account_number | customer_id | beneficiary_name
--------------------------|-------------|-----------------
UNIQUE001                 | 101         | First User
```

Only **1 record** - the duplicate was blocked! ✅

---

## 🎯 Summary

- ✅ **Account numbers must be unique** across all customers
- ✅ **Same account number** cannot be used by multiple customers
- ✅ **Clear error message** when duplicate is attempted
- ✅ **Data integrity** maintained

**Your validation is working perfectly!** 🚀

