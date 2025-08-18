-- ================================
-- 1. ROLES & PERMISSIONS SYSTEM
-- ================================

CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL UNIQUE,
                       description TEXT,
                       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (name, description) VALUES
                                          ('ADMIN', 'System administrator with full access'),
                                          ('TEACHER', 'Course instructor and content creator'),
                                          ('USER', 'Regular student user'),
                                          ('LEAD_INSTRUCTOR', 'Lead instructor for specific courses'),
                                          ('TEACHING_ASSISTANT', 'Teaching assistant for specific courses'),
                                          ('GUEST_LECTURER', 'Guest lecturer for specific courses'),
                                          ('COURSE_MODERATOR', 'Course moderator for specific courses');

CREATE TABLE permissions (
                             id SERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE,
                             description TEXT,
                             created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO permissions (name, description) VALUES
                                                ('user:write', 'Create and modify user accounts'),
                                                ('user:read', 'View user information'),
                                                ('course:write', 'Create and modify courses'),
                                                ('course:read', 'View course content'),
                                                ('enrollment:manage', 'Manage course enrollments'),
                                                ('payment:manage', 'Handle payments and subscriptions'),
                                                ('analytics:read', 'View analytics and reports');

CREATE TABLE role_permissions (
                                  id SERIAL PRIMARY KEY,
                                  role_id INTEGER NOT NULL,
                                  permission_id INTEGER NOT NULL,
                                  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_role_permissions_role
                                      FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_role_permissions_permission
                                      FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
                                  CONSTRAINT unique_role_permission UNIQUE(role_id, permission_id)
);

INSERT INTO role_permissions (role_id, permission_id) VALUES
                                                          (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), -- ADMIN gets all
                                                          (2, 3), (2, 4), (2, 5), (2, 7), -- TEACHER
                                                          (3, 4); -- USER gets read access to courses

-- ================================
-- 2. USERS SYSTEM
-- ================================

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(250) NOT NULL,
                       email VARCHAR(250) NOT NULL UNIQUE,
                       phone VARCHAR(250),
                       password VARCHAR(250) NOT NULL,
                       role_id INTEGER NOT NULL,
                       is_active BOOLEAN DEFAULT TRUE,
                       email_verified BOOLEAN DEFAULT FALSE,
                       last_login_at TIMESTAMPTZ,
                       profile_picture_url TEXT,
                       bio TEXT,
                       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_users_role
                           FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
                       CONSTRAINT chk_email_format
                           CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
                       CONSTRAINT chk_password_length
                           CHECK (LENGTH(password) >= 8)
);

-- ================================
-- 3. SUBSCRIPTION PLANS & PRICING
-- ================================

