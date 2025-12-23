# Local Development Setup - Configuration Changes

## Overview
This document describes the changes made to enable the myBillOne application to run successfully in a local development environment without requiring external services like Stripe and Firebase.

## Date: December 22, 2025

---

## 1. Password Authentication Changes

### Issue
The application was using BCrypt password hashing, but the database initialization script had inconsistent password hashes that didn't match the expected password "test12345".

### Solution
**File Modified:** `grabbill-backend/grabbill-server/src/main/java/com/grabbill/server/security/SecurityConfiguration.java`

Changed the password encoder from BCrypt to NoOpPasswordEncoder to allow plain text passwords in local development:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    // Using NoOpPasswordEncoder for local development (plain text passwords)
    // WARNING: This is NOT secure for production!
    return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
}
```

**Database Changes:**
- All user passwords in the `user` and `admin_user` tables are now stored as plain text: `test12345`

**⚠️ IMPORTANT:** This change is ONLY for local development. For production, you MUST:
1. Revert to BCryptPasswordEncoder
2. Generate proper BCrypt hashes for all passwords
3. Update the database initialization script with the correct hashes

---

## 2. Stripe Integration Bypass

### Issue
The application was attempting to create/lookup Stripe customers for all accounts during login, which failed because:
- No valid Stripe API keys were configured
- Local development doesn't require payment processing

### Solution
**File Modified:** `grabbill-backend/grabbill-server/src/main/java/com/grabbill/server/service/AuthServiceImpl.java`

Added payment exemption checks to skip Stripe API calls:

```java
// Skip Stripe customer lookup/creation for payment exempted accounts
if (!account.isPaymentExempted()) {
    customer = customerService.getOrCreate(account);
}

