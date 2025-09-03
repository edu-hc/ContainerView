-- Tabela de imagens das sacarias
CREATE TABLE sack_images (
                                  id BIGSERIAL PRIMARY KEY,
                                  operation_id BIGINT NOT NULL,
                                  image_key VARCHAR(255) NOT NULL,
                                  FOREIGN KEY (operation_id) REFERENCES operations(id) ON DELETE CASCADE
);