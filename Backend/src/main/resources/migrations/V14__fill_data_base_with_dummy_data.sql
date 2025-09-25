-- ===========================================
-- V__13_fill_data_base_with_dummy_data.sql
-- ===========================================
-- This migration fills the database with dummy data for testing and development purposes.
-- Note: Passwords are hashed using BCrypt. In a real application, use proper password hashing.

-- ================================
-- 1. INSERT DUMMY USERS
-- ================================

-- Admin user
INSERT INTO users (name, email, phone, password, role_id, is_active, email_verified, bio, profile_picture_url)
VALUES
    ('System Admin', 'admin@elearning.com', '+1234567890', '$2a$10$dummy.hash.for.admin', 1, true, true, 'System administrator', 'https://example.com/admin.jpg'),
    ('John Teacher', 'john.teacher@elearning.com', '+1234567891', '$2a$10$dummy.hash.for.teacher1', 2, true, true, 'Experienced programming instructor', 'https://example.com/john.jpg'),
    ('Sarah Instructor', 'sarah.instructor@elearning.com', '+1234567892', '$2a$10$dummy.hash.for.teacher2', 2, true, true, 'Data science expert', 'https://example.com/sarah.jpg'),
    ('Mike Developer', 'mike.dev@elearning.com', '+1234567893', '$2a$10$dummy.hash.for.teacher3', 2, true, true, 'Full-stack developer and mentor', 'https://example.com/mike.jpg'),
    ('Alice Student', 'alice.student@elearning.com', '+1234567894', '$2a$10$dummy.hash.for.student1', 3, true, true, 'Aspiring software engineer', 'https://example.com/alice.jpg'),
    ('Bob Learner', 'bob.learner@elearning.com', '+1234567895', '$2a$10$dummy.hash.for.student2', 3, true, true, 'Learning new technologies', 'https://example.com/bob.jpg'),
    ('Charlie User', 'charlie.user@elearning.com', '+1234567896', '$2a$10$dummy.hash.for.student3', 3, true, true, 'Tech enthusiast', 'https://example.com/charlie.jpg'),
    ('Diana Analyst', 'diana.analyst@elearning.com', '+1234567897', '$2a$10$dummy.hash.for.student4', 3, true, true, 'Data analyst in training', 'https://example.com/diana.jpg'),
    ('Eve Coder', 'eve.coder@elearning.com', '+1234567898', '$2a$10$dummy.hash.for.student5', 3, true, true, 'Passionate about coding', 'https://example.com/eve.jpg'),
    ('Frank Developer', 'frank.dev@elearning.com', '+1234567899', '$2a$10$dummy.hash.for.student6', 3, true, true, 'Backend developer', 'https://example.com/frank.jpg');

-- ================================
-- 2. INSERT DUMMY TAGS
-- ================================

INSERT INTO tags (name, description, color)
VALUES
    ('PROGRAMMING', 'General programming concepts and languages', '#FF5733'),
    ('DATA_SCIENCE', 'Data analysis, machine learning, and statistics', '#33FF57'),
    ('MACHINE_LEARNING', 'AI and machine learning algorithms', '#3357FF'),
    ('WEB_DEVELOPMENT', 'Frontend and backend web development', '#FF33A8'),
    ('MOBILE_DEVELOPMENT', 'iOS and Android app development', '#A833FF'),
    ('CLOUD_COMPUTING', 'AWS, Azure, and cloud technologies', '#33FFF6'),
    ('CYBER_SECURITY', 'Security, encryption, and ethical hacking', '#FF8C33'),
    ('DEVOPS', 'CI/CD, containers, and infrastructure', '#8C33FF'),
    ('DATABASES', 'SQL, NoSQL, and database design', '#33FF8C'),
    ('SOFTWARE_ENGINEERING', 'Software design and architecture', '#FFC300');

-- ================================
-- 3. INSERT DUMMY COURSES
-- ================================

