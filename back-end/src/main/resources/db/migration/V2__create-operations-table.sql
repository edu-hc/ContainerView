-- Tabela de operações com todos os campos da entidade Operation
CREATE TABLE operations (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            ctv VARCHAR(255) NOT NULL,
                            exporter VARCHAR(255) NOT NULL,
                            ship VARCHAR(255) NOT NULL,
                            terminal VARCHAR(255) NOT NULL,
                            deadline_draft DATE NOT NULL,
                            destination VARCHAR(255) NOT NULL,
                            arrival_date DATE NOT NULL,
                            reservation VARCHAR(255) NOT NULL,
                            ref_client VARCHAR(255) NOT NULL,
                            load_deadline VARCHAR(255) NOT NULL,
                            status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP,
                            updated_by_cpf VARCHAR(15),
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            CONSTRAINT chk_operation_status CHECK (status IN ('OPEN', 'COMPLETED'))
);