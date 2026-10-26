# GRFE - Gestão de Plantões e Acionamentos

API para gerenciar divisões, departamentos, especialidades, funcionários, escalas, chamados e histórico de acionamentos.

## Pré-requisitos

- Java 17
- Maven instalado e disponível no `PATH` (`mvn -version` para verificar)
- PostgreSQL em execução

Este repositório não inclui Maven Wrapper. Os comandos abaixo devem ser executados na pasta que contém este README e o `pom.xml`.

## Banco de dados

Crie apenas o banco vazio no PostgreSQL:

```sql
CREATE DATABASE grfe;
```

Ao iniciar, o Flyway cria e atualiza as tabelas usando as migrações em `src/main/resources/db/migration`. Não execute um script manual de criação do schema.

Por padrão, a aplicação usa `jdbc:postgresql://localhost:5432/grfe`, usuário `postgres` e a senha configurada em `src/main/resources/application.properties`. Para não depender dos valores locais do arquivo, defina as variáveis de ambiente antes de iniciar. Exemplo no PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/grfe"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "sua_senha_local"
$env:JWT_SECRET = "substitua-por-uma-chave-secreta-com-mais-de-32-caracteres"
```

Use um segredo JWT próprio e não compartilhe credenciais. Para integrar com o ServiceNow, configure também:

```powershell
$env:SERVICENOW_USERNAME = "seu_usuario"
$env:SERVICENOW_PASSWORD = "sua_senha"
```

Essas variáveis ficam disponíveis apenas na sessão atual do PowerShell. A migração `V8__usuario_admin.sql` cria o login `admin`, mas contém somente o hash da senha; a senha em texto não está documentada no projeto.

## Executar

Com o PostgreSQL ativo e as variáveis configuradas, inicie a API:

```powershell
mvn spring-boot:run
```

Por padrão, a aplicação fica disponível em `http://localhost:8080`. A interface Swagger pode ser acessada em `http://localhost:8080/swagger-ui.html`; o documento OpenAPI fica em `http://localhost:8080/v3/api-docs`.

## Testes

Execute toda a suíte com:

```powershell
mvn test
```

Os testes usam o perfil `test` e um banco H2 em memória; não precisam do PostgreSQL local. Para executar uma classe específica:

```powershell
mvn "-Dtest=ChamadoServiceTest" test
```

## Tecnologias

- Java 17 e Spring Boot 4.1.1
- Spring Web, Spring Security e Spring Data JPA
- PostgreSQL e Flyway
- JWT e OpenFeign para integração com o ServiceNow
- H2 para testes