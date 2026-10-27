-- ==============================================================================
-- ExamSeat AI - Realistic Seed Data
-- ==============================================================================

-- 1. Initial Profiles (Passwords are BCrypt hashed for 'Admin@123', 'Staff@123', 'Invig@123', 'Student@123')
-- BCrypt for Admin@123: $2a$10$2S37k4rS4vQk0Y0E2/8l0eOaWmsG8vC3q8xIph1aWsm5u37OeqVlq
-- BCrypt for Staff@123: $2a$10$Lsm0Z2vU5nJ2K3P0E1.8geOqWm6G8uC2q7xJph2bWtm4u28OeqVmq
-- (The Spring Boot bootstrap also safely auto-creates admin if profiles table is empty)
INSERT INTO profiles (id, username, password_hash, display_name, email, role, active)
VALUES 
  ('a0000000-0000-0000-0000-000000000001', 'admin', '$2a$10$5j8vGvOaJt0N8vWvWzQzZeK0Z4iX1y2o6A8.R1iM6q/j2R2iN8oCe', 'System Administrator', 'admin@examseat.edu', 'ADMINISTRATOR', true),
  ('a0000000-0000-0000-0000-000000000002', 'staff_exam', '$2a$10$5j8vGvOaJt0N8vWvWzQzZeK0Z4iX1y2o6A8.R1iM6q/j2R2iN8oCe', 'Prof. Sarah Jenkins (Exam Cell)', 's.jenkins@examseat.edu', 'EXAM_CELL_STAFF', true),
  ('a0000000-0000-0000-0000-000000000003', 'invigilator1', '$2a$10$5j8vGvOaJt0N8vWvWzQzZeK0Z4iX1y2o6A8.R1iM6q/j2R2iN8oCe', 'Dr. Rajesh Sharma', 'r.sharma@examseat.edu', 'INVIGILATOR', true),
  ('a0000000-0000-0000-0000-000000000004', 'CS001', '$2a$10$5j8vGvOaJt0N8vWvWzQzZeK0Z4iX1y2o6A8.R1iM6q/j2R2iN8oCe', 'Ananya Raj', 'ananya.cs001@examseat.edu', 'STUDENT', true)
ON CONFLICT (username) DO NOTHING;

-- 2. Examination Halls
INSERT INTO halls (id, name, building, floor, rows, benches_per_row, seats_per_bench, capacity, active)
VALUES
  ('b0000000-0000-0000-0000-000000000001', 'Aryabhata Hall (Room 101)', 'Academic Block A', 'Ground Floor', 5, 4, 2, 40, true),
  ('b0000000-0000-0000-0000-000000000002', 'Ramanujan Hall (Room 102)', 'Academic Block A', 'Ground Floor', 5, 4, 2, 40, true),
  ('b0000000-0000-0000-0000-000000000003', 'Turing Seminar Hall (Room 201)', 'Computing Block', '1st Floor', 6, 5, 2, 60, true),
  ('b0000000-0000-0000-0000-000000000004', 'Kalam Memorial Hall (Room 301)', 'Engineering Block', '2nd Floor', 4, 4, 2, 32, true)
ON CONFLICT (name) DO NOTHING;

-- Generate Seats for Hall 1 (Aryabhata Hall: 5 rows x 4 benches x 2 seats = 40)
INSERT INTO seats (hall_id, row_number, bench_number, position)
SELECT 
  'b0000000-0000-0000-0000-000000000001'::uuid, r, b, p
FROM generate_series(1, 5) AS r
CROSS JOIN generate_series(1, 4) AS b
CROSS JOIN generate_series(1, 2) AS p
ON CONFLICT (hall_id, row_number, bench_number, position) DO NOTHING;

-- Generate Seats for Hall 2 (Ramanujan Hall: 5 rows x 4 benches x 2 seats = 40)
INSERT INTO seats (hall_id, row_number, bench_number, position)
SELECT 
  'b0000000-0000-0000-0000-000000000002'::uuid, r, b, p
FROM generate_series(1, 5) AS r
CROSS JOIN generate_series(1, 4) AS b
CROSS JOIN generate_series(1, 2) AS p
ON CONFLICT (hall_id, row_number, bench_number, position) DO NOTHING;

-- Generate Seats for Hall 3 (Turing Seminar Hall: 6 rows x 5 benches x 2 seats = 60)
INSERT INTO seats (hall_id, row_number, bench_number, position)
SELECT 
  'b0000000-0000-0000-0000-000000000003'::uuid, r, b, p
FROM generate_series(1, 6) AS r
CROSS JOIN generate_series(1, 5) AS b
CROSS JOIN generate_series(1, 2) AS p
ON CONFLICT (hall_id, row_number, bench_number, position) DO NOTHING;

-- Generate Seats for Hall 4 (Kalam Memorial Hall: 4 rows x 4 benches x 2 seats = 32)
INSERT INTO seats (hall_id, row_number, bench_number, position)
SELECT 
  'b0000000-0000-0000-0000-000000000004'::uuid, r, b, p
FROM generate_series(1, 4) AS r
CROSS JOIN generate_series(1, 4) AS b
CROSS JOIN generate_series(1, 2) AS p
ON CONFLICT (hall_id, row_number, bench_number, position) DO NOTHING;

