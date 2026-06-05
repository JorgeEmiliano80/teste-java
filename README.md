# Portfolio API

API REST desenvolvida em Java com Spring Boot para gerenciamento de portfólio de projetos, incluindo ciclo de vida do projeto, equipe alocada, orçamento, classificação dinâmica de risco, controle de status e relatório consolidado do portfólio.

## Tecnologias utilizadas

* Java 21
* Spring Boot 3.5.x
* Maven / Maven Wrapper
* PostgreSQL
* Spring Web
* Spring Data JPA
* Hibernate
* Flyway
* Spring Validation
* Spring Security
* Springdoc OpenAPI / Swagger
* JUnit 5
* Mockito
* JaCoCo
* Docker Compose

## Arquitetura

O projeto segue uma arquitetura em camadas baseada em MVC:

```text
controller -> service -> repository -> database
                |
             domain
                |
             mapper / dto
```

Principais responsabilidades:

* `controller`: expõe os endpoints REST.
* `service`: concentra regras de negócio e transações.
* `repository`: acesso a dados via Spring Data JPA.
* `domain`: regras puras e testáveis, como cálculo de risco e política de transição de status.
* `dto`: objetos de entrada e saída da API.
* `mapper`: conversão entre entidades e DTOs.
* `exception`: tratamento global de erros.
* `config`: segurança e documentação OpenAPI.

## Regras de negócio implementadas

### Projetos

O sistema permite criar, consultar, atualizar, listar e excluir projetos com os seguintes campos:

* Nome
* Data de início
* Previsão de término
* Data real de término
* Orçamento total
* Descrição
* Gerente responsável
* Status atual
* Membros alocados
* Classificação dinâmica de risco

### Classificação de risco

A classificação de risco é calculada dinamicamente, sem persistir o valor no banco:

* `BAIXO`: orçamento até R$ 100.000 e prazo até 3 meses.
* `MEDIO`: orçamento entre R$ 100.001 e R$ 500.000 ou prazo entre 3 e 6 meses.
* `ALTO`: orçamento acima de R$ 500.000 ou prazo superior a 6 meses.

### Status do projeto

Os status são fixos e seguem a sequência:

```text
EM_ANALISE -> ANALISE_REALIZADA -> ANALISE_APROVADA -> INICIADO -> PLANEJADO -> EM_ANDAMENTO -> ENCERRADO
```

O status `CANCELADO` pode ser aplicado a qualquer momento.

Não é permitido pular etapas na transição de status.

### Exclusão de projetos

Projetos não podem ser excluídos quando estiverem nos status:

* `INICIADO`
* `EM_ANDAMENTO`
* `ENCERRADO`

### Membros

O cadastro de membros é disponibilizado por uma API REST externa mockada:

```text
/api/external/members
```

Cada membro possui:

* Nome
* Atribuição

Apenas membros com atribuição `FUNCIONARIO` podem ser associados a projetos.

### Alocação de membros

Regras de alocação:

* Cada projeto deve ter no mínimo 1 membro alocado.
* Cada projeto pode ter no máximo 10 membros alocados.
* Um membro não pode estar alocado em mais de 3 projetos ativos.
* Projetos com status `ENCERRADO` ou `CANCELADO` não contam como projetos ativos para essa regra.

### Relatório de portfólio

A API possui um endpoint de relatório consolidado contendo:

* Quantidade de projetos por status.
* Total orçado por status.
* Total orçado geral.
* Média de duração dos projetos encerrados.
* Total de membros únicos alocados.

Endpoint:

```http
GET /api/reports/portfolio-summary
```

## Segurança

A aplicação utiliza Spring Security com autenticação básica em memória.

Credenciais padrão:

```text
Usuário: admin
Senha: admin123
```

Swagger e OpenAPI estão liberados para facilitar a avaliação técnica.

## Como executar o projeto

### Pré-requisitos

* Java 21
* Docker
* Docker Compose

### Subir PostgreSQL

```bash
docker compose up -d
```

O PostgreSQL é exposto localmente na porta `5433` para evitar conflito com instalações locais na porta padrão `5432`.

Configuração utilizada:

```text
Database: teste_java
Username: postgres
Password: postgres
Host: localhost
Port: 5433
```

### Executar a aplicação

```bash
./mvnw spring-boot:run
```

A aplicação será iniciada em:

```text
http://localhost:8080
```

## Swagger / OpenAPI

A documentação interativa da API está disponível em:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Executar testes

```bash
./mvnw clean test
```

## Cobertura de testes

A cobertura é gerada automaticamente pelo JaCoCo ao executar os testes.

Relatório HTML:

