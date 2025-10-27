CREATE TABLE user_watched_videos (
                                     id SERIAL PRIMARY KEY,
                                     user_id INTEGER NOT NULL,
                                     video_id INTEGER NOT NULL,
                                     created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT fk_watched_user
                                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                     CONSTRAINT fk_watched_video
                                         FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE,
                                     CONSTRAINT unique_user_video UNIQUE(user_id, video_id)
);
