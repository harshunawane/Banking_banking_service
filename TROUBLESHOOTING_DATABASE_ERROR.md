# Troubleshooting: Database Schema Error

## Error Message
```
could not execute statement [Field 'beneficiary_id' doesn't have...
```

## Root Cause
The database table schema doesn't match the Java entity definition. This happens when:
- Old tables exist from a previous run with different structure
- The `ddl-auto` setting is set to `update` instead of recreating tables
- Manual SQL table creation doesn't match the entity annotations

---

## Solution Steps

### Step 1: Stop the Application
- Terminate the running application (Ctrl+C in terminal)

### Step 2: Reset the Database
Run one of these options:

**Option A: Clean Database Reset (Recommended)**

Execute this SQL in MySQL:
```sql
DROP DATABASE IF EXISTS banking_db;
CREATE DATABASE banking_db;
```

**Option B: Using the Provided Script**

Run the file: `DATABASE_RESET.sql` in MySQL

### Step 3: Verify Configuration
Check that `application.properties` has:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/banking_db?...
spring.jpa.hibernate.ddl-auto=create-drop
```

✅ Database name: `banking_db`
✅ ddl-auto: `create-drop` (will auto-create tables)

### Step 4: Restart Application
```bash
cd C:\Users\admin\OneDrive\Desktop\HarshStudy_Updated_Data\Banking_Banking_service\banking-service
mvn clean spring-boot:run
```

### Step 5: Wait for Startup
Watch for this message:
```
Started BankingServiceApplication in X.XXX seconds
```

**Do NOT proceed until you see this message**

### Step 6: Insert Test Data (In MySQL)
Once application is running, execute:

```sql
USE banking_db;

INSERT INTO customer_accounts 
(account_number, account_holder_name, account_status, bank_account_name) 
VALUES 
('ACC001', 'John Doe', 'ACTIVE', 'John Doe Savings Account');

INSERT INTO customer_accounts 
(account_number, account_holder_name, account_status, bank_account_name) 
VALUES 
('ACC002', 'Jane Smith', 'FROZEN', 'Jane Smith Checking Account');

INSERT INTO customer_accounts 
(account_number, account_holder_name, account_status, bank_account_name) 
VALUES 
('ACC003', 'Bob Johnson', 'BLOCKED', 'Bob Johnson Savings Account');
```

**Note:** Don't specify `customer_id` - let it AUTO_INCREMENT

### Step 7: Verify Tables
```sql
SHOW TABLES IN banking_db;

-- Should show:
-- customer_accounts
-- beneficiary_details
```

### Step 8: Verify Data
```sql
SELECT * FROM customer_accounts;

-- Should show 3 rows
```

### Step 9: Test API
Now test the API:
```
POST http://localhost:8081/api/beneficiary/add
```

---

## How the Fix Works

| Setting | Before | After | Effect |
|---------|--------|-------|--------|
| Database | banking_service | banking_db | Uses correct database |
| ddl-auto | update | create-drop | Auto-recreates tables on startup |

**create-drop:**
- Drops all tables when app shuts down ✓
- Recreates all tables when app starts ✓
- Ensures schema always matches entities ✓
- Good for development ✓

---

## What to Expect

### When Application Starts:
```
2026-05-12 10:15:00 INFO  [main] org.hibernate.tool.schema.SchemaCreationObserver
                          : HHH000476: Creating all new database schema objects
2026-05-12 10:15:00 DEBUG [main] org.hibernate.SQL
                          : drop table if exists beneficiary_details
2026-05-12 10:15:00 DEBUG [main] org.hibernate.SQL
                          : drop table if exists customer_accounts
2026-05-12 10:15:00 DEBUG [main] org.hibernate.SQL
                          : create table customer_accounts (...)
2026-05-12 10:15:00 DEBUG [main] org.hibernate.SQL
                          : create table beneficiary_details (...)
```

This is ✅ **NORMAL** and **EXPECTED**

### After Data Insertion:
```
mysql> SELECT * FROM customer_accounts;
+-------------+----------+-------------------+----------+-----------------------------+
| customer_id | account_number | account_holder_name | account_status | bank_account_name |
+-------------+----------+-------------------+----------+-----------------------------+
| 1           | ACC001        | John Doe             | ACTIVE         | John Doe Savings Account    |
| 2           | ACC002        | Jane Smith           | FROZEN         | Jane Smith Checking Account |
| 3           | ACC003        | Bob Johnson          | BLOCKED        | Bob Johnson Savings Account |
+-------------+----------+-------------------+----------+-----------------------------+
```

---

## If Error Still Persists

### Check 1: MySQL Version
Ensure MySQL 5.7 or higher:
```sql
SELECT VERSION();
```

### Check 2: JDBC Driver
Check pom.xml has MySQL connector:
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Check 3: Credentials
Verify MySQL username and password:
```bash
mysql -u root -p
-- Type your password when prompted
```

### Check 4: Port
Verify MySQL is running on port 3306:
```bash
netstat -an | findstr :3306
```

### Check 5: Clean Build
```bash
mvn clean install
mvn clean spring-boot:run
```

---

## Quick Fix Checklist

- [ ] Stop the application
- [ ] Drop database: `DROP DATABASE IF EXISTS banking_db;`
- [ ] Create database: `CREATE DATABASE banking_db;`
- [ ] Verify `application.properties` settings
- [ ] Start application: `mvn clean spring-boot:run`
- [ ] Wait for "Started BankingServiceApplication" message
- [ ] Run INSERT statements for test data
- [ ] Verify data: `SELECT * FROM customer_accounts;`
- [ ] Test API endpoint

---

## Prevention Tips

💡 **Remember:**
- Always use `create-drop` or `create` for development
- Use `update` only in production with careful schema planning
- Always backup your database before making changes
- Check logs for Hibernate table creation messages

---

## Still Having Issues?

Check the application logs for:
```
Error creating bean with name 'dataSource'
Error executing DDL
Could not execute statement
```

These indicate database connectivity or schema issues.

**Solution:** Repeat the database reset steps above.

