CREATE TABLE verification_codes (
                                    id SERIAL PRIMARY KEY,
                                    user_cpf VARCHAR(20) NOT NULL,
                                    code VARCHAR(10) NOT NULL,
                                    expiry_date TIMESTAMP NOT NULL
);