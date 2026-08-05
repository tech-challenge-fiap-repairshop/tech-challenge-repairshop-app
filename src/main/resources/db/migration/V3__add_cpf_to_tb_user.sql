-- ============================================================
-- Repair Shop — V3: Add cpf column to tb_user
-- ============================================================

ALTER TABLE tb_user ADD COLUMN IF NOT EXISTS cpf VARCHAR(14) UNIQUE;
CREATE INDEX IF NOT EXISTS idx_user_cpf ON tb_user(cpf);
