-- src/main/resources/db/migration/V1__init.sql
CREATE TABLE roles (
  id   BIGSERIAL PRIMARY KEY,
  name VARCHAR(30) UNIQUE NOT NULL
);

CREATE TABLE users (
  id       BIGSERIAL PRIMARY KEY,
  username VARCHAR(60) UNIQUE NOT NULL,
  password VARCHAR(120)        NOT NULL,
  enabled  BOOLEAN DEFAULT TRUE
);

CREATE TABLE user_roles (
  user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
  role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, role_id)
);

CREATE TABLE files (
  id            UUID PRIMARY KEY,
  original_name TEXT        NOT NULL,
  stored_name   TEXT        NOT NULL,
  type          VARCHAR(20),
  status        VARCHAR(20),
  uploaded_at   TIMESTAMP   DEFAULT now(),
  uploader_id   BIGINT REFERENCES users(id)
);