// Only check Stripe payment method if customer exists (non-payment-exempted accounts)
com.stripe.model.PaymentMethod defaultPaymentMethod = null;
if (customer != null) {
    defaultPaymentMethod = customerPaymentMethodService
            .getDefaultPaymentMethodByCustomerId(customer.getId());
}
```

**Database Changes:**
- Set `payment_exempted = 1` for the demo account (id=1) in the `account` table

**Configuration:**
- Added `payment.stripe.mode=test` to `application.properties`
- Placeholder Stripe API keys remain in configuration but are not used for exempted accounts

---

## 3. Firebase Integration Made Optional

### Issue
The application required a valid Firebase service account JSON file for push notifications, which wasn't available in local development.

### Solution
**File Modified:** `grabbill-backend/grabbill-server/src/main/java/com/grabbill/server/GrabbillServerApplication.java`

Wrapped Firebase initialization in a try-catch block to make it optional:

```java
@PostConstruct
public void init() throws IOException {
    // Firebase initialization - optional for local development
    try {
        FirebaseApp.initializeApp(
                FirebaseOptions.builder().setCredentials(
                        GoogleCredentials.fromStream(
                                new ClassPathResource(
                                        firebaseServiceAccountFileName,
                                        GrabbillServerApplication.class.getClassLoader()
                                ).getInputStream()
                        )
                ).build()
        );
    } catch (Exception e) {
        System.err.println("WARNING: Firebase initialization failed. Push notifications will not work.");
        System.err.println("Error: " + e.getMessage());
    }

    Stripe.apiKey = "live".equals(mode) ? stripeLiveApiKey: stripeTestApiKey;
}
```

**File Created:** `grabbill-backend/grabbill-server/src/main/resources/firebase-mock.json`
- A mock Firebase service account file (with invalid credentials)
- Allows the application to start even if Firebase initialization fails

---

## 4. Application Properties Updates

### File Modified: `grabbill-backend/grabbill-server/src/main/resources/application.properties`

**Changes Made:**

1. **Database Driver Specification:**
   ```properties
   spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
   ```

2. **Database Dialect Update:**
   ```properties
   spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect
   ```
   (Changed from `MySQL5InnoDBDialect` for better MariaDB compatibility)

3. **Stripe Mode Configuration:**
   ```properties
   payment.stripe.mode=test
   ```

**File Unignored:**
- Removed `application.properties` from `.gitignore` to allow version control

---

## 5. Start/Stop Scripts Enhancement

### File Modified: `start-local.ps1`

**Improvements:**

1. **HOME Environment Variable:**
   ```powershell
   $env:HOME = $env:USERPROFILE
   ```
   Ensures Docker Compose path expansion works correctly on Windows

2. **Process Cleanup:**
   ```powershell
   # Stop any existing instances to prevent port conflicts
   $javaProcs = Get-Process -Name java -ErrorAction SilentlyContinue
   if ($javaProcs) {
       $javaProcs | Stop-Process -Force -ErrorAction SilentlyContinue
   }
   $nodeProcs = Get-Process -Name node -ErrorAction SilentlyContinue
   if ($nodeProcs) {
       $nodeProcs | Stop-Process -Force -ErrorAction SilentlyContinue
   }
   ```
   Prevents multiple instances from conflicting

3. **MariaDB Readiness Check:**
   ```powershell
   # Verify host port 3307 is accessible from host
   $portReady = $false
   $portRetries = 0
   while (-not $portReady -and $portRetries -lt 15) {
       try {
           $tcpClient = New-Object System.Net.Sockets.TcpClient
           $tcpClient.Connect("localhost", 3307)
           $tcpClient.Close()
           $portReady = $true
       } catch {
           Start-Sleep -Seconds 1
           $portRetries++
       }
   }
   ```
   Ensures MariaDB port is accessible before starting backend

### File Modified: `stop-local.ps1`

Enhanced to properly stop all Java and Node processes.

---

## 6. Database Initialization Updates

### File Modified: `environments/dev/grabbill-infra/config/mariadb/docker-entrypoint-initdb.d/create-database.sql`

**Changes:**

1. **Account Payment Exemption:**
   ```sql
   INSERT INTO account (..., payment_exempted, ...) 
   VALUES (..., true, ...);
   ```

2. **Usage Stats Schema Update:**
   Added SMS-related columns:
   ```sql
   total_sms_sent BIGINT DEFAULT 0,
   sms_credit INT DEFAULT 0,
   sms_credit_used INT DEFAULT 0
   ```

3. **Professional Plan Configuration:**
   Updated default subscription to use "Professional" plan with appropriate limits

4. **Password Storage:**
   Currently using plain text `test12345` (needs BCrypt hashes for production)

---

## Login Credentials

### Client Dashboard (http://localhost:4801)
- **Email:** owner@demo.com
- **Password:** test12345

### Admin Dashboard (http://localhost:4802)
- **Email:** admin@grabbill.com
- **Password:** test12345

---

## Running the Application

1. **Start all services:**
   ```powershell
   .\start-local.ps1
   ```

2. **Stop all services:**
   ```powershell
   .\stop-local.ps1
   ```

3. **Access URLs:**
   - Client App: http://localhost:4801
   - Admin App: http://localhost:4802
   - Backend API: http://localhost:8080

---

## Infrastructure Services

The following Docker services are started automatically:

- **MariaDB:** localhost:3307 (user: grabbill.root / pass: test12345)
- **RabbitMQ:** localhost:5672 (user: grabbill.root / pass: test12345)
- **MinIO:** http://localhost:9000 (user: grabbill.root / pass: test12345)
- **MailHog:** http://localhost:8025
- **Traefik:** *.grabbill.localhost (requires hosts file entries)

---

## Known Issues and Limitations

1. **Security Warning:** Plain text passwords are used. This is NOT production-ready.

2. **Firebase:** Push notifications will not work as Firebase is disabled.

3. **Stripe:** Payment processing is disabled for exempted accounts.

4. **Database Persistence:** The MariaDB data is stored in `$HOME/.grabbill-infra/data/mariadb/var-lib-mysql`. Delete this directory to reset the database.

5. **Port Conflicts:** Ensure ports 3307, 5672, 8080, 9000, 4801, 4802 are not in use by other applications.

---

## Production Deployment Checklist

Before deploying to production, you MUST:

- [ ] Revert `SecurityConfiguration.java` to use `BCryptPasswordEncoder`
- [ ] Generate proper BCrypt password hashes for all users
- [ ] Update database initialization script with BCrypt hashes
- [ ] Configure valid Stripe API keys
- [ ] Configure valid Firebase service account credentials
- [ ] Remove or properly secure the `payment_exempted` flag
- [ ] Review and update all security configurations
- [ ] Enable HTTPS/TLS for all endpoints
- [ ] Configure proper CORS origins
- [ ] Review and update database connection pooling settings

---

## Troubleshooting

### Backend won't start
- Check if port 8080 is already in use: `netstat -an | findstr ":8080"`
- Check backend logs in the PowerShell window
- Ensure MariaDB is running: `docker ps | findstr mariadb`

### Frontend won't compile
- Delete `node_modules` and run `npm install` in `grabbill-ui` directory
- Check Node.js version (should be v18 or compatible)

### Database connection errors
- Verify MariaDB container is running: `docker ps`
- Check MariaDB logs: `docker logs grabbill-infra-mariadb-1`
- Verify port 3307 is accessible: `netstat -an | findstr ":3307"`

### Authentication fails
- Verify database has plain text passwords: 
  ```sql
  SELECT id, email, password FROM user WHERE id = 1;
  ```
- Ensure `SecurityConfiguration.java` is using `NoOpPasswordEncoder`
- Restart the backend after any configuration changes

---

## File Summary

### Modified Files
1. `grabbill-backend/grabbill-server/src/main/java/com/grabbill/server/security/SecurityConfiguration.java`
2. `grabbill-backend/grabbill-server/src/main/java/com/grabbill/server/service/AuthServiceImpl.java`
3. `grabbill-backend/grabbill-server/src/main/java/com/grabbill/server/GrabbillServerApplication.java`
4. `grabbill-backend/grabbill-server/src/main/resources/application.properties`
5. `environments/dev/grabbill-infra/config/mariadb/docker-entrypoint-initdb.d/create-database.sql`
6. `start-local.ps1`
7. `stop-local.ps1`
8. `.gitignore`

### Created Files
1. `grabbill-backend/grabbill-server/src/main/resources/firebase-mock.json`

---

**Document Version:** 1.0  
**Last Updated:** December 22, 2025  
**Author:** Development Team
