CREATE TABLE tasks (
    id          CHAR(36)     NOT NULL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL
);

CREATE INDEX idx_tasks_title ON tasks (title);