```text
target/site/jacoco/index.html
```

Última validação local:

```text
Tests run: 83
Failures: 0
Errors: 0
Skipped: 0
```

Cobertura global:

```text
Instructions: 84.31%
Branches:     75.00%
Lines:        82.02%
```

Cobertura das regras de negócio:

```text
Instructions: 89.59%
Branches:     78.38%
Lines:        90.05%
```

## Endpoints principais

### Membros

```http
POST /api/external/members
GET  /api/external/members/{id}
```

Exemplo de criação de gerente:

```json
{
  "name": "Carla Mendes",
  "assignment": "GERENTE"
}
```

Exemplo de criação de funcionário:

```json
{
  "name": "João Silva",
  "assignment": "FUNCIONARIO"
}
```

### Projetos

```http
POST   /api/projects
GET    /api/projects/{id}
GET    /api/projects
PUT    /api/projects/{id}
DELETE /api/projects/{id}
PATCH  /api/projects/{id}/status
POST   /api/projects/{projectId}/members/{memberId}
DELETE /api/projects/{projectId}/members/{memberId}
```

Exemplo de criação de projeto:

```json
{
  "name": "Data Platform Modernization",
  "startDate": "2026-01-01",
  "expectedEndDate": "2026-04-01",
  "totalBudget": 100000.00,
  "description": "Projeto para modernização da plataforma de dados.",
  "managerId": 1,
  "memberIds": [2]
}
```

Exemplo de alteração de status:

```json
{
  "status": "ANALISE_REALIZADA"
}
```

### Relatório

```http
GET /api/reports/portfolio-summary
```

## Paginação e filtros

A listagem de projetos aceita filtros e paginação:

```http
GET /api/projects
```

Parâmetros disponíveis:

```text
name
status
managerId
startDateFrom
startDateTo
minBudget
maxBudget
page
size
sortBy
sortDirection
```

Exemplo:

```bash
curl -u admin:admin123 \
  "http://localhost:8080/api/projects?page=0&size=10&sortBy=id&sortDirection=ASC"
```

## Exemplos com curl

### Criar gerente

```bash
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/external/members \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Carla Mendes",
    "assignment": "GERENTE"
  }'
```

### Criar funcionário

```bash
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/external/members \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "assignment": "FUNCIONARIO"
  }'
```

### Criar projeto

```bash
curl -u admin:admin123 \
  -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Data Platform Modernization",
    "startDate": "2026-01-01",
    "expectedEndDate": "2026-04-01",
    "totalBudget": 100000.00,
    "description": "Projeto para modernização da plataforma de dados.",
    "managerId": 1,
    "memberIds": [2]
  }'
```

### Listar projetos

```bash
curl -u admin:admin123 \
  "http://localhost:8080/api/projects?page=0&size=10&sortBy=id&sortDirection=ASC"
```

### Consultar relatório

```bash
curl -u admin:admin123 \
  http://localhost:8080/api/reports/portfolio-summary
```

## Banco de dados e migrations

O projeto utiliza Flyway para versionamento do banco de dados.

Migration inicial:

```text
src/main/resources/db/migration/V1__create_initial_tables.sql
```

O Hibernate está configurado com:

```text
ddl-auto: validate
```

Assim, o schema é criado pelo Flyway e validado pelo Hibernate ao iniciar a aplicação.

## Tratamento de erros

A API possui tratamento global de exceções com respostas padronizadas para:

* Erros de regra de negócio.
* Recursos não encontrados.
* Erros de validação.
* Violações de constraints.
* Erros inesperados.

Exemplo de erro:

```json
{
  "timestamp": "2026-06-05T22:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "A transição de status deve respeitar a sequência lógica.",
  "path": "/api/projects/1/status",
  "fieldErrors": {}
}
```

## Validação final

Antes da entrega, foram executadas as seguintes validações:

```bash
./mvnw clean test
```

```bash
grep -RIn "palavra restrita" . --exclude-dir=.git --exclude-dir=target
```

Resultado:

```text
Build executado com sucesso.
Testes passando.
Cobertura acima do mínimo solicitado.
Nenhuma ocorrência indevida encontrada no projeto.
```

## Observações técnicas

* A lógica crítica foi isolada em classes de domínio para facilitar testes unitários.
* A classificação de risco é derivada dinamicamente a partir de orçamento e prazo.
* As transições de status são validadas por uma política própria.
* A exclusão de projetos é protegida por regra de domínio.
* O relatório de portfólio é calculado na camada de serviço.
* Controllers não possuem regra de negócio.
* DTOs são utilizados para entrada e saída da API.
* Mappers centralizam a conversão entre entidades e respostas.
