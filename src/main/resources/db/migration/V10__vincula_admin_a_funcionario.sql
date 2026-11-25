
INSERT INTO funcionario (id, nome, matricula, email)
VALUES (nextval('funcionario_seq'), 'Administrador do Sistema', 'ADMIN', NULL);

UPDATE usuario
SET funcionario_id = (SELECT id FROM funcionario WHERE matricula = 'ADMIN')
WHERE login = 'admin'
AND funcionario_id IS NULL;