INSERT INTO courses (name, description, status, one_time_price, subscription_price_3_months, subscription_price_6_months, currency, thumbnail_url, preview_video_url, estimated_duration_hours, difficulty_level, created_by, is_active)
VALUES
    ('Introduction to Java Programming', 'Learn the fundamentals of Java programming from scratch', 'PUBLISHED', 49.99, 24.99, 44.99, 'USD', 'https://example.com/java-intro.jpg', 'https://example.com/java-preview.mp4', 40, 'BIGINNER', 2, true),
    ('Advanced Python for Data Science', 'Master Python for data analysis and machine learning', 'PUBLISHED', 79.99, 39.99, 74.99, 'USD', 'https://example.com/python-ds.jpg', 'https://example.com/python-preview.mp4', 60, 'INTERMEDIATE', 3, true),
    ('React.js Web Development', 'Build modern web applications with React', 'PUBLISHED', 59.99, 29.99, 54.99, 'USD', 'https://example.com/react.jpg', 'https://example.com/react-preview.mp4', 45, 'INTERMEDIATE', 4, true),
    ('Machine Learning Fundamentals', 'Understand core ML concepts and algorithms', 'DRAFT', 89.99, 44.99, 84.99, 'USD', 'https://example.com/ml.jpg', 'https://example.com/ml-preview.mp4', 70, 'ADVANCED', 3, true),
    ('Database Design and SQL', 'Learn to design and query relational databases', 'PUBLISHED', 39.99, 19.99, 34.99, 'USD', 'https://example.com/sql.jpg', 'https://example.com/sql-preview.mp4', 30, 'BIGINNER', 2, true),
    ('AWS Cloud Computing', 'Master Amazon Web Services cloud platform', 'PUBLISHED', 99.99, 49.99, 94.99, 'USD', 'https://example.com/aws.jpg', 'https://example.com/aws-preview.mp4', 80, 'ADVANCED', 4, true),
    ('Cyber Security Essentials', 'Learn basic security principles and practices', 'PUBLISHED', 69.99, 32.99, 59.99, 'USD', 'https://example.com/security.jpg', 'https://example.com/security-preview.mp4', 50, 'INTERMEDIATE', 2, true),
    ('Mobile App Development with Flutter', 'Build cross-platform mobile apps', 'DRAFT', 74.99, 36.99, 69.99, 'USD', 'https://example.com/flutter.jpg', 'https://example.com/flutter-preview.mp4', 55, 'INTERMEDIATE', 4, true),
    ('DevOps with Docker and Kubernetes', 'Learn containerization and orchestration', 'PUBLISHED', 84.99, 41.99, 79.99, 'USD', 'https://example.com/devops.jpg', 'https://example.com/devops-preview.mp4', 65, 'ADVANCED', 4, true),
    ('Free JavaScript Basics', 'Free introduction to JavaScript programming', 'PUBLISHED', 0.00, 0.00, 0.00, 'USD', 'https://example.com/js-free.jpg', 'https://example.com/js-preview.mp4', 20, 'BIGINNER', 2, true);

-- ================================
-- 4. LINK COURSES TO TAGS
-- ================================

INSERT INTO course_tags (course_id, tag_id)
VALUES
    (1, 1), (1, 10), -- Java: Programming, Software Engineering
    (2, 2), (2, 3), -- Python DS: Data Science, Machine Learning
    (3, 4), (3, 1), -- React: Web Development, Programming
    (4, 3), (4, 2), -- ML: Machine Learning, Data Science
    (5, 9), (5, 1), -- SQL: Databases, Programming
    (6, 5), (6, 8), -- AWS: Cloud Computing, DevOps
    (7, 6), (7, 1), -- Security: Cyber Security, Programming
    (8, 5), (8, 1), -- Flutter: Mobile Development, Programming
    (9, 8), (9, 5), -- DevOps: DevOps, Cloud Computing
    (10, 1), (10, 4); -- JS Free: Programming, Web Development

-- ================================
-- 5. INSERT DUMMY MODULES
-- ================================

INSERT INTO modules (name, description, status, estimated_duration_minutes, created_by, is_active)
VALUES
    -- Java Course Modules
    ('Java Basics', 'Variables, data types, and basic syntax', 'PUBLISHED', 120, 2, true),
    ('Object-Oriented Programming', 'Classes, objects, inheritance, and polymorphism', 'PUBLISHED', 180, 2, true),
    ('Collections and Generics', 'Lists, sets, maps, and generic types', 'PUBLISHED', 150, 2, true),
    ('Exception Handling', 'Try-catch blocks and error management', 'PUBLISHED', 90, 2, true),

    -- Python DS Course Modules
    ('Python Fundamentals', 'Basic Python syntax and data structures', 'PUBLISHED', 180, 3, true),
    ('NumPy and Pandas', 'Data manipulation with NumPy and Pandas', 'PUBLISHED', 240, 3, true),
    ('Data Visualization', 'Creating charts and graphs with Matplotlib', 'PUBLISHED', 150, 3, true),
    ('Statistical Analysis', 'Basic statistics and data analysis', 'PUBLISHED', 180, 3, true),

    -- React Course Modules
    ('React Components', 'Creating and managing React components', 'PUBLISHED', 150, 4, true),
    ('State Management', 'useState, useEffect, and component lifecycle', 'PUBLISHED', 180, 4, true),
    ('Routing with React Router', 'Client-side routing and navigation', 'PUBLISHED', 120, 4, true),

    -- SQL Course Modules
    ('SQL Basics', 'SELECT, INSERT, UPDATE, DELETE statements', 'PUBLISHED', 120, 2, true),
    ('Joins and Relationships', 'INNER, LEFT, RIGHT, and FULL JOINs', 'PUBLISHED', 150, 2, true),
    ('Database Design', 'Normalization and database schema design', 'PUBLISHED', 120, 2, true);

