ALTER TABLE quizzes
DROP CONSTRAINT fk_quizzes_course;

ALTER TABLE quizzes
DROP COLUMN course_id;

ALTER TABLE quizzes
    ADD COLUMN video_id INTEGER NOT NULL;

ALTER TABLE quizzes
    ADD CONSTRAINT fk_quizzes_video
        FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE;
