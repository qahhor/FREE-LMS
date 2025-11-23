-- Initial Database Setup for LMS

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- This file is executed only once during database initialization
-- TypeORM will handle table creation through entities

-- Insert default categories
INSERT INTO categories (name, slug, description, is_active, created_at, updated_at)
VALUES
  ('Programming', 'programming', 'Programming and Software Development', true, NOW(), NOW()),
  ('Business', 'business', 'Business and Entrepreneurship', true, NOW(), NOW()),
  ('Design', 'design', 'Design and Creativity', true, NOW(), NOW()),
  ('Marketing', 'marketing', 'Marketing and Sales', true, NOW(), NOW()),
  ('Personal Development', 'personal-development', 'Personal Development and Productivity', true, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_courses_status ON courses(status);
CREATE INDEX IF NOT EXISTS idx_courses_instructor ON courses(instructor_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_user ON enrollments(user_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course ON enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_status ON enrollments(status);