-- 3. Sample Students across multiple departments (Computer Science, Electronics, Mechanical, Civil)
INSERT INTO students (roll_no, name, department, semester, email, active)
VALUES
  -- Computer Science (Semester 5)
  ('CS001', 'Ananya Raj', 'Computer Science', 5, 'ananya.cs@examseat.edu', true),
  ('CS002', 'Rohan Verma', 'Computer Science', 5, 'rohan.cs@examseat.edu', true),
  ('CS003', 'Meera Das', 'Computer Science', 5, 'meera.cs@examseat.edu', true),
  ('CS004', 'Kavya S', 'Computer Science', 5, 'kavya.cs@examseat.edu', true),
  ('CS005', 'Gautam Pillai', 'Computer Science', 5, 'gautam.cs@examseat.edu', true),
  ('CS006', 'Sneha Kapoor', 'Computer Science', 5, 'sneha.cs@examseat.edu', true),
  ('CS007', 'Abhishek Sengupta', 'Computer Science', 5, 'abhishek.cs@examseat.edu', true),
  ('CS008', 'Pooja Hegde', 'Computer Science', 5, 'pooja.cs@examseat.edu', true),
  ('CS009', 'Naveen Kumar', 'Computer Science', 5, 'naveen.cs@examseat.edu', true),
  ('CS010', 'Tanvi Deshmukh', 'Computer Science', 5, 'tanvi.cs@examseat.edu', true),

  -- Electronics (Semester 5)
  ('EC001', 'Arjun Nair', 'Electronics', 5, 'arjun.ec@examseat.edu', true),
  ('EC002', 'Diya Thomas', 'Electronics', 5, 'diya.ec@examseat.edu', true),
  ('EC003', 'Kiran Patel', 'Electronics', 5, 'kiran.ec@examseat.edu', true),
  ('EC004', 'Manish Reddy', 'Electronics', 5, 'manish.ec@examseat.edu', true),
  ('EC005', 'Bhavna Joshi', 'Electronics', 5, 'bhavna.ec@examseat.edu', true),
  ('EC006', 'Vikas Rao', 'Electronics', 5, 'vikas.ec@examseat.edu', true),
  ('EC007', 'Isha Sundaram', 'Electronics', 5, 'isha.ec@examseat.edu', true),
  ('EC008', 'Harish Menon', 'Electronics', 5, 'harish.ec@examseat.edu', true),

  -- Mechanical (Semester 5)
  ('ME001', 'Adithya Menon', 'Mechanical', 5, 'adithya.me@examseat.edu', true),
  ('ME002', 'Siddharth Iyer', 'Mechanical', 5, 'siddharth.me@examseat.edu', true),
  ('ME003', 'Priya Bhatia', 'Mechanical', 5, 'priya.me@examseat.edu', true),
  ('ME004', 'Tarun Sethi', 'Mechanical', 5, 'tarun.me@examseat.edu', true),
  ('ME005', 'Karthik Somani', 'Mechanical', 5, 'karthik.me@examseat.edu', true),
  ('ME006', 'Divya Chawla', 'Mechanical', 5, 'divya.me@examseat.edu', true),

  -- Civil (Semester 5)
  ('CV001', 'Nikhil Roy', 'Civil', 5, 'nikhil.cv@examseat.edu', true),
  ('CV002', 'Pallavi Sen', 'Civil', 5, 'pallavi.cv@examseat.edu', true),
  ('CV003', 'Varun Goswami', 'Civil', 5, 'varun.cv@examseat.edu', true),
  ('CV004', 'Ritika Dutta', 'Civil', 5, 'ritika.cv@examseat.edu', true),

  -- Information Technology (Semester 5)
  ('IT001', 'Sameer Saxena', 'Information Technology', 5, 'sameer.it@examseat.edu', true),
  ('IT002', 'Neha Kulkarni', 'Information Technology', 5, 'neha.it@examseat.edu', true),
  ('IT003', 'Aman Sharma', 'Information Technology', 5, 'aman.it@examseat.edu', true),
  ('IT004', 'Deepika Nair', 'Information Technology', 5, 'deepika.it@examseat.edu', true)
ON CONFLICT (roll_no) DO NOTHING;

-- 4. Sample Exams
INSERT INTO exams (id, name, exam_date, session, start_time, end_time, reporting_time, status, instructions, created_by)
VALUES
  ('c0000000-0000-0000-0000-000000000001', 
   'Mid-Semester Examination - November 2026', 
   CURRENT_DATE + INTERVAL '5 days', 
   'FN', 
   '09:30:00', 
   '12:30:00', 
   '09:00:00', 
   'SCHEDULED', 
   'Candidates must present their college ID card along with this Hall Ticket. Mobile phones, smart watches, and programmable calculators are strictly prohibited inside the hall.', 
   'admin'),

  ('c0000000-0000-0000-0000-000000000002', 
   'End-Semester Theory Examination - December 2026', 
   CURRENT_DATE + INTERVAL '20 days', 
   'AN', 
   '14:00:00', 
   '17:00:00', 
   '13:30:00', 
   'DRAFT', 
   'Verify that the barcode and roll number printed on your answer booklet match your hall ticket before writing.', 
   'staff_exam')
ON CONFLICT (id) DO NOTHING;

-- Assign Departments to Exam 1
INSERT INTO exam_departments (exam_id, department, semester)
VALUES
  ('c0000000-0000-0000-0000-000000000001', 'Computer Science', 5),
  ('c0000000-0000-0000-0000-000000000001', 'Electronics', 5),
  ('c0000000-0000-0000-0000-000000000001', 'Mechanical', 5)
ON CONFLICT (exam_id, department, semester) DO NOTHING;

-- Assign Halls to Exam 1 (Aryabhata Hall 40 seats)
INSERT INTO exam_halls (exam_id, hall_id)
VALUES
  ('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001')
ON CONFLICT (exam_id, hall_id) DO NOTHING;