CREATE TABLE subscription_plans (
                                    id SERIAL PRIMARY KEY,
                                    name VARCHAR(100) NOT NULL UNIQUE,
                                    description TEXT,
                                    price DECIMAL(10, 2) NOT NULL,
                                    currency VARCHAR(3) DEFAULT 'USD',
                                    billing_cycle VARCHAR(20) NOT NULL CHECK (billing_cycle IN ('monthly', 'quarterly', 'annual', 'lifetime')),
                                    access_duration_days INTEGER, -- NULL for lifetime
                                    features JSONB,
                                    max_courses INTEGER, -- NULL for unlimited
                                    trial_days INTEGER DEFAULT 0,
                                    is_active BOOLEAN DEFAULT TRUE,
                                    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO subscription_plans (name, description, price, billing_cycle, max_courses) VALUES
                                                                                          ('FREE', 'Basic free access', 0.00, 'lifetime', 3),
                                                                                          ('Basic Monthly', 'Monthly subscription', 19.99, 'monthly', 10),
                                                                                          ('Premium Monthly', 'Premium monthly access', 39.99, 'monthly', NULL),
                                                                                          ('Premium Annual', 'Premium annual access', 399.99, 'annual', NULL);

-- ================================
-- 4. COURSES SYSTEM
-- ================================

CREATE TYPE course_status AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
CREATE TYPE access_model AS ENUM ('FREE', 'ONE_TIME', 'SUBSCRIPTION');

CREATE TABLE courses (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(250) NOT NULL,
                         description TEXT,
                         status course_status DEFAULT 'DRAFT',
                         access_model access_model DEFAULT 'SUBSCRIPTION',
                         one_time_price DECIMAL(10, 2) DEFAULT 0.00,
                         currency VARCHAR(3) DEFAULT 'USD',
                         required_plan_level INTEGER DEFAULT 1,
                         thumbnail_url TEXT,
                         preview_video_url TEXT,
                         estimated_duration_hours INTEGER,
                         difficulty_level VARCHAR(20) CHECK (difficulty_level IN ('BIGINNER', 'INTERMEDIATE', 'ADVANCED')),
                         created_by INTEGER NOT NULL,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    -- Generated column for free courses
                         is_free BOOLEAN GENERATED ALWAYS AS (access_model = 'FREE' OR one_time_price = 0.00) STORED,

                         CONSTRAINT fk_courses_creator
                             FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);

-- ================================
-- 5. SUBSCRIPTIONS SYSTEM
-- ================================

CREATE TYPE subscription_status AS ENUM ('ACTIVE', 'CANCELLED', 'EXPIRED', 'SUSPENDED', 'PENDING');

CREATE TABLE subscriptions (
                               id SERIAL PRIMARY KEY,
                               user_id INTEGER NOT NULL,
                               plan_id INTEGER NOT NULL,

    -- Subscription Status
                               status subscription_status DEFAULT 'PENDING',

    -- Billing Cycle Info
                               current_period_start TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                               current_period_end TIMESTAMPTZ NOT NULL,
                               next_billing_date TIMESTAMPTZ,

    -- Subscription Management
                               auto_renew BOOLEAN DEFAULT TRUE,
                               cancelled_at TIMESTAMPTZ,
                               cancellation_reason TEXT,

    -- Payment Integration
                               stripe_subscription_id VARCHAR(255),
                               trial_start TIMESTAMPTZ,
                               trial_end TIMESTAMPTZ,

    -- Audit Fields
                               created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                               is_active BOOLEAN DEFAULT TRUE,

                               CONSTRAINT fk_subscriptions_user
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
                               CONSTRAINT fk_subscriptions_plan
                                   FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE RESTRICT
);

-- ================================
-- 6. PAYMENTS SYSTEM
-- ================================

CREATE TYPE payment_status AS ENUM ('PENDING', 'COMPLETED', 'failed', 'CANCELLED', 'REFUNDED');
CREATE TYPE payment_type AS ENUM ('SUBSCRIPTION', 'COURSE_PURCHASE', 'REFUND');

CREATE TABLE payments (
                          id SERIAL PRIMARY KEY,
                          user_id INTEGER NOT NULL,
                          subscription_id INTEGER,
                          course_id INTEGER,

    -- Payment Details
                          amount DECIMAL(10, 2) NOT NULL,
                          currency VARCHAR(3) DEFAULT 'USD',
                          payment_type payment_type NOT NULL,

    -- Payment Status
                          status payment_status DEFAULT 'PENDING',

    -- Payment Method & Processor
                          payment_method VARCHAR(50) NOT NULL,
                          payment_processor_id VARCHAR(255),
                          processor_response JSONB,

    -- Transaction Details
                          transaction_id VARCHAR(255) UNIQUE,
                          processed_at TIMESTAMPTZ,
                          refunded_at TIMESTAMPTZ,
                          refund_amount DECIMAL(10, 2) DEFAULT 0.00,

    -- Audit Fields
                          created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                          is_active BOOLEAN DEFAULT TRUE,

    -- Foreign Key Constraints
                          CONSTRAINT fk_payments_user
                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
                          CONSTRAINT fk_payments_subscription
                              FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE SET NULL,
                          CONSTRAINT fk_payments_course
                              FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL,

    -- Business Logic Constraints
                          CONSTRAINT chk_payment_refs CHECK (
                              (payment_type = 'SUBSCRIPTION' AND subscription_id IS NOT NULL) OR
                              (payment_type = 'COURSE_PURCHASE' AND course_id IS NOT NULL) OR
                              (payment_type = 'REFUND')
                              )
);

-- ================================
-- 7. COURSE ENROLLMENTS
-- ================================

CREATE TYPE enrollment_status AS ENUM ('ENROLLED', 'IN_PROGRESS', 'COMPLETED', 'DROPPED', 'SUSPENDED');
CREATE TYPE enrollment_access_type AS ENUM ('FREE', 'SUBSCRIPTION', 'ONE_TIME_PURCHASE', 'ADMIN_GRANTED');

CREATE TABLE course_enrolments (
                                   id SERIAL PRIMARY KEY,
                                   user_id INTEGER NOT NULL,
                                   course_id INTEGER NOT NULL,

    -- Access Method
                                   subscription_id INTEGER,
                                   payment_id INTEGER,
                                   access_type enrollment_access_type NOT NULL,

    -- Enrollment Status
                                   status enrollment_status DEFAULT 'ENROLLED',

    -- Progress Tracking
                                   enrollment_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                   first_accessed_at TIMESTAMPTZ,
                                   last_accessed_at TIMESTAMPTZ,
                                   completed_at TIMESTAMPTZ,
                                   completion_percentage DECIMAL(5, 2) DEFAULT 0.00,

    -- Access Control
                                   access_expires_at TIMESTAMPTZ,

    -- Audit Fields
                                   created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                   is_active BOOLEAN DEFAULT TRUE,

    -- Foreign Key Constraints
                                   CONSTRAINT fk_enrolments_user
                                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
                                   CONSTRAINT fk_enrolments_course
                                       FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE RESTRICT,
                                   CONSTRAINT fk_enrolments_subscription
                                       FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE SET NULL,
                                   CONSTRAINT fk_enrolments_payment
                                       FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL,

    -- Business Logic Constraints
                                   CONSTRAINT unique_user_course_enrollment
                                       UNIQUE(user_id, course_id),
                                   CONSTRAINT chk_access_method CHECK (
                                       (access_type = 'SUBSCRIPTION' AND subscription_id IS NOT NULL) OR
                                       (access_type = 'ONE_TIME_PURCHASE' AND payment_id IS NOT NULL) OR
                                       (access_type IN ('FREE', 'ADMIN_GRANTED'))
                                       )
);

-- ================================
-- 8. SUBSCRIPTION PLAN ACCESS
-- ================================

CREATE TABLE plan_course_access (
                                    id SERIAL PRIMARY KEY,
                                    plan_id INTEGER NOT NULL,
                                    course_id INTEGER NOT NULL,
                                    included_in_plan BOOLEAN DEFAULT TRUE,
                                    access_level VARCHAR(20) DEFAULT 'full' CHECK (access_level IN ('full', 'preview', 'limited')),
                                    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                    is_active BOOLEAN DEFAULT TRUE,

                                    CONSTRAINT fk_plan_access_plan
                                        FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE CASCADE,
                                    CONSTRAINT fk_plan_access_course
                                        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                                    CONSTRAINT unique_plan_course
                                        UNIQUE(plan_id, course_id)
);

-- ================================
-- 9. COURSE TEACHERS
-- ================================

CREATE TABLE course_teachers (
                                 id SERIAL PRIMARY KEY,
                                 course_id INTEGER NOT NULL,
                                 user_id INTEGER NOT NULL,
                                 role_id INTEGER,
                                 assigned_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                 is_active BOOLEAN DEFAULT TRUE,
                                 created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_course_teachers_course
                                     FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_course_teachers_user
                                     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
                                 CONSTRAINT fk_course_teachers_role
                                     FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
                                 CONSTRAINT unique_course_teacher_role
                                     UNIQUE(course_id, user_id, role_id)
);

-- ================================
-- 10. MODULES & CONTENT STRUCTURE
-- ================================

CREATE TABLE modules (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(250) NOT NULL,
                         description TEXT,
                         status course_status DEFAULT 'DRAFT',
                         estimated_duration_minutes INTEGER,
                         created_by INTEGER NOT NULL,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_modules_creator
                             FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE course_modules (
                                id SERIAL PRIMARY KEY,
                                course_id INTEGER NOT NULL,
                                module_id INTEGER NOT NULL,
                                module_order INTEGER NOT NULL,
                                is_active BOOLEAN DEFAULT TRUE,
                                created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_course_modules_course
                                    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                                CONSTRAINT fk_course_modules_module
                                    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE RESTRICT,
                                CONSTRAINT unique_course_module_order
                                    UNIQUE(course_id, module_order)
);

-- ================================
-- 11. VIDEOS & ATTACHMENTS
-- ================================

CREATE TABLE videos (
                        id SERIAL PRIMARY KEY,
                        title VARCHAR(250),
                        metadata JSONB NOT NULL,
                        video_url TEXT,
                        thumbnail_url TEXT,
                        duration_seconds INTEGER,
                        uploaded_by INTEGER NOT NULL,
                        is_active BOOLEAN DEFAULT TRUE,
                        created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_videos_uploader
                            FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE attachments (
                             id SERIAL PRIMARY KEY,
                             title VARCHAR(250),
                             metadata JSONB NOT NULL,
                             file_url TEXT,
                             file_size_bytes BIGINT,
                             file_type VARCHAR(100),
                             uploaded_by INTEGER NOT NULL,
                             is_active BOOLEAN DEFAULT TRUE,
                             created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_attachments_uploader
                                 FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE module_videos (
                               id SERIAL PRIMARY KEY,
                               module_id INTEGER NOT NULL,
                               video_id INTEGER NOT NULL,
                               video_order INTEGER NOT NULL,
                               is_active BOOLEAN DEFAULT TRUE,
                               created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_module_videos_module
                                   FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,
                               CONSTRAINT fk_module_videos_video
                                   FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE RESTRICT,
                               CONSTRAINT unique_module_video_order
                                   UNIQUE(module_id, video_order)
);

CREATE TABLE video_attachments (
                                   id SERIAL PRIMARY KEY,
                                   video_id INTEGER NOT NULL,
                                   attachment_id INTEGER NOT NULL,
                                   is_active BOOLEAN DEFAULT TRUE,
                                   created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_video_attachments_video
                                       FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_video_attachments_attachment
                                       FOREIGN KEY (attachment_id) REFERENCES attachments(id) ON DELETE RESTRICT
);

-- ================================
-- 12. QUIZZES & ASSESSMENTS
-- ================================

CREATE TABLE quizzes (
                         id SERIAL PRIMARY KEY,
                         course_id INTEGER NOT NULL,
                         name VARCHAR(250) NOT NULL,
                         description TEXT,
                         status course_status DEFAULT 'DRAFT',
                         max_attempts INTEGER DEFAULT 3,
                         passing_score DECIMAL(5, 2) DEFAULT 70.00,
                         time_limit_minutes INTEGER,
                         created_by INTEGER NOT NULL,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_quizzes_course
                             FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                         CONSTRAINT fk_quizzes_creator
                             FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE quiz_questions (
                                id SERIAL PRIMARY KEY,
                                quiz_id INTEGER NOT NULL,
                                question_text TEXT NOT NULL,
                                question_type VARCHAR(20) DEFAULT 'multiple_choice'
                                    CHECK (question_type IN ('multiple_choice', 'true_false', 'short_answer')),
                                points DECIMAL(5, 2) DEFAULT 1.00,
                                question_order INTEGER,
                                is_active BOOLEAN DEFAULT TRUE,
                                created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_quiz_questions_quiz
                                    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

CREATE TABLE quiz_answers (
                              id SERIAL PRIMARY KEY,
                              question_id INTEGER NOT NULL,
                              answer_text TEXT NOT NULL,
                              is_correct BOOLEAN DEFAULT FALSE,
                              answer_order INTEGER,
                              is_active BOOLEAN DEFAULT TRUE,
                              created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_quiz_answers_question
                                  FOREIGN KEY (question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE
);

-- ================================
-- 13. USER PROGRESS & ATTEMPTS
-- ================================

CREATE TABLE user_progress (
                               id SERIAL PRIMARY KEY,
                               enrolment_id INTEGER NOT NULL,
                               module_id INTEGER NOT NULL,
                               progress_percentage DECIMAL(5, 2) DEFAULT 0.00,
                               last_accessed TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                               completed_at TIMESTAMPTZ,
                               updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_progress_enrolment
                                   FOREIGN KEY (enrolment_id) REFERENCES course_enrolments(id) ON DELETE CASCADE,
                               CONSTRAINT fk_progress_module
                                   FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE RESTRICT,
                               CONSTRAINT unique_user_module_progress
                                   UNIQUE(enrolment_id, module_id)
);

CREATE TABLE user_quiz_attempts (
                                    id SERIAL PRIMARY KEY,
                                    enrolment_id INTEGER NOT NULL,
                                    quiz_id INTEGER NOT NULL,
                                    attempt_number INTEGER NOT NULL,
                                    score DECIMAL(5, 2) DEFAULT 0.00,
                                    max_score DECIMAL(5, 2) NOT NULL,
                                    passed BOOLEAN DEFAULT FALSE,
                                    started_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                    completed_at TIMESTAMPTZ,
                                    is_active BOOLEAN DEFAULT TRUE,
                                    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT fk_quiz_attempts_enrolment
                                        FOREIGN KEY (enrolment_id) REFERENCES course_enrolments(id) ON DELETE CASCADE,
                                    CONSTRAINT fk_quiz_attempts_quiz
                                        FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE RESTRICT
);

CREATE TABLE user_quiz_answers (
                                   id SERIAL PRIMARY KEY,
                                   attempt_id INTEGER NOT NULL,
                                   question_id INTEGER NOT NULL,
                                   answer_id INTEGER,
                                   answer_text TEXT, -- For short answer questions
                                   is_correct BOOLEAN DEFAULT FALSE,
                                   points_earned DECIMAL(5, 2) DEFAULT 0.00,
                                   is_active BOOLEAN DEFAULT TRUE,
                                   created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_user_quiz_answers_attempt
                                       FOREIGN KEY (attempt_id) REFERENCES user_quiz_attempts(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_user_quiz_answers_question
                                       FOREIGN KEY (question_id) REFERENCES quiz_questions(id) ON DELETE RESTRICT,
                                   CONSTRAINT fk_user_quiz_answers_answer
                                       FOREIGN KEY (answer_id) REFERENCES quiz_answers(id) ON DELETE SET NULL
);

-- ================================
-- 14. FEEDBACK & RATINGS
-- ================================

CREATE TABLE user_feedback (
                               id SERIAL PRIMARY KEY,
                               course_id INTEGER NOT NULL,
                               user_id INTEGER NOT NULL,
                               feedback_text TEXT NOT NULL,
                               rating INTEGER CHECK (rating >= 1 AND rating <= 5),
                               is_anonymous BOOLEAN DEFAULT FALSE,
                               is_active BOOLEAN DEFAULT TRUE,
                               created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_feedback_course
                                   FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                               CONSTRAINT fk_feedback_user
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
                               CONSTRAINT unique_user_course_feedback
                                   UNIQUE(user_id, course_id)
);

-- ================================
-- COMPREHENSIVE INDEXING STRATEGY
-- ================================

-- Users Table Indexes
CREATE INDEX idx_users_email ON users(email) WHERE is_active = true;
CREATE INDEX idx_users_role_active ON users(role_id) WHERE is_active = true;
CREATE INDEX idx_users_last_login ON users(last_login_at);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Roles & Permissions Indexes
CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions(permission_id);

-- Subscription Plans Indexes
CREATE INDEX idx_subscription_plans_active ON subscription_plans(is_active, price);
CREATE INDEX idx_subscription_plans_billing_cycle ON subscription_plans(billing_cycle) WHERE is_active = true;

-- Courses Indexes
CREATE INDEX idx_courses_status_active ON courses(status) WHERE is_active = true;
CREATE INDEX idx_courses_access_model ON courses(access_model) WHERE is_active = true;
CREATE INDEX idx_courses_price ON courses(one_time_price) WHERE is_active = true AND access_model = 'ONE_TIME';
CREATE INDEX idx_courses_free ON courses(is_free) WHERE is_active = true;
CREATE INDEX idx_courses_difficulty ON courses(difficulty_level) WHERE is_active = true;
CREATE INDEX idx_courses_created_at ON courses(created_at);
CREATE INDEX idx_courses_creator ON courses(created_by) WHERE is_active = true;
CREATE INDEX idx_courses_name_search ON courses USING gin(to_tsvector('english', name)) WHERE is_active = true;
CREATE INDEX idx_courses_description_search ON courses USING gin(to_tsvector('english', description)) WHERE is_active = true;

-- Subscriptions Indexes
CREATE INDEX idx_subscriptions_user_active ON subscriptions(user_id, status) WHERE is_active = true;
CREATE INDEX idx_subscriptions_status_active ON subscriptions(status) WHERE is_active = true;
CREATE INDEX idx_subscriptions_period_end ON subscriptions(current_period_end) WHERE status = 'ACTIVE';
CREATE INDEX idx_subscriptions_auto_renew ON subscriptions(auto_renew, next_billing_date) WHERE status = 'ACTIVE';
CREATE INDEX idx_subscriptions_stripe ON subscriptions(stripe_subscription_id) WHERE stripe_subscription_id IS NOT NULL;
CREATE INDEX idx_subscriptions_trial_end ON subscriptions(trial_end) WHERE trial_end IS NOT NULL;

-- Payments Indexes
CREATE INDEX idx_payments_user_date ON payments(user_id, created_at) WHERE is_active = true;
CREATE INDEX idx_payments_subscription ON payments(subscription_id) WHERE subscription_id IS NOT NULL;
CREATE INDEX idx_payments_course ON payments(course_id) WHERE course_id IS NOT NULL;
CREATE INDEX idx_payments_status_type ON payments(status, payment_type) WHERE is_active = true;
CREATE INDEX idx_payments_processor_id ON payments(payment_processor_id) WHERE payment_processor_id IS NOT NULL;
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id) WHERE transaction_id IS NOT NULL;
CREATE INDEX idx_payments_processed_at ON payments(processed_at) WHERE processed_at IS NOT NULL;
CREATE INDEX idx_payments_amount ON payments(amount, currency) WHERE is_active = true;

-- Course Enrollments Indexes
CREATE INDEX idx_enrolments_user_active ON course_enrolments(user_id, status) WHERE is_active = true;
CREATE INDEX idx_enrolments_course_active ON course_enrolments(course_id, status) WHERE is_active = true;
CREATE INDEX idx_enrolments_subscription ON course_enrolments(subscription_id) WHERE subscription_id IS NOT NULL;
CREATE INDEX idx_enrolments_payment ON course_enrolments(payment_id) WHERE payment_id IS NOT NULL;
CREATE INDEX idx_enrolments_access_type ON course_enrolments(access_type) WHERE is_active = true;
CREATE INDEX idx_enrolments_completion ON course_enrolments(completion_percentage) WHERE is_active = true;
CREATE INDEX idx_enrolments_last_accessed ON course_enrolments(last_accessed_at);
CREATE INDEX idx_enrolments_access_expires ON course_enrolments(access_expires_at) WHERE access_expires_at IS NOT NULL;

-- Plan Course Access Indexes
CREATE INDEX idx_plan_course_access_plan ON plan_course_access(plan_id) WHERE is_active = true;
CREATE INDEX idx_plan_course_access_course ON plan_course_access(course_id) WHERE is_active = true;
CREATE INDEX idx_plan_course_included ON plan_course_access(plan_id, included_in_plan) WHERE is_active = true;

-- Course Teachers Indexes
CREATE INDEX idx_course_teachers_course ON course_teachers(course_id) WHERE is_active = true;
CREATE INDEX idx_course_teachers_user ON course_teachers(user_id) WHERE is_active = true;
CREATE INDEX idx_course_teachers_role ON course_teachers(role_id) WHERE is_active = true;

-- Modules & Course Structure Indexes
CREATE INDEX idx_modules_status ON modules(status) WHERE is_active = true;
CREATE INDEX idx_modules_creator ON modules(created_by) WHERE is_active = true;
CREATE INDEX idx_course_modules_course ON course_modules(course_id) WHERE is_active = true;
CREATE INDEX idx_course_modules_module ON course_modules(module_id) WHERE is_active = true;
CREATE INDEX idx_course_modules_order ON course_modules(course_id, module_order) WHERE is_active = true;

-- Videos & Content Indexes
CREATE INDEX idx_videos_active ON videos(is_active, created_at);
CREATE INDEX idx_videos_duration ON videos(duration_seconds) WHERE is_active = true;
CREATE INDEX idx_videos_uploader ON videos(uploaded_by) WHERE is_active = true;
CREATE INDEX idx_videos_metadata ON videos USING gin(metadata);
CREATE INDEX idx_attachments_active ON attachments(is_active, created_at);
CREATE INDEX idx_attachments_type ON attachments(file_type) WHERE is_active = true;
CREATE INDEX idx_attachments_size ON attachments(file_size_bytes) WHERE is_active = true;
CREATE INDEX idx_attachments_uploader ON attachments(uploaded_by) WHERE is_active = true;

-- Module Videos Indexes
CREATE INDEX idx_module_videos_module ON module_videos(module_id) WHERE is_active = true;
CREATE INDEX idx_module_videos_video ON module_videos(video_id) WHERE is_active = true;
CREATE INDEX idx_module_videos_order ON module_videos(module_id, video_order) WHERE is_active = true;

-- Video Attachments Indexes
CREATE INDEX idx_video_attachments_video ON video_attachments(video_id) WHERE is_active = true;
CREATE INDEX idx_video_attachments_attachment ON video_attachments(attachment_id) WHERE is_active = true;

-- Quizzes Indexes
CREATE INDEX idx_quizzes_course ON quizzes(course_id) WHERE is_active = true;
CREATE INDEX idx_quizzes_status ON quizzes(status) WHERE is_active = true;
CREATE INDEX idx_quizzes_creator ON quizzes(created_by) WHERE is_active = true;
CREATE INDEX idx_quiz_questions_quiz ON quiz_questions(quiz_id) WHERE is_active = true;
CREATE INDEX idx_quiz_questions_order ON quiz_questions(quiz_id, question_order) WHERE is_active = true;
CREATE INDEX idx_quiz_answers_question ON quiz_answers(question_id) WHERE is_active = true;
CREATE INDEX idx_quiz_answers_correct ON quiz_answers(question_id, is_correct) WHERE is_active = true;

-- User Progress Indexes
CREATE INDEX idx_user_progress_enrolment ON user_progress(enrolment_id);
CREATE INDEX idx_user_progress_module ON user_progress(module_id);
CREATE INDEX idx_user_progress_percentage ON user_progress(progress_percentage);
CREATE INDEX idx_user_progress_last_accessed ON user_progress(last_accessed);
CREATE INDEX idx_user_progress_completed ON user_progress(completed_at) WHERE completed_at IS NOT NULL;

-- Quiz Attempts Indexes
CREATE INDEX idx_quiz_attempts_enrolment ON user_quiz_attempts(enrolment_id) WHERE is_active = true;
CREATE INDEX idx_quiz_attempts_quiz ON user_quiz_attempts(quiz_id) WHERE is_active = true;
CREATE INDEX idx_quiz_attempts_score ON user_quiz_attempts(score) WHERE is_active = true;
CREATE INDEX idx_quiz_attempts_passed ON user_quiz_attempts(passed) WHERE is_active = true;
CREATE INDEX idx_quiz_attempts_completed ON user_quiz_attempts(completed_at) WHERE completed_at IS NOT NULL;

-- Quiz Answers Indexes
CREATE INDEX idx_user_quiz_answers_attempt ON user_quiz_answers(attempt_id) WHERE is_active = true;
CREATE INDEX idx_user_quiz_answers_question ON user_quiz_answers(question_id) WHERE is_active = true;
CREATE INDEX idx_user_quiz_answers_correct ON user_quiz_answers(is_correct) WHERE is_active = true;

-- Feedback Indexes
CREATE INDEX idx_user_feedback_course ON user_feedback(course_id) WHERE is_active = true;
CREATE INDEX idx_user_feedback_user ON user_feedback(user_id) WHERE is_active = true;
CREATE INDEX idx_user_feedback_rating ON user_feedback(rating) WHERE is_active = true;
CREATE INDEX idx_user_feedback_created ON user_feedback(created_at);

-- ================================
-- COMPOSITE INDEXES FOR COMPLEX QUERIES
-- ================================

-- Dashboard and Analytics
CREATE INDEX idx_user_course_progress_composite ON user_progress(enrolment_id, progress_percentage, last_accessed);
CREATE INDEX idx_active_enrollments_composite ON course_enrolments(user_id, status, enrollment_date) WHERE is_active = true;
CREATE INDEX idx_course_statistics_composite ON course_enrolments(course_id, status, enrollment_date) WHERE is_active = true;
CREATE INDEX idx_subscription_revenue_composite ON payments(created_at, amount, status, payment_type) WHERE is_active = true;
CREATE INDEX idx_user_subscription_active ON subscriptions(user_id, status, current_period_end) WHERE is_active = true;

-- Search and Filtering
CREATE INDEX idx_course_search_composite ON courses USING gin(to_tsvector('english', name || ' ' || COALESCE(description, ''))) WHERE is_active = true;
CREATE INDEX idx_user_search_composite ON users USING gin(to_tsvector('english', name || ' ' || email)) WHERE is_active = true;

-- Performance Critical Queries
CREATE INDEX idx_subscription_billing_composite ON subscriptions(status, auto_renew, next_billing_date) WHERE is_active = true;
CREATE INDEX idx_enrollment_access_composite ON course_enrolments(user_id, course_id, access_type, status) WHERE is_active = true;
CREATE INDEX idx_payment_processing_composite ON payments(status, payment_type, created_at) WHERE is_active = true;