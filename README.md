# ExamSeat AI
### AI-Powered Automatic Examination Seating Arrangement & Hall Ticket Generator

![Java 17](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)
![Database](https://img.shields.io/badge/Database-Supabase%20PostgreSQL-3ecf8e)
![Security](https://img.shields.io/badge/Auth-Spring%20Security%20%2B%20JWT-red)
![PDF Engine](https://img.shields.io/badge/PDF-OpenPDF-orange)
![Tests](https://img.shields.io/badge/JUnit%205-21%20Passed-success)

**ExamSeat AI** is a production-grade academic examination management application engineered for college and university examination branches. It automates hall venue modeling, student cohort registration, examination scheduling, interleaved multi-department seating arrangement generation, student seat lookup, OpenPDF hall ticket generation, and invigilator attendance reports.

Persistent persistence is strictly backed by **Supabase PostgreSQL** via direct JDBC connectivity with HikariCP connection pooling and Spring Data JPA.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Features and Modules](#2-features-and-modules)
3. [Architecture](#3-architecture)
4. [Technology Stack](#4-technology-stack)
5. [Prerequisites](#5-prerequisites)
6. [Java 17 Installation Verification](#6-java-17-installation-verification)
7. [Maven Setup](#7-maven-setup)
8. [Supabase Project Creation](#8-supabase-project-creation)
9. [PostgreSQL Connection Details](#9-postgresql-connection-details)
10. [How to Run schema.sql](#10-how-to-run-schemasql)
11. [How to Run seed.sql](#11-how-to-run-seedsql)
12. [How to Configure Supabase Auth](#12-how-to-configure-supabase-auth)
13. [How to Create the Initial Administrator](#13-how-to-create-the-initial-administrator)
14. [How to Configure .env](#14-how-to-configure-env)
15. [How to Run the Spring Boot Application](#15-how-to-run-the-spring-boot-application)
16. [How to Open the Frontend](#16-how-to-open-the-frontend)
17. [How to Import students.csv](#17-how-to-import-studentscsv)
18. [How to Generate a Seating Plan](#18-how-to-generate-a-seating-plan)
19. [How to Download Hall Tickets](#19-how-to-download-hall-tickets)
20. [How to Print Invigilator Reports](#20-how-to-print-invigilator-reports)
21. [How to Run Tests](#21-how-to-run-tests)
22. [Common Errors and Troubleshooting](#22-common-errors-and-troubleshooting)
23. [Security Considerations](#23-security-considerations)
24. [Deployment Instructions](#24-deployment-instructions)

---

## 1. Project Overview

Colleges conduct centralized examinations across multiple engineering and science departments simultaneously. Manually arranging students so that adjacent seats are occupied by candidates from different departments is error-prone, labor-intensive, and susceptible to academic malpractice.

**ExamSeat AI** solves this through a **Deterministic Interleaved Round-Robin Seating Algorithm** that maximizes department separation on two-seat and multi-seat benches, eliminates seating conflicts, guarantees unique occupancy, and generates verified PDF hall tickets and room-wise invigilation charts in seconds.

---

## 2. Features and Modules

- **Module 1 — Live Dashboard**: Live metrics from Supabase, total halls, available seats, scheduled exams, unassigned draft counters, upcoming exams timetable, Chart.js department breakdowns and hall capacities.
- **Module 2 — Rooms & Benches**: Hall CRUD, dimension modeling (`rows × benches_per_row × seats_per_bench`), automatic physical `Seat` generation, interactive visual layout preview, protection against mutating layouts mapped to finalized exams.
- **Module 3 — Student Management & CSV Import**: Student CRUD, multi-criteria search and filter, pagination, robust CSV importer with validation preview, duplicate detection in file and database, summary of inserted/skipped/rejected.
- **Module 4 — Examination Management**: Exam scheduling (FN/AN/EV), department and semester cohort multi-selection, hall assignment, real-time student count vs hall capacity estimation, overlap prevention across same hall/date/session, full lifecycle status transitions (`DRAFT` &rarr; `SCHEDULED` &rarr; `SEATING_GENERATED` &rarr; `FINALIZED` &rarr; `COMPLETED` / `CANCELLED`).
- **Module 5 — Interleaved Seating Plan**: Deterministic round-robin allocation across department queues, 2-seat bench alternation preference, capacity checking with `InsufficientCapacityException`, transactional rollback, comprehensive 7-point integrity verification checklist, finalization locking, administrator reopen workflow, color-coded visual seating grid, clickable seat modal.
- **Module 6 — Student Seat Lookup**: Roll number lookup (authenticated or public), venue coordinates, hall name, row, bench, seat position, reporting time, direct hall ticket PDF download link.
- **Module 7 — Hall Ticket Generation**: Vector OpenPDF admit cards formatted for A4 printing, college branding, student credentials, exam timetable, seat coordinates, candidate rules, invigilator/controller signatures, single and bulk ZIP download.
- **Module 8 — Invigilator Reports**: Room-wise seating attendance chart with signature lines, department breakdown summary, vacant seats list, exportable as OpenPDF, Apache Commons CSV, or print layout.
- **Module 9 — User Management & Roles**: Role-based access control (`ADMINISTRATOR`, `EXAM_CELL_STAFF`, `INVIGILATOR`, `STUDENT`), automatic bootstrap administrator on startup.

---

## 3. Architecture

ExamSeat AI follows a modular, layered clean architecture:

```
ExamSeatAI/
├── database/
│   ├── schema.sql              # Supabase PostgreSQL DDL
│   ├── seed.sql                # Realistic college seed dataset
│   └── README.md               # Database execution guide
├── sample-data/
│   └── students.csv            # Sample CSV for bulk student import
├── src/
│   ├── main/
│   │   ├── java/com/examseat/
│   │   │   ├── ExamSeatApplication.java
│   │   │   ├── algorithm/      # Interleaved Round-Robin Seating Engine
│   │   │   ├── config/         # System configurations
│   │   │   ├── controller/     # REST Endpoints
│   │   │   ├── dto/            # Data Transfer Objects & Requests
│   │   │   ├── entity/         # JPA Entities
│   │   │   ├── exception/      # Custom Exceptions & Global Handler
│   │   │   ├── repository/     # Spring Data JPA Repositories
│   │   │   ├── security/       # JWT Provider, Filters, SecurityConfig
│   │   │   └── service/        # Business Logic, OpenPDF, CSV Engines
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/         # Frontend Web Dashboard
│   │           ├── index.html  # Single-Page Management Dashboard
│   │           ├── login.html  # Academic Login Portal
│   │           ├── lookup.html # Public Student Lookup Portal
│   │           ├── css/styles.css
│   │           └── js/         # Vanilla ES6 Modular Scripts
└── pom.xml
```

---

## 4. Technology Stack

- **Backend Platform**: Java 17 (LTS), Spring Boot 3.3.5
- **Web & APIs**: Spring Web MVC, Jakarta Bean Validation
- **Database & Persistence**: Supabase PostgreSQL 15+, Hibernate 6, Spring Data JPA, HikariCP
- **Authentication & Security**: Spring Security 6, JJWT 0.12.6 (HMAC-SHA256), BCrypt Password Hashing
- **Document & Data Processing**: OpenPDF 2.0.3, Apache Commons CSV 1.11.0
- **Frontend**: HTML5, Modern CSS3 (Navy & Teal academic theme), Vanilla JavaScript ES6+, Chart.js
- **Build System**: Apache Maven 3.9+
- **Testing**: JUnit 5, Mockito

---

## 5. Prerequisites

Before running ExamSeat AI, ensure you have:
1. **JDK 17 or higher** installed.
2. **Apache Maven 3.8+** installed (or an IDE like VS Code / IntelliJ IDEA with Maven support).
3. A **Supabase project** (Free or Pro tier) with PostgreSQL database access.

---

## 6. Java 17 Installation Verification

Open PowerShell or Terminal and run:

```bash
java -version
```

Expected output:
```text
java version "17.0.x" (or higher)
Java(TM) SE Runtime Environment
Java HotSpot(TM) 64-Bit Server VM
```

---

## 7. Maven Setup

Verify your Maven installation:

```bash
mvn -version
```

If Maven is installed via VS Code extensions or another directory on Windows, you can call it directly:
```powershell
& "C:\Users\<YourUser>\.vscode\extensions\oracle.oracle-java-24.1.2\nbcode\java\maven\bin\mvn.cmd" -version
```

---

## 8. Supabase Project Creation

1. Navigate to [Supabase Console](https://supabase.com/dashboard).
2. Click **New project**, name it `ExamSeat-AI`, and set a strong database password.
3. Select your preferred region and click **Create new project**.

---

## 9. PostgreSQL Connection Details

In the Supabase Dashboard:
1. Go to **Project Settings** &rarr; **Database**.
2. Find the **Connection string** tab:
   - **Host**: `db.<your-project-ref>.supabase.co` (Direct) or `aws-0-<region>.pooler.supabase.com` (Pooler)
   - **Port**: `5432` (or `6543` for connection pooler)
   - **Database**: `postgres`
   - **User**: `postgres`
   - **Password**: `<your-database-password>`

---

## 10. How to Run schema.sql

1. In Supabase Dashboard, click the **SQL Editor** icon (`>_`).
2. Click **New query**.
3. Open `database/schema.sql`, copy all SQL statements, paste them into the editor, and click **Run**.
4. All 9 tables and their indexes will be created.

---

## 11. How to Run seed.sql

1. In Supabase **SQL Editor**, open another **New query**.
2. Copy the contents of `database/seed.sql` and click **Run**.
3. This pre-seeds:
   - 4 examination halls (172 seats)
   - 32 students across 5 engineering departments
   - 2 examinations with department cohorts
   - 4 user accounts with hashed credentials

---

## 12. How to Configure Supabase Auth

ExamSeat AI includes dual authentication support:
- Built-in secure BCrypt token authorization with local user profiles table.
- Supabase Auth compatibility: If you configure `SUPABASE_URL` and `SUPABASE_ANON_KEY`, Supabase user tokens are mapped to roles in the `profiles` table.

---

## 13. How to Create the Initial Administrator

If the `profiles` table is empty when the Spring Boot application boots, the `AuthService.bootstrapDefaultAdmin()` method automatically initializes the primary administrator:
- **Username**: `admin`
- **Password**: `Admin@123`
- **Role**: `ADMINISTRATOR`

You can also create more accounts in the **User Management** tab once logged in.

---

## 14. How to Configure .env

Create a `.env` file in the project root:

```bash
# Supabase PostgreSQL Database Credentials
SUPABASE_DB_HOST=db.vlcurbuflipriltgkyhe.supabase.co
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=your_supabase_password

# Security
APP_JWT_SECRET=super_secret_jwt_signing_key_for_examseat_ai_must_be_long_and_secure_32chars
PORT=8080

# Institution Branding
COLLEGE_NAME=ExamSeat Institute of Technology & Management
COLLEGE_ADDRESS=University Campus, Innovation Road, Metro City - 600001
COLLEGE_CODE=ESITM-2026
```

---

## 15. How to Run the Spring Boot Application

From the project root directory, run:

```bash
# Using Maven Spring Boot plugin:
mvn spring-boot:run

# Or running the packaged JAR:
mvn package -DskipTests
java -jar target/examseat-ai-1.0.0.jar
```

---

## 16. How to Open the Frontend

Once the application starts:
- **Login Portal**: Open [http://localhost:8080/login.html](http://localhost:8080/login.html)
  - Quick-fill buttons are available for Admin, Staff, Invigilator, and Student.
- **Main Administration Dashboard**: [http://localhost:8080/index.html](http://localhost:8080/index.html)
- **Public Student Lookup**: [http://localhost:8080/lookup.html](http://localhost:8080/lookup.html)

---

## 17. How to Import students.csv

1. In the sidebar, navigate to **Students**.
2. Click the **📥 Import CSV** button.
3. Select `sample-data/students.csv` (or upload your own CSV).
4. Review the instant **Validation Preview**:
   - Total rows
   - Valid records ready for import
   - Duplicate roll numbers flagged in red
5. Click **Confirm & Insert Valid Students**.

---

## 18. How to Generate a Seating Plan

1. Navigate to **Seating Plan** in the sidebar.
2. Select an examination from the dropdown (e.g. *Mid-Semester Examination*).
3. Click **⚡ Generate Plan**.
4. The Interleaved Round-Robin algorithm will:
   - Group candidates by department.
   - Alternate departments on two-seat benches.
   - Assign physical seats deterministically.
5. Click **🔍 Verify Checklist** to audit the 7 academic constraints (zero unassigned, zero duplicates, capacity match).
6. Click **🔒 Finalize Plan** to lock the arrangement.

---

## 19. How to Download Hall Tickets

1. **Staff / Admin**: Navigate to **Hall Tickets**, select the exam, and click **Download PDF** for any student, or **📦 Download All Tickets (ZIP)** to download all tickets at once.
2. **Students**: Navigate to **Student Lookup** (or `/lookup.html`), enter your roll number (e.g., `CS001`), and click **📄 Download Official Hall Ticket (PDF)**.

---

## 20. How to Print Invigilator Reports

1. Navigate to **Invigilator Reports**.
2. Select the Examination and the Hall/Room.
3. You can:
   - Click **📄 Export PDF** for an official A4 room report.
   - Click **📊 Export CSV** for spreadsheets.
   - Click **🖨️ Print Sheet** for browser print.

---

## 21. How to Run Tests

Run the complete automated JUnit 5 test suite:

```bash
mvn test
```

### Test Coverage Summary:
- `InterleavedSeatingAlgorithmTest`: Round-robin distribution, bench alternation, uniqueness, capacity checks, deterministic ordering.
- `HallServiceTest`: Capacity calculations, automatic seat creation, finalized modification blocking.
- `StudentServiceTest`: CSV parsing, header verification, duplicate rejection.
- `ExamAndSeatingServiceTest`: Timing validations, hall overlap prevention, rollback on failure, admin reopen.
- `HallTicketAndReportServiceTest`: OpenPDF byte header validation, CSV generation, department breakdown.

**Result**: 21 / 21 Tests Passing (0 Failures, 0 Errors).

---

## 22. Common Errors and Troubleshooting

1. **`InsufficientCapacityException`**:
   - Cause: Eligible candidates exceed total selected hall capacity.
   - Fix: Assign an additional examination hall in the Exam configuration.
2. **`ConflictException: Hall is already scheduled`**:
   - Cause: Another active examination uses the same hall on the same date and session.
   - Fix: Choose a different hall or reschedule session.
3. **`Ambiguous reference to List`**:
   - Fix: OpenPDF `com.lowagie.text.List` and `java.util.List` conflict if wildcard imports are used; both are explicitly imported in ExamSeat AI.
4. **`Connection refused to PostgreSQL`**:
   - Fix: Verify `SUPABASE_DB_HOST` and `SUPABASE_DB_PASSWORD` in `.env` and verify port `5432` or pooler port `6543`.

---

## 23. Security Considerations

- **No Hardcoded Credentials**: Database passwords, JWT signing secrets, and Supabase keys are injected strictly via environment variables.
- **Backend Authorization**: Role authorization (`ADMINISTRATOR`, `EXAM_CELL_STAFF`, `INVIGILATOR`, `STUDENT`) is enforced by Spring Security filters at the controller level.
- **Audited Reopen**: Once finalized, exam seating cannot be silently changed; only administrators can reopen the plan with audit logging.
- **Student Privacy**: Student lookup endpoints do not expose credentials, staff actions, or other students' private data.

---

## 24. Deployment Instructions

### Docker Deployment
Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/examseat-ai-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t examseat-ai:latest .
docker run -p 8080:8080 --env-file .env examseat-ai:latest
```

### Cloud Deployment (Render / Railway / AWS / GCP)
Set the environment variables `SUPABASE_DB_HOST`, `SUPABASE_DB_PASSWORD`, `APP_JWT_SECRET`, and `PORT`, and deploy the JAR directly.
