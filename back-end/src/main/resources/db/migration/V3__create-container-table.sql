-- Tabela de containers com todos os campos da entidade Container
CREATE TABLE containers (
                            id BIGSERIAL PRIMARY KEY,
                            container_id VARCHAR(100) UNIQUE NOT NULL,
                            description VARCHAR(500) NOT NULL,
                            user_id BIGINT NOT NULL,
                            operation_id BIGINT NOT NULL,
                            sacks_count INTEGER DEFAULT 0,
                            tare_tons FLOAT DEFAULT 0.0,
                            tare_weight FLOAT DEFAULT 0.0,
                            gross_weight FLOAT DEFAULT 0.0,
                            agency_seal VARCHAR(255),
                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP,
                            updated_by_cpf VARCHAR(15),
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (operation_id) REFERENCES operations(id)
);

-- Tabela para armazenar other_seals (ElementCollection)
CREATE TABLE container_other_seals (
                                       container_id BIGINT NOT NULL,
                                       other_seal VARCHAR(255) NOT NULL,
                                       FOREIGN KEY (container_id) REFERENCES containers(id) ON DELETE CASCADE
);