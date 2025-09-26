-- Add missing fields to videos table

ALTER TABLE videos ADD COLUMN explanation TEXT;
ALTER TABLE videos ADD COLUMN what_we_will_learn TEXT;
ALTER TABLE videos ADD COLUMN status VARCHAR(50);
ALTER TABLE videos ADD COLUMN prerequisites TEXT;

-- Update thumbnail_url to thumbnail_id as foreign key
ALTER TABLE videos RENAME COLUMN thumbnail_url TO thumbnail_id_old;
ALTER TABLE videos ADD COLUMN thumbnail_id INTEGER;
UPDATE videos SET thumbnail_id = NULL WHERE thumbnail_id_old IS NOT NULL; -- Assuming we can't convert text to int, set to null
ALTER TABLE videos DROP COLUMN thumbnail_id_old;

-- Add foreign key constraint
ALTER TABLE videos ADD CONSTRAINT fk_videos_thumbnail
    FOREIGN KEY (thumbnail_id) REFERENCES attachments(id) ON DELETE SET NULL;