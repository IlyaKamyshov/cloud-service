CREATE TABLE IF NOT EXISTS files
(
    id           SERIAL PRIMARY KEY,
    file_name    VARCHAR(255)                     NOT NULL,
    file_hash    VARCHAR(255)                     NOT NULL,
    file_size    BIGINT                           NOT NULL,
    created_date TIMESTAMP WITH SYSTEM VERSIONING NOT NULL,
    file_owner   VARCHAR(255)                     NOT NULL,
    FOREIGN KEY (file_owner) REFERENCES cloudservice.users (login)
);