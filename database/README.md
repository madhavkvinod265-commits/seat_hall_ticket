# ExamSeat AI - Supabase PostgreSQL Database Setup Guide

This guide explains how to set up the **Supabase PostgreSQL** database for ExamSeat AI.

---

## 1. Create a Supabase Project

1. Log in to [Supabase Console](https://supabase.com/dashboard).
2. Click **"New project"**.
3. Choose your organization, set the project name to `ExamSeat-AI`, and create a strong database password (keep this safe).
4. Select your preferred region and click **"Create new project"**.

---

## 2. Execute Database Schema in Supabase

1. Open your Supabase project dashboard.
2. Navigate to **SQL Editor** in the left sidebar (icon with `>_`).
3. Click **"New query"**.
4. Open `database/schema.sql` from this repository, copy the full contents, paste it into the SQL editor, and click **"Run"** (or press `Ctrl+Enter`).
5. Confirm that all tables (`profiles`, `students`, `halls`, `seats`, `exams`, `exam_departments`, `exam_halls`, `allocations`, `hall_tickets`) and indexes are created successfully.

---

## 3. Load Sample Seed Data (Optional but Recommended)

1. In the Supabase **SQL Editor**, open another **"New query"**.
2. Copy and paste the contents of `database/seed.sql` into the editor.
3. Click **"Run"**.
4. This seeds:
   - 4 examination halls with all physical seats generated
   - 32 realistic students across Computer Science, Electronics, Mechanical, Civil, and IT
   - 2 sample exams (Scheduled & Draft) with department cohorts attached
   - Pre-configured staff and administrator profiles

---

## 4. Obtain Supabase PostgreSQL Connection Credentials

In the Supabase Dashboard:
1. Go to **Project Settings** (gear icon) -> **Database**.
2. Under **Connection string**, select **JDBC** or **URI**:
   - **Host**: e.g., `db.yourprojectref.supabase.co` (Direct) or `aws-0-region.pooler.supabase.com` (Session/Transaction pooler, port `5432` or `6543`)
   - **Database**: `postgres`
   - **Port**: `5432` (or `6543` for connection pooler)
   - **User**: `postgres` (or `postgres.yourprojectref` if using pooler)
   - **Password**: The password you set when creating the project.

---

## 5. Configure Spring Boot `.env` or Environment Variables

Copy `.env.example` to `.env` in the project root:

```bash
SUPABASE_DB_HOST=db.yourprojectref.supabase.co
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=your_db_password
APP_JWT_SECRET=super_secret_jwt_key_at_least_32_chars_long_examseat_ai
```

Spring Boot will automatically pick up these environment variables to connect via standard PostgreSQL JDBC.

---

## 6. Initial Administrator Bootstrap

If you boot ExamSeat AI with an empty `profiles` table, the backend automatically detects this and creates the bootstrap administrator:
- **Username**: `admin`
- **Password**: `Admin@123`
- **Role**: `ADMINISTRATOR`

You can change this password or add other exam cell staff immediately after logging in.
