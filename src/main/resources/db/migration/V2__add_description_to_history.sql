-- ============================================================
-- Repair Shop — V2: Add description to history tables
-- ============================================================

ALTER TABLE tb_service_order_history ADD COLUMN description VARCHAR(255);
ALTER TABLE tb_execution_history     ADD COLUMN description VARCHAR(255);
