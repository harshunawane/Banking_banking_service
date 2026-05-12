# Banking Service - Complete Summary & Architecture

## 📋 Project Overview

The Banking Service is a Spring Boot application that manages beneficiary accounts for customers. It provides secure beneficiary management with comprehensive validation and automated activation.

---

## 🏗️ Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENT (Postman/Browser)                 │
└──────────────────────┬──────────────────────────────────────┘
                       │
       ┌───────────────┴───────────────┐
       │                               │
   POST REQUEST                    GET REQUEST
   /add                           /active/{id}
       │                               │
       ▼                               ▼
┌────────────────────────────────────────────────┐
│         BeneficiaryController                  │
│  (HTTP Request Handler)                        │
└────────────────────────────────────────────────┘
       │                               │
       ▼                               ▼
┌────────────────────────────────────────────────┐
│         BeneficiaryService                     │
│  (Business Logic & Validations)                │
│  - addBeneficiary()                            │
│  - getActiveBeneficiaries()                    │
│  - activatePendingBeneficiaries() [Scheduled] │
└────────────────────────────────────────────────┘
       │                               │
       ├─────────────────┬─────────────┤
       │                 │             │
       ▼                 ▼             ▼
┌─────────────────┐ ┌──────────────┐ ┌──────────────────┐
│   Validation    │ │ IFSC Service │ │  Repositories    │
│   Engine        │ │              │ │  - Customer ACC  │
│ (7 validations) │ │ RestTemplate │ │  - Beneficiary   │
└─────────────────┘ └──────────────┘ └──────────────────┘
       │                 │                      │
       │                 │                      ▼
       │                 │            ┌────────────────────┐
       │                 │            │   MySQL Database   │
       │                 ▼            │  - customer_accts  │
       │        ┌──────────────────┐  │  - beneficiary_    │
       │        │ External IFSC    │  │    details         │
       │        │ Validation API   │  └────────────────────┘
       │        │ :8080/api/       │
       │        │ validateifsc/    │
       │        └──────────────────┘
       │
       ▼
   Response
```

---

## 🔧 Tech Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.5.14 |
| Language | Java 17 |
| ORM | Hibernate/JPA |
| Database | MySQL 8.0+ |
| HTTP Client | RestTemplate |
| Annotations | Lombok |
| Build Tool | Maven |
| Scheduling | Spring @Scheduled |

---

## 📁 Project Structure

```
banking-service/
├── src/main/java/com/banking_service/
│   ├── controller/
│   │   └── BeneficiaryController.java      ← API Endpoints
│   ├── service/
│   │   ├── BeneficiaryService.java         ← Business Logic
│   │   └── IFSCValidationService.java      ← IFSC Validation
│   ├── entity/
│   │   ├── CustomerAccount.java            ← Entity
│   │   └── BeneficiaryDetails.java         ← Entity
│   ├── dto/
│   │   ├── AddBeneficiaryRequestDTO.java   ← Request DTO
│   │   ├── AddBeneficiaryResponseDTO.java  ← Response DTO
│   │   └── IFSCValidationResponseDTO.java  ← External API DTO
│   ├── repository/
│   │   ├── CustomerAccountRepository.java  ← Data Access
│   │   └── BeneficiaryRepository.java      ← Data Access
│   ├── exception/
│   │   ├── BeneficiaryException.java       ← Custom Exception
│   │   └── GlobalExceptionHandler.java     ← Error Handling
│   └── BankingServiceApplication.java      ← Main Application
├── src/main/resources/
│   └── application.properties               ← Configuration
├── pom.xml                                  ← Maven Dependencies
└── Documentation Files (This Folder)
```

---

## 🔐 Validation Rules

When adding a beneficiary, the system validates:

| # | Rule | Error Message |
|---|------|---------------|
| 1 | Account numbers must match | "Account numbers must match" |
| 2 | Customer account must exist | "Customer account not found" |
| 3 | Account status must be ACTIVE | "Not allowed for BLOCKED/FROZEN" |
| 4 | IFSC code must be valid | "Invalid IFSC code" |
| 5 | Can't add own account as beneficiary | "Cannot be same as customer's account" |
| 6 | No duplicate beneficiaries | "Duplicate beneficiary exists" |
| 7 | Max 10 active beneficiaries | "Cannot have more than 10 beneficiaries" |

---

## ⏱️ Beneficiary Lifecycle

```
CREATION         ACTIVATION PROCESS              FINAL STATE
   │                                                  │
   ▼                                                  ▼
