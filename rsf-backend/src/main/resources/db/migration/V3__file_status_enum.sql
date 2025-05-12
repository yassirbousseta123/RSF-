-- add a small lookup so future reports use FK not free text
CREATE TYPE file_status AS ENUM ('QUEUED','PROCESSING','READY','ERROR');

ALTER TABLE files
    ALTER COLUMN status DROP DEFAULT,
    ALTER COLUMN status TYPE file_status USING status::file_status,
    ALTER COLUMN status SET DEFAULT 'QUEUED'; 