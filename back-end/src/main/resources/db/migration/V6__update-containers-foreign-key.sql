-- Corrige a referência de containers para operations
-- Remove a constraint incorreta se existir
ALTER TABLE containers DROP CONSTRAINT IF EXISTS containers_operation_id_fkey;

-- Adiciona a constraint correta
ALTER TABLE containers ADD CONSTRAINT containers_operation_id_fkey
    FOREIGN KEY (operation_id) REFERENCES operations(id) ON DELETE CASCADE;