┌─────────────────────────────────────────────────────┐
│ User submits Add Beneficiary request                │
└────────────────┬────────────────────────────────────┘
                 │
                 ▼
         ✓ Validations Pass
                 │
                 ▼
    ┌─────────────────────────────┐
    │ Beneficiary Created         │
    │ Status: PENDING_ACTIVATION  │
    │ CreatedDate: T=0            │
    │ ActivationTime: null        │
    └────────────────┬────────────┘
                     │
            Wait 1 minute...
                     │
                     ▼
    ┌─────────────────────────────┐
    │ Scheduled Task Runs         │
    │ (Every 60 seconds)          │
    │ Checks PENDING items        │
    │ > 1 minute old              │
    └────────────────┬────────────┘
                     │
                     ▼
    ┌─────────────────────────────┐
    │ Beneficiary Activated       │
    │ Status: ACTIVE              │
    │ ActivationTime: T=1 minute  │
    └─────────────────────────────┘
                     │
                     ▼
           Available in API queries
```

---

## 🚀 Complete Execution Flow

### FLOW 1: Add Beneficiary Successfully

```
1. USER SUBMITS REQUEST
   POST /api/beneficiary/add
   {
     customerId: 1,
     beneficiaryName: "Alice",
     beneficiaryAccountNumber: "BENE001",
     confirmAccountNumber: "BENE001",
     ifscCode: "YESB00088",
     nickname: "Alice"
   }
   ↓
2. CONTROLLER RECEIVES REQUEST
   BeneficiaryController.addBeneficiary()
   ↓
3. SERVICE LAYER VALIDATION
   ✓ Check account numbers match
   ✓ Fetch customer account from DB
   ✓ Check account status (ACTIVE/FROZEN/BLOCKED)
   ✓ Call external IFSC validation API
   ✓ Verify not own account
   ✓ Check no duplicate exists
   ✓ Check < 10 active beneficiaries
   ↓
4. ALL VALIDATIONS PASS
   ↓
5. SAVE BENEFICIARY
   - Status: PENDING_ACTIVATION
   - CreatedDate: NOW()
   - Save to database
   ↓
6. RETURN RESPONSE
   {
     message: "Beneficiary added. Will activate in 1 minute",
     beneficiaryStatus: "PENDING_ACTIVATION",
     activationTime: null
   }
```

### FLOW 2: Scheduled Activation

```
1. SPRING SCHEDULER TRIGGERS (Every 60 seconds)
   ↓
2. activatePendingBeneficiaries() Method Runs
   ↓
3. QUERY DATABASE
   SELECT * FROM beneficiary_details
   WHERE status = 'PENDING_ACTIVATION'
   AND createdDate < NOW() - 1 MINUTE
   ↓
4. FOR EACH PENDING BENEFICIARY
   - Update status to ACTIVE
   - Set activationTime = NOW()
   - Save to database
   ↓
5. LOG COMPLETION
```

### FLOW 3: Get Active Beneficiaries

```
1. USER REQUESTS ACTIVE BENEFICIARIES
   GET /api/beneficiary/active/1
   ↓
2. CONTROLLER RECEIVES REQUEST
   BeneficiaryController.getActiveBeneficiaries(1)
   ↓
3. SERVICE VALIDATES CUSTOMER EXISTS
   ✓ Query customer account from DB
   ↓
4. FETCH ACTIVE BENEFICIARIES
   SELECT * FROM beneficiary_details
   WHERE customerId = 1
   AND beneficiaryStatus = 'ACTIVE'
   ↓
5. RETURN LIST OF BENEFICIARIES
   [
     {
       beneficiaryId: 1,
       customerId: 1,
       beneficiaryName: "Alice Brown",
       beneficiaryAccountNumber: "BENE001",
       ifscCode: "YESB00088",
       bankName: "YES Bank",
       branchName: "Mumbai Branch",
       nickname: "Alice",
       beneficiaryStatus: "ACTIVE",
       activationTime: "2024-05-12T10:31:45",
       createdDate: "2024-05-12T10:30:45"
     }
   ]
```

---

## 📝 API Summary

### Endpoint 1: Add Beneficiary
- **URL:** `POST http://localhost:8081/api/beneficiary/add`
- **Input:** `AddBeneficiaryRequestDTO`
- **Output:** `AddBeneficiaryResponseDTO`
- **Status:** Returns 200 on success, 400 on validation error
- **Processing Time:** < 1 second (after external API call)

