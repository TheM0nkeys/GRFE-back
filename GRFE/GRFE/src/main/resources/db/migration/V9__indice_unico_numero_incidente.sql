DROP INDEX IF EXISTS idx_chamado_numero_incidente;

CREATE UNIQUE INDEX idx_chamado_numero_incidente
    ON chamado (numero_incidente);