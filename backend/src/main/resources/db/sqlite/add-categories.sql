PRAGMA foreign_keys = ON;
BEGIN;

CREATE TABLE categories (
    id INTEGER PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    display_order BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE boards ADD COLUMN category_id BIGINT REFERENCES categories(id);
CREATE INDEX idx_boards_category_id ON boards(category_id);

COMMIT;
