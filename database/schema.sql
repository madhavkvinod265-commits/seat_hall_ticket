-- ==============================================================================
-- ExamSeat AI - Supabase PostgreSQL Database Schema
-- Production Schema for Examination Seating Arrangement & Hall Ticket Generator
-- Compatible with Supabase PostgreSQL (PostgreSQL 14+)
-- ==============================================================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Clean existing tables if resetting (dependency order)
-- DROP TABLE IF EXISTS hall_tickets CASCADE;
-- DROP TABLE IF EXISTS allocations CASCADE;
-- DROP TABLE IF EXISTS exam_halls CASCADE;
-- DROP TABLE IF EXISTS exam_departments CASCADE;
-- DROP TABLE IF EXISTS exams CASCADE;
-- DROP TABLE IF EXISTS seats CASCADE;
-- DROP TABLE IF EXISTS halls CASCADE;
-- DROP TABLE IF EXISTS students CASCADE;
-- DROP TABLE IF EXISTS profiles CASCADE;

-- ------------------------------------------------------------------------------
-- 1. PROFILES (User accounts & roles)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    display_name VARCHAR(150) NOT NULL,
    email VARCHAR(255),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMINISTRATOR', 'EXAM_CELL_STAFF', 'INVIGILATOR', 'STUDENT')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_profiles_username ON profiles(username);
CREATE INDEX IF NOT EXISTS idx_profiles_role ON profiles(role);

-- ------------------------------------------------------------------------------
-- 2. STUDENTS
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roll_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    department VARCHAR(100) NOT NULL,
    semester INTEGER NOT NULL CHECK (semester >= 1 AND semester <= 12),
    email VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_students_roll_no ON students(roll_no);
CREATE INDEX IF NOT EXISTS idx_students_dept_sem ON students(department, semester);
CREATE INDEX IF NOT EXISTS idx_students_active ON students(active);

-- ------------------------------------------------------------------------------
-- 3. HALLS (Examination halls/rooms)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS halls (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    building VARCHAR(100) NOT NULL,
    floor VARCHAR(50) NOT NULL,
    rows INTEGER NOT NULL CHECK (rows > 0),
    benches_per_row INTEGER NOT NULL CHECK (benches_per_row > 0),
    seats_per_bench INTEGER NOT NULL CHECK (seats_per_bench > 0),
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_halls_name ON halls(name);
CREATE INDEX IF NOT EXISTS idx_halls_active ON halls(active);

-- ------------------------------------------------------------------------------
-- 4. SEATS (Individual seating coordinates generated per hall)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS seats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hall_id UUID NOT NULL REFERENCES halls(id) ON DELETE CASCADE,
    row_number INTEGER NOT NULL CHECK (row_number > 0),
    bench_number INTEGER NOT NULL CHECK (bench_number > 0),
    position INTEGER NOT NULL CHECK (position > 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_hall_row_bench_position UNIQUE (hall_id, row_number, bench_number, position)
);

CREATE INDEX IF NOT EXISTS idx_seats_hall_id ON seats(hall_id);
CREATE INDEX IF NOT EXISTS idx_seats_coords ON seats(hall_id, row_number, bench_number, position);

-- ------------------------------------------------------------------------------
-- 5. EXAMS (Examinations master table)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS exams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    exam_date DATE NOT NULL,
    session VARCHAR(10) NOT NULL CHECK (session IN ('FN', 'AN', 'EV')),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    reporting_time TIME NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'SCHEDULED', 'SEATING_GENERATED', 'FINALIZED', 'COMPLETED', 'CANCELLED')),
    instructions TEXT,
    created_by VARCHAR(150),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_exam_times CHECK (start_time < end_time AND reporting_time <= start_time)
);

CREATE INDEX IF NOT EXISTS idx_exams_date ON exams(exam_date);
CREATE INDEX IF NOT EXISTS idx_exams_status ON exams(status);
CREATE INDEX IF NOT EXISTS idx_exams_date_session ON exams(exam_date, session);

-- ------------------------------------------------------------------------------
-- 6. EXAM DEPARTMENTS (Selected departments and semesters for the exam)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS exam_departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    department VARCHAR(100) NOT NULL,
    semester INTEGER NOT NULL CHECK (semester >= 1 AND semester <= 12),
    CONSTRAINT uq_exam_dept_sem UNIQUE (exam_id, department, semester)
);

CREATE INDEX IF NOT EXISTS idx_exam_departments_exam_id ON exam_departments(exam_id);

-- ------------------------------------------------------------------------------
-- 7. EXAM HALLS (Selected examination halls for the exam)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS exam_halls (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    hall_id UUID NOT NULL REFERENCES halls(id) ON DELETE RESTRICT,
    CONSTRAINT uq_exam_hall UNIQUE (exam_id, hall_id)
);

CREATE INDEX IF NOT EXISTS idx_exam_halls_exam_id ON exam_halls(exam_id);
CREATE INDEX IF NOT EXISTS idx_exam_halls_hall_id ON exam_halls(hall_id);

-- ------------------------------------------------------------------------------
-- 8. ALLOCATIONS (Seating allocations connecting exam, student, and seat)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE RESTRICT,
    seat_id UUID NOT NULL REFERENCES seats(id) ON DELETE RESTRICT,
    allocated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    allocated_by VARCHAR(150),
    CONSTRAINT uq_exam_student UNIQUE (exam_id, student_id),
    CONSTRAINT uq_exam_seat UNIQUE (exam_id, seat_id)
);

CREATE INDEX IF NOT EXISTS idx_allocations_exam_id ON allocations(exam_id);
CREATE INDEX IF NOT EXISTS idx_allocations_student_id ON allocations(student_id);
CREATE INDEX IF NOT EXISTS idx_allocations_seat_id ON allocations(seat_id);

-- ------------------------------------------------------------------------------
-- 9. HALL TICKETS (Generated hall tickets)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hall_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_number VARCHAR(100) NOT NULL UNIQUE,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    ticket_data JSONB,
    generated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_ticket_student_exam UNIQUE (student_id, exam_id)
);

CREATE INDEX IF NOT EXISTS idx_hall_tickets_student ON hall_tickets(student_id);
CREATE INDEX IF NOT EXISTS idx_hall_tickets_exam ON hall_tickets(exam_id);
CREATE INDEX IF NOT EXISTS idx_hall_tickets_num ON hall_tickets(ticket_number);

-- ------------------------------------------------------------------------------
-- Helpful Comments & Audit Triggers
-- ------------------------------------------------------------------------------
COMMENT ON TABLE profiles IS 'User credentials, roles, and profiles for administrative and staff access';
COMMENT ON TABLE students IS 'Student master registry with roll number, department, semester';
COMMENT ON TABLE halls IS 'Examination halls with dimension configuration';
COMMENT ON TABLE seats IS 'Concrete seats generated according to hall dimensions';
COMMENT ON TABLE exams IS 'Examination schedules, sessions, and status';
COMMENT ON TABLE exam_departments IS 'Eligible department-semester cohorts for an exam';
COMMENT ON TABLE exam_halls IS 'Selected halls allocated to an exam';
COMMENT ON TABLE allocations IS 'Deterministic seating assignments per student and seat';
COMMENT ON TABLE hall_tickets IS 'Archived hall tickets with verification tokens';
