

CREATE TABLE courses (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(250) NOT NULL,
                         description VARCHAR(1000),
                         status VARCHAR(250) NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE attachments (
                             id SERIAL PRIMARY KEY ,
                             metadata JSONB NOT NULL,
                             is_active BOOLEAN DEFAULT TRUE,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);

CREATE TABLE videos (
                        id SERIAL PRIMARY KEY,
                        metadata JSONB NOT NULL,
                        is_active BOOLEAN DEFAULT TRUE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE video_attachments (
                                   id SERIAL PRIMARY KEY,
                                   video_id INTEGER NOT NULL REFERENCES videos(id),
                                   attachment_id INTEGER NOT NULL REFERENCES attachments(id),
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE modules (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(250) NOT NULL,
                         description VARCHAR(1000),
                         status VARCHAR(250) NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE module_videos (
                               id SERIAL PRIMARY KEY,
                               module_id INTEGER NOT NULL REFERENCES modules(id),
                               video_id INTEGER NOT NULL REFERENCES videos(id),
                               video_order INTEGER NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               is_active BOOLEAN DEFAULT TRUE
);


CREATE TABLE course_modules (
                                id SERIAL PRIMARY KEY,
                                course_id INTEGER NOT NULL REFERENCES courses(id),
                                module_id INTEGER NOT NULL REFERENCES modules(id),
                                module_order INTEGER NOT NULL,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                is_active BOOLEAN DEFAULT TRUE
);

-- not really necessary right now , but a good idea to have it
-- CREATE TABLE module_attachments (
--     id SERIAL PRIMARY KEY,
--     module_id INTEGER NOT NULL REFERENCES modules(id),
--     attachment_id INTEGER NOT NULL REFERENCES attachments(id),
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     is_active BOOLEAN DEFAULT TRUE
-- );


CREATE TABLE  quizzes (
                          id SERIAL PRIMARY KEY,
                          course_id INTEGER NOT NULL REFERENCES courses(id),
                          name VARCHAR(250) NOT NULL,
                          description VARCHAR(1000),
                          status VARCHAR(250) NOT NULL,
                          number_of_attempts INTEGER DEFAULT 1,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE quiz_questions (
                                id SERIAL PRIMARY KEY,
                                quiz_id INTEGER NOT NULL REFERENCES quizzes(id),
                                question_text TEXT NOT NULL,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE quiz_answers (
                              id SERIAL PRIMARY KEY,
                              question_id INTEGER NOT NULL REFERENCES quiz_questions(id),
                              answer_text TEXT NOT NULL,
                              is_correct BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              is_active BOOLEAN DEFAULT TRUE
);


CREATE TABLE  course_enrolments (
                                    id SERIAL PRIMARY KEY,
                                    course_id INTEGER NOT NULL REFERENCES courses(id),
                                    user_id INTEGER NOT NULL REFERENCES users(id),
                                    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    status VARCHAR(50) DEFAULT 'enrolled', -- e.g., enrolled, completed, dropped
                                    is_active BOOLEAN DEFAULT TRUE,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    completed_at TIMESTAMP NULL

);

CREATE TABLE user_progress (
                               id SERIAL PRIMARY KEY,
                               enrolment_id INTEGER NOT NULL REFERENCES course_enrolments(id),
                               module_id INTEGER NOT NULL REFERENCES modules(id),
                               progress_percentage DECIMAL(5, 2) DEFAULT 0.00, -- e.g., 0.00 to 100.00
                               last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               completed_at TIMESTAMP NULL

);

CREATE TABLE user_quiz_attempts (
                                    id SERIAL PRIMARY KEY,
                                    enrolment_id INTEGER NOT NULL REFERENCES course_enrolments(id),
                                    quiz_id INTEGER NOT NULL REFERENCES quizzes(id),
                                    attempt_number INTEGER NOT NULL,
                                    score DECIMAL(5, 2) DEFAULT 0.00, -- e.g., 0.00 to 100.00
                                    attempt_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    is_active BOOLEAN DEFAULT TRUE,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_quiz_answers (
                                   id SERIAL PRIMARY KEY,
                                   attempt_id INTEGER NOT NULL REFERENCES user_quiz_attempts(id),
                                   question_id INTEGER NOT NULL REFERENCES quiz_questions(id),
                                   answer_id INTEGER NOT NULL REFERENCES quiz_answers(id),
                                   is_correct BOOLEAN DEFAULT FALSE,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   is_active BOOLEAN DEFAULT TRUE
);



CREATE TABLE user_feedback (
                               id SERIAL PRIMARY KEY,
                               course_id INTEGER NOT NULL REFERENCES courses(id),
                               user_id INTEGER NOT NULL REFERENCES users(id),
                               feedback_text TEXT NOT NULL,
                               rating INTEGER CHECK (rating >= 1 AND rating <= 5), -- Rating from 1 to 5
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE course_teachers (
                                 id SERIAL PRIMARY KEY,
                                 course_id INTEGER NOT NULL REFERENCES courses(id),
                                 user_id INTEGER NOT NULL REFERENCES users(id),
                                 role_id INTEGER NOT NULL REFERENCES roles(id), --! i have added these for more flexibility
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 is_active BOOLEAN DEFAULT TRUE
);

-- Update your subscriptions table
CREATE TABLE subscriptions (
                               id SERIAL PRIMARY KEY,
                               user_id INTEGER NOT NULL REFERENCES users(id),
                               course_id INTEGER NOT NULL REFERENCES courses(id),
                               subscription_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               current_period_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               current_period_end TIMESTAMP NOT NULL,
                               status VARCHAR(50) DEFAULT 'active',
                               subscription_type VARCHAR(50) DEFAULT 'monthly',
                               renewal_count INTEGER DEFAULT 0,
                               last_renewal_date TIMESTAMP NULL,
                               auto_renew BOOLEAN DEFAULT TRUE,
                               is_active BOOLEAN DEFAULT TRUE,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payments (
                          id SERIAL PRIMARY KEY,
                          user_id INTEGER NOT NULL REFERENCES users(id),
                          subscription_id INTEGER NOT NULL REFERENCES subscriptions(id),
                          amount DECIMAL(10, 2) NOT NULL,
                          payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          payment_method VARCHAR(50) NOT NULL,
                          status VARCHAR(50) DEFAULT 'completed',
                          is_active BOOLEAN DEFAULT TRUE,
                          state VARCHAR(50) DEFAULT 'pending',
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);