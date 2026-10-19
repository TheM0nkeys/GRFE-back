INSERT INTO usuario (id, login, senha_hash, role)
VALUES (
           nextval('usuario_seq'),
           'admin',
           '$2a$10$8ChNeQGGvv57oj4gGD6xWObcb8NKK0phTHRCpoQp3WQaylkAsGzYK',
           'ADMIN'
       );