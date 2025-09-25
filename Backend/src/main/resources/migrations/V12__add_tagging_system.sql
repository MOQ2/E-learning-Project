create table if not exists tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    color VARCHAR(7) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);


create table if not exists course_tags (
    course_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    
    CONSTRAINT fk_course_tags_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    CONSTRAINT unique_course_tag 
        UNIQUE(course_id, tag_id)
);

create index if not exists idx_tags_id on tags(id);
create index if not exists idx_tags_name on tags(name);

create index if not exists idx_course_tags_course_id on course_tags(course_id);
create index if not exists idx_course_tags_tag_id on course_tags(tag_id);