-- Tabela de imagens dos containers
CREATE TABLE container_images (
                        id BIGSERIAL PRIMARY KEY,
                        container_id_def BIGINT NOT NULL,
                        image_key VARCHAR(255) NOT NULL,
                        category VARCHAR(50) NOT NULL,
                        FOREIGN KEY (container_id_def) REFERENCES containers(id) ON DELETE CASCADE,
                        CONSTRAINT chk_image_category CHECK (category IN (
                                                                          'VAZIO_FORRADO', 'FIADA', 'CHEIO_ABERTO', 'MEIA_PORTA',
                                                                          'LACRADO_FECHADO', 'LACRES_PRINCIPAIS', 'LACRES_OUTROS'
                            ))
);