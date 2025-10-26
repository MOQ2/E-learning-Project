-- we need to add a Category (single string) to the courses table replace the thumbnail column with an attachment id to the attachments table
-- we need to add explanation column , key takeaways and a prerequisites column to the courses table


ALTER TABLE courses
    DROP COLUMN IF EXISTS thumbnail_url,
    DROP COLUMN IF EXISTS preview_video_url,
    ADD COLUMN thumbnail INTEGER,
    Add COLUMN category VARCHAR(255),
    ADD CONSTRAINT fk_courses_thumbnail
        FOREIGN KEY (thumbnail) REFERENCES attachments(id) ON DELETE SET NULL;