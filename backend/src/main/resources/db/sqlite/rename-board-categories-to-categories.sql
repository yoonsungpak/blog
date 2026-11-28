PRAGMA foreign_keys = ON;
BEGIN;

ALTER TABLE board_categories RENAME TO categories;

COMMIT;
