CREATE INDEX idx_departamento_divisao
    ON departamento (divisao_id);

CREATE INDEX idx_especialidades_divisao
    ON especialidades (divisao_id);

CREATE INDEX idx_especialidades_departamento
    ON especialidades (departamento_id);

CREATE INDEX idx_funcionario_especialidades_funcionario
    ON funcionario_especialidades (funcionario_id);

CREATE INDEX idx_funcionario_especialidades_especialidade
    ON funcionario_especialidades (especialidade_id);

CREATE INDEX idx_escala_funcionario
    ON escala (funcionario_id);

CREATE INDEX idx_escala_especialidade
    ON escala (especialidade_id);

CREATE INDEX idx_chamado_especialidade
    ON chamado (especialidade_id);

CREATE INDEX idx_chamado_plantonista
    ON chamado (plantonista_id);

CREATE INDEX idx_chamado_responsavel
    ON chamado (usuario_responsavel_id);

CREATE INDEX idx_chamado_status
    ON chamado (status);

CREATE INDEX idx_historico_chamado
    ON historico_acionamento (chamado_id);

CREATE INDEX idx_historico_autor
    ON historico_acionamento (autor_id);