-- Adiciona coluna para armazenar o secret TOTP
ALTER TABLE users ADD COLUMN totp_secret VARCHAR(64);

-- Remove a tabela de códigos de verificação por email (não é mais necessária)
DROP TABLE IF EXISTS verification_codes;

-- Índice para busca eficiente (opcional, mas recomendado)
CREATE INDEX idx_users_totp_secret ON users(totp_secret);