### Endpoint 2: Get Active Beneficiaries
- **URL:** `GET http://localhost:8081/api/beneficiary/active/{customerId}`
- **Input:** Path parameter `customerId`
- **Output:** List of `BeneficiaryDetails`
- **Status:** Returns 200 on success, 400 if customer not found
- **Processing Time:** < 500ms

---

## 🗄️ Database Schema

### customer_accounts Table
```sql
customer_id (PK, INT)
├── account_number (VARCHAR, UNIQUE)
├── account_holder_name (VARCHAR)
├── account_status (VARCHAR) - ACTIVE/FROZEN/BLOCKED
└── bank_account_name (VARCHAR)
```

### beneficiary_details Table
```sql
beneficiary_id (PK, INT)
├── customer_id (FK, INT)
├── beneficiary_name (VARCHAR)
├── beneficiary_account_number (VARCHAR)
├── ifsc_code (VARCHAR)
├── bank_name (VARCHAR)
├── branch_name (VARCHAR)
├── nickname (VARCHAR)
├── beneficiary_status (VARCHAR) - PENDING_ACTIVATION/ACTIVE/INACTIVE
├── activation_time (DATETIME)
└── created_date (DATETIME)
```

---

## 🧪 Test Data Reference

| Customer | ID | Account # | Status | Purpose |
|----------|----|-----------  |--------|---------|
| John Doe | 1 | ACC001 | ACTIVE | ✅ Test success scenarios |
| Jane Smith | 2 | ACC002 | FROZEN | ❌ Test frozen rejection |
| Bob Johnson | 3 | ACC003 | BLOCKED | ❌ Test blocked rejection |

---

## 📖 Documentation Files

| File | Purpose |
|------|---------|
| `API_TESTING_GUIDE.md` | Comprehensive step-by-step testing guide |
| `QUICK_REFERENCE.md` | Quick API reference and error codes |
| `Banking_Service_Postman_Collection.json` | Ready-to-import Postman collection |
| `database_setup.sql` | Database and test data setup script |
| `ARCHITECTURE_SUMMARY.md` | This file (architecture overview) |

---

## 🎯 Quick Start Checklist

- [ ] MySQL installed and running
- [ ] Create database: `CREATE DATABASE banking_db;`
- [ ] Run `database_setup.sql` to create tables and insert test data
- [ ] Run application: `mvn spring-boot:run`
- [ ] Verify startup: "Started BankingServiceApplication in X seconds"
- [ ] Import Postman collection
- [ ] Test API #1: Add Beneficiary (POST)
- [ ] Wait 1 minute
- [ ] Test API #2: Get Active Beneficiaries (GET)
- [ ] Verify response contains ACTIVE beneficiary

---

## 🐛 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| "Connection refused" | Ensure MySQL is running |
| "Unknown database 'banking_db'" | Run `database_setup.sql` |
| "Port 8081 already in use" | Change port in `application.properties` |
| "IFSC validation fails" | Ensure external API is running on :8080 |
| "Beneficiary not appearing" | Wait 1+ minute for scheduled activation |

---

## 📞 Key Classes Reference

| Class | Purpose |
|-------|---------|
| `BeneficiaryController` | Handles HTTP requests |
| `BeneficiaryService` | Core business logic & validations |
| `IFSCValidationService` | External API integration |
| `BeneficiaryRepository` | Database queries |
| `BeneficiaryDetails` | Entity model |
| `AddBeneficiaryRequestDTO` | Request format |
| `AddBeneficiaryResponseDTO` | Response format |

---

## ✅ Acceptance Criteria Checklist

- [x] Beneficiary added with PENDING_ACTIVATION status
- [x] After 1 minute, status changes to ACTIVE automatically
- [x] Duplicate beneficiaries are rejected
- [x] Invalid IFSC codes are rejected
- [x] FROZEN accounts cannot add beneficiaries
- [x] BLOCKED accounts cannot add beneficiaries
- [x] Maximum 10 active beneficiaries per customer
- [x] Account numbers must match (validation)
- [x] Cannot add own account as beneficiary
- [x] Bank details fetched from external IFSC API
- [x] Inactive beneficiaries can be reactivated
- [x] API to fetch all active beneficiaries

---

**Project Status:** ✅ COMPLETE

All requirements implemented with clean architecture, comprehensive validation, and proper error handling.

