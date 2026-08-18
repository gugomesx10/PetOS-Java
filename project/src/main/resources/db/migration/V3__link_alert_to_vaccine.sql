-- =====================================================================
-- PetOS - alerta preventivo vinculado a vacina de origem
--
-- Sem esta coluna nao e possivel distinguir "novo alerta" de
-- "alerta ja existente para a mesma vacina", o que gerava duplicidade.
-- =====================================================================

ALTER TABLE alerts ADD COLUMN vaccine_id BIGINT;

ALTER TABLE alerts ADD CONSTRAINT fk_alerts_vaccine
    FOREIGN KEY (vaccine_id) REFERENCES vaccines (id);

CREATE INDEX idx_alerts_vaccine ON alerts (vaccine_id);

