CREATE TABLE container_images (
    container_id VARCHAR(100) NOT NULL,
    image_key VARCHAR(255) NOT NULL,
    PRIMARY KEY (container_id, image_key),
    FOREIGN KEY (container_id) REFERENCES containers(id)
);