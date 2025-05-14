CREATE TABLE operations (
    id BIGSERIAL PRIMARY KEY,
    container_id VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (container_id) REFERENCES containers(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