-- ================================
-- 6. LINK COURSES TO MODULES
-- ================================

INSERT INTO course_modules (course_id, module_id, module_order, is_active)
VALUES
    -- Java Course (ID: 1)
    (1, 1, 1, true), -- Java Basics
    (1, 2, 2, true), -- OOP
    (1, 3, 3, true), -- Collections
    (1, 4, 4, true), -- Exception Handling

    -- Python DS Course (ID: 2)
    (2, 5, 1, true), -- Python Fundamentals
    (2, 6, 2, true), -- NumPy and Pandas
    (2, 7, 3, true), -- Data Visualization
    (2, 8, 4, true), -- Statistical Analysis

    -- React Course (ID: 3)
    (3, 9, 1, true), -- React Components
    (3, 10, 2, true), -- State Management
    (3, 11, 3, true), -- Routing

    -- SQL Course (ID: 5)
    (5, 12, 1, true), -- SQL Basics
    (5, 13, 2, true), -- Joins
    (5, 14, 3, true); -- Database Design

-- ================================
-- 7. INSERT DUMMY VIDEOS
-- ================================

INSERT INTO videos (title, metadata, video_url, thumbnail_url, duration_seconds, uploaded_by, is_active)
VALUES
    -- Java Videos
    ('Variables and Data Types', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/java-vars.mp4', 'https://example.com/java-vars.jpg', 600, 2, true),
    ('Control Structures', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/java-control.mp4', 'https://example.com/java-control.jpg', 720, 2, true),
    ('Classes and Objects', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/java-classes.mp4', 'https://example.com/java-classes.jpg', 900, 2, true),
    ('Inheritance in Java', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/java-inheritance.mp4', 'https://example.com/java-inheritance.jpg', 800, 2, true),
    ('ArrayList and Collections', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/java-collections.mp4', 'https://example.com/java-collections.jpg', 750, 2, true),
    ('Try-Catch Blocks', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/java-exceptions.mp4', 'https://example.com/java-exceptions.jpg', 450, 2, true),

    -- Python Videos
    ('Python Variables', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/python-vars.mp4', 'https://example.com/python-vars.jpg', 480, 3, true),
    ('Lists and Dictionaries', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/python-lists.mp4', 'https://example.com/python-lists.jpg', 600, 3, true),
    ('NumPy Arrays', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/numpy-intro.mp4', 'https://example.com/numpy-intro.jpg', 900, 3, true),
    ('Pandas DataFrames', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/pandas-intro.mp4', 'https://example.com/pandas-intro.jpg', 1200, 3, true),
    ('Matplotlib Charts', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/matplotlib-intro.mp4', 'https://example.com/matplotlib-intro.jpg', 750, 3, true),

    -- React Videos
    ('JSX and Components', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/react-jsx.mp4', 'https://example.com/react-jsx.jpg', 600, 4, true),
    ('useState Hook', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/react-usestate.mp4', 'https://example.com/react-usestate.jpg', 720, 4, true),
    ('React Router Setup', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/react-router.mp4', 'https://example.com/react-router.jpg', 480, 4, true),

    -- SQL Videos
    ('SELECT Statement', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/sql-select.mp4', 'https://example.com/sql-select.jpg', 450, 2, true),
    ('JOIN Operations', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/sql-joins.mp4', 'https://example.com/sql-joins.jpg', 600, 2, true),
    ('Database Normalization', '{"format": "mp4", "quality": "1080p"}', 'https://example.com/sql-normalization.mp4', 'https://example.com/sql-normalization.jpg', 540, 2, true);

-- ================================
-- 8. LINK MODULES TO VIDEOS
-- ================================

INSERT INTO module_videos (module_id, video_id, video_order, is_active)
VALUES
    -- Java Basics Module (ID: 1)
    (1, 1, 1, true), -- Variables
    (1, 2, 2, true), -- Control Structures

    -- OOP Module (ID: 2)
    (2, 3, 1, true), -- Classes and Objects
    (2, 4, 2, true), -- Inheritance

    -- Collections Module (ID: 3)
    (3, 5, 1, true), -- ArrayList and Collections

    -- Exception Handling Module (ID: 4)
    (4, 6, 1, true), -- Try-Catch Blocks

    -- Python Fundamentals Module (ID: 5)
    (5, 7, 1, true), -- Python Variables
    (5, 8, 2, true), -- Lists and Dictionaries

    -- NumPy and Pandas Module (ID: 6)
    (6, 9, 1, true), -- NumPy Arrays
    (6, 10, 2, true), -- Pandas DataFrames

    -- Data Visualization Module (ID: 7)
    (7, 11, 1, true), -- Matplotlib Charts

    -- React Components Module (ID: 9)
    (9, 12, 1, true), -- JSX and Components

    -- State Management Module (ID: 10)
    (10, 13, 1, true), -- useState Hook

    -- Routing Module (ID: 11)
    (11, 14, 1, true), -- React Router Setup

    -- SQL Basics Module (ID: 12)
    (12, 15, 1, true), -- SELECT Statement

    -- Joins Module (ID: 13)
    (13, 16, 1, true), -- JOIN Operations

    -- Database Design Module (ID: 14)
    (14, 17, 1, true); -- Database Normalization

-- ================================
-- 9. INSERT DUMMY PACKAGES
-- ================================

INSERT INTO packages (name, description, price, discount_percentage, is_active)
VALUES
    ('Programming Essentials Bundle', 'Complete programming foundation with Java, Python, and SQL', 149.99, 20.00, true),
    ('Web Development Starter Pack', 'Frontend and backend web development courses', 99.99, 15.00, true),
    ('Data Science Complete Package', 'Full data science curriculum from basics to advanced', 199.99, 25.00, true),
    ('Cloud Computing Suite', 'AWS and DevOps cloud technologies', 159.99, 18.00, true);

-- ================================
-- 10. LINK PACKAGES TO COURSES
-- ================================

INSERT INTO package_courses (package_id, course_id)
VALUES
    -- Programming Essentials Bundle
    (1, 1), -- Java
    (1, 2), -- Python DS
    (1, 5), -- SQL

    -- Web Development Starter Pack
    (2, 3), -- React
    (2, 10), -- JavaScript Free

    -- Data Science Complete Package
    (3, 2), -- Python DS
    (3, 4), -- Machine Learning

    -- Cloud Computing Suite
    (4, 6), -- AWS
    (4, 9); -- DevOps

-- ================================
-- 11. INSERT DUMMY USER COURSE ACCESS
-- ================================

INSERT INTO user_course_access (user_id, course_id, package_id, access_type, access_until, is_active)
VALUES
    -- Alice has access to Java and Python courses
    (5, 1, NULL, 'PURCHASED', CURRENT_TIMESTAMP + INTERVAL '1 year', true),
    (5, 2, NULL, 'SUBSCRIPTION_ACCESS', CURRENT_TIMESTAMP + INTERVAL '1 year', true),

    -- Bob has access to React and SQL
    (6, 3, NULL, 'PURCHASED', CURRENT_TIMESTAMP + INTERVAL '1 year', true),
    (6, 5, NULL, 'FREE', CURRENT_TIMESTAMP + INTERVAL '1 year', true),

    -- Charlie has access to AWS and Security via package
    (7, 6, 4, 'PACKAGE_ACCESS', CURRENT_TIMESTAMP + INTERVAL '1 year', true),
    (7, 7, NULL, 'PURCHASED', CURRENT_TIMESTAMP + INTERVAL '1 year', true),

    -- Diana has access to free JavaScript course
    (8, 10, NULL, 'FREE', CURRENT_TIMESTAMP + INTERVAL '1 year', true),

    -- Eve has completed SQL course
    (9, 5, NULL, 'PURCHASED', CURRENT_TIMESTAMP + INTERVAL '1 year', true),

    -- Frank has access to DevOps course
    (10, 9, NULL, 'SUBSCRIPTION_ACCESS', CURRENT_TIMESTAMP + INTERVAL '1 year', true);

-- ================================
-- 12. INSERT DUMMY QUIZZES
-- ================================

INSERT INTO quizzes (video_id, title, total_score, is_active)
VALUES
    (1, 'Java Fundamentals Quiz', 100, true), -- Java course
    (2, 'Python Data Structures Quiz', 100, true), -- Python DS course
    (5, 'SQL Query Quiz', 100, true); -- SQL course

-- ================================
-- 13. INSERT QUIZ QUESTIONS AND OPTIONS
-- ================================

-- Java Quiz Questions
INSERT INTO quiz_question (quiz_id, text, question_mark)
VALUES
    (1, 'What is the correct way to declare a variable in Java?', 10),
    (1, 'Which of the following is not a primitive data type in Java?', 10),
    (1, 'What does OOP stand for?', 10);

INSERT INTO quiz_options (question_id, text, is_correct)
VALUES
    (1, 'int number = 5;', true),
    (1, 'number int = 5;', false),
    (1, 'int = number 5;', false),
    (1, 'number = int 5;', false),

    (2, 'int', false),
    (2, 'double', false),
    (2, 'String', true),
    (2, 'boolean', false),

    (3, 'Object Oriented Programming', true),
    (3, 'Object Operation Process', false),
    (3, 'Open Object Protocol', false),
    (3, 'Ordered Object Processing', false);

-- Python Quiz Questions
INSERT INTO quiz_question (quiz_id, text, question_mark)
VALUES
    (2, 'What is the output of print(2 ** 3)?', 10),
    (2, 'Which data structure is mutable in Python?', 10);

INSERT INTO quiz_options (question_id, text, is_correct)
VALUES
    (4, '6', false),
    (4, '8', true),
    (4, '9', false),
    (4, '16', false),

    (5, 'tuple', false),
    (5, 'string', false),
    (5, 'list', true),
    (5, 'int', false);

-- SQL Quiz Questions
INSERT INTO quiz_question (quiz_id, text, question_mark)
VALUES
    (3, 'Which SQL command is used to retrieve data from a database?', 10),
    (3, 'What does SQL stand for?', 10);

INSERT INTO quiz_options (question_id, text, is_correct)
VALUES
    (6, 'SELECT', true),
    (6, 'GET', false),
    (6, 'RETRIEVE', false),
    (6, 'FETCH', false),

    (7, 'Structured Query Language', true),
    (7, 'Simple Query Language', false),
    (7, 'Standard Query Language', false),
    (7, 'System Query Language', false);

-- ================================
-- 14. INSERT DUMMY QUIZ SUBMISSIONS
-- ================================

INSERT INTO quiz_submissions (user_id, quiz_id, score)
VALUES
    (5, 1, 80), -- Alice took Java quiz
    (6, 2, 90), -- Bob took Python quiz
    (9, 3, 95); -- Eve took SQL quiz

-- ================================
-- 15. INSERT DUMMY STUDENT ANSWERS
-- ================================



INSERT INTO student_answer (submission_id, question_id, selected_option_id, is_correct)
VALUES
  -- Submission 1 (Alice on Java Quiz)
  (1, 1, 1, true),  -- Correct: Q1 -> 'int number = 5;' (Option ID 1)
  (1, 2, 7, true),  -- Correct: Q2 -> 'String' (Option ID 7)
  (1, 3, 9, true),  -- Correct: Q3 -> 'Object Oriented Programming' (Option ID 9)

  -- Submission 2 (Bob on Python Quiz)
  (2, 4, 14, true), -- Correct: Q4 -> '8' (Option ID 14)
  (2, 5, 19, true), -- Correct: Q5 -> 'list' (Option ID 19)

  -- Submission 3 (Eve on SQL Quiz)
  (3, 6, 21, true), -- Correct: Q6 -> 'SELECT' (Option ID 21)
  (3, 7, 25, true); -- Correct: Q7 -> 'Structured Query Language' (Option ID 25)

-- ================================
-- 16. INSERT DUMMY USER WATCHED VIDEOS
-- ================================

INSERT INTO user_watched_videos (user_id, video_id)
VALUES
    (5, 1), (5, 2), (5, 3), -- Alice watched some Java videos
    (6, 12), (6, 13), -- Bob watched some React videos
    (7, 11), (7, 12), -- Charlie watched some SQL videos
    (8, 4), -- Diana watched JS video
    (3, 2), (3, 3), (3, 4), -- Eve watched all SQL videos
    (5, 9), (5, 10); -- Frank watched some Python videos

