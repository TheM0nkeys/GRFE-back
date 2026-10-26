# GRFE - Gestão de Chamados Itaipu

Sistema de gestão de chamados, equipes e escalas desenvolvido em Spring Boot.

## Tecnologias

- Java 17
- Spring Boot 4.1.1
- Spring Data JPA / Hibernate
- PostgreSQL

## Banco de dados

Crie o banco `grfe` no PostgreSQL antes de rodar a aplicação:

```sql
CREATE DATABASE grfe;
```

Depois execute o script abaixo para criar as tabelas (ou rode via `psql -U postgres -d grfe -f sql/schema.sql`):

```sql
-- ============================================================
-- GRFE - Script de criação do schema (PostgreSQL)
-- ============================================================

CREATE TABLE divisao (
    id     BIGSERIAL PRIMARY KEY,
    nome   VARCHAR(255) NOT NULL
);

CREATE TABLE departamento (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(255) NOT NULL,
    divisao_id  BIGINT NOT NULL,
    CONSTRAINT fk_departamento_divisao
        FOREIGN KEY (divisao_id) REFERENCES divisao (id)
);

CREATE TABLE especialidades (
    id               BIGSERIAL PRIMARY KEY,
    nome             VARCHAR(255) NOT NULL,
    descricao        VARCHAR(255),
    divisao_id       BIGINT NOT NULL,
    departamento_id  BIGINT NOT NULL,
    CONSTRAINT fk_especialidades_divisao
        FOREIGN KEY (divisao_id) REFERENCES divisao (id),
    CONSTRAINT fk_especialidades_departamento
        FOREIGN KEY (departamento_id) REFERENCES departamento (id)
);

CREATE TABLE funcionario (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(255) NOT NULL,
    matricula  VARCHAR(255) NOT NULL,
    email      VARCHAR(255),
    CONSTRAINT uq_funcionario_matricula UNIQUE (matricula)
);

CREATE TABLE funcionario_especialidades (
    funcionario_id    BIGINT NOT NULL,
    especialidade_id  BIGINT NOT NULL,
    PRIMARY KEY (funcionario_id, especialidade_id),
    CONSTRAINT fk_fe_funcionario
        FOREIGN KEY (funcionario_id) REFERENCES funcionario (id),
    CONSTRAINT fk_fe_especialidade
        FOREIGN KEY (especialidade_id) REFERENCES especialidades (id)
);

CREATE TABLE escala (
    id                BIGSERIAL PRIMARY KEY,
    data_hora_inicio  TIMESTAMP NOT NULL,
    data_hora_fim     TIMESTAMP NOT NULL,
    especialidade_id  BIGINT NOT NULL,
    funcionario_id    BIGINT NOT NULL,
    CONSTRAINT fk_escala_especialidade
        FOREIGN KEY (especialidade_id) REFERENCES especialidades (id),
    CONSTRAINT fk_escala_funcionario
        FOREIGN KEY (funcionario_id) REFERENCES funcionario (id)
);

CREATE TABLE chamado (
    id                      BIGSERIAL PRIMARY KEY,
    data_hora_acionamento   TIMESTAMP NOT NULL,
    especialidade_id        BIGINT NOT NULL,
    plantonista_id          BIGINT NOT NULL,
    usuario_responsavel_id  BIGINT NOT NULL,
    motivo                  VARCHAR(255) NOT NULL,
    numero_incidente        VARCHAR(255),
    status                  VARCHAR(20) NOT NULL,
    CONSTRAINT fk_chamado_especialidade
        FOREIGN KEY (especialidade_id) REFERENCES especialidades (id),
    CONSTRAINT fk_chamado_plantonista
        FOREIGN KEY (plantonista_id) REFERENCES funcionario (id),
    CONSTRAINT fk_chamado_responsavel
        FOREIGN KEY (usuario_responsavel_id) REFERENCES funcionario (id),
    CONSTRAINT ck_chamado_status
        CHECK (status IN ('ABERTO', 'EM_ANDAMENTO', 'FECHADO'))
);

CREATE TABLE historico_acionamento (
    id           BIGSERIAL PRIMARY KEY,
    data_hora    TIMESTAMP NOT NULL,
    autor_id     BIGINT NOT NULL,
    comentario   VARCHAR(2000) NOT NULL,
    chamado_id   BIGINT NOT NULL,
    CONSTRAINT fk_historico_autor
        FOREIGN KEY (autor_id) REFERENCES funcionario (id),
    CONSTRAINT fk_historico_chamado
        FOREIGN KEY (chamado_id) REFERENCES chamado (id)
);

-- Índices auxiliares
CREATE INDEX idx_chamado_status ON chamado (status);
CREATE INDEX idx_chamado_especialidade ON chamado (especialidade_id);
CREATE INDEX idx_escala_funcionario ON escala (funcionario_id);
CREATE INDEX idx_historico_chamado ON historico_acionamento (chamado_id);
```

### Modelo de dados

| Tabela | Descrição |
|---|---|
| `divisao` | Divisões organizacionais |
| `departamento` | Departamentos, vinculados a uma divisão |
| `especialidades` | Especialidades técnicas, vinculadas a divisão e departamento |
| `funcionario` | Funcionários, com N:N para especialidades |
| `funcionario_especialidades` | Tabela associativa funcionário ↔ especialidade |
| `escala` | Escalas de plantão por funcionário e especialidade |
| `chamado` | Chamados abertos, com plantonista e responsável |
| `historico_acionamento` | Histórico de interações em um chamado |

## Configuração

Configure o `application.properties` com os dados do seu banco:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/grfe
spring.datasource.username=postgres
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=validate
```

## Como rodar

```
mvn spring-boot:run
```