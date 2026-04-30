# 📁 Portfolio Manager

Sistema de Gerenciamento de Portfólio de Projetos desenvolvido com **Spring Boot 3**, **JPA/Hibernate** e **PostgreSQL**.

---

## 🏗️ Arquitetura

```
└── 📁java
    └── 📁com
        └── 📁portfolio
            └── 📁manager
                └── 📁client
                    ├── MemberApiClient.java
                └── 📁config
                    ├── OpenApiConfig.java
                    ├── RestTemplateConfig.java
                    ├── SecurityConfig.java
                    ├── StartupLogger.java
                └── 📁controller
                    ├── MemberController.java
                    ├── ProjectController.java
                └── 📁domain
                    ├── RiskClassifier.java
                └── 📁dto
                    └── 📁request
                        ├── MemberCreateRequest.java
                        ├── ProjectCreateRequest.java
                        ├── ProjectFilterRequest.java
                        ├── ProjectStatusUpdateRequest.java
                        ├── ProjectUpdateRequest.java
                    └── 📁response
                        ├── MemberResponse.java
                        ├── PortfolioReportResponse.java
                        ├── ProjectResponse.java
                └── 📁entity
                    ├── Project.java
                └── 📁enums
                    ├── MemberRole.java
                    ├── ProjectStatus.java
                    ├── RiskClassification.java
                └── 📁exception
                    ├── BusinessException.java
                    ├── ErrorResponse.java
                    ├── GlobalExceptionHandler.java
                    ├── InvalidStatusTransitionException.java
                    ├── MemberServiceException.java
                    ├── ResourceNotFoundException.java
                └── 📁mapper
                    ├── ProjectMapper.java
                └── 📁repository
                    ├── ProjectRepository.java
                └── 📁service
                    └── 📁impl
                        ├── MemberServiceImpl.java
                        ├── ProjectServiceImpl.java
                    ├── MemberService.java
                    ├── ProjectService.java
                └── PortfolioManagerApplication.java
```

---

## 🚀 Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.8+

---

## 📖 Documentação da API

Acesse o Swagger UI após iniciar a aplicação:

> **http://localhost:8080/swagger-ui.html**

---

## 📋 Endpoints Principais

### Projetos

| Método | Endpoint                              | Descrição                      | Role mínima |
| ------ | ------------------------------------- | ------------------------------ | ----------- |
| POST   | `/api/v1/projects`                    | Criar projeto                  | MANAGER     |
| GET    | `/api/v1/projects`                    | Listar com filtros e paginação | VIEWER      |
| GET    | `/api/v1/projects/{id}`               | Buscar por ID                  | VIEWER      |
| PUT    | `/api/v1/projects/{id}`               | Atualizar dados                | MANAGER     |
| PATCH  | `/api/v1/projects/{id}/status`        | Atualizar status               | MANAGER     |
| DELETE | `/api/v1/projects/{id}`               | Excluir projeto                | ADMIN       |
| POST   | `/api/v1/projects/{id}/members/{mid}` | Adicionar membro               | MANAGER     |
| DELETE | `/api/v1/projects/{id}/members/{mid}` | Remover membro                 | ADMIN       |
| GET    | `/api/v1/projects/report/portfolio`   | Relatório do portfólio         | VIEWER      |

### Membros (via API externa mockada)

| Método | Endpoint               | Descrição     | Role mínima |
| ------ | ---------------------- | ------------- | ----------- |
| POST   | `/api/v1/members`      | Criar membro  | MANAGER     |
| GET    | `/api/v1/members`      | Listar todos  | VIEWER      |
| GET    | `/api/v1/members/{id}` | Buscar por ID | VIEWER      |

---

## 🔄 Sequência de Status

```
em análise → análise realizada → análise aprovada → iniciado → planejado → em andamento → encerrado
                                                                   ↑
                                              cancelado (pode ser aplicado a qualquer etapa)
```

- **Não é permitido pular etapas**
- **Cancelado** pode ser aplicado a qualquer status exceto `encerrado` e `cancelado`
- Projetos `iniciado`, `em andamento` e `encerrado` **não podem ser excluídos**

---

## ⚠️ Classificação de Risco (calculada dinamicamente)

| Risco | Orçamento               | Prazo       |
| ----- | ----------------------- | ----------- |
| Baixo | ≤ R$ 100.000            | ≤ 3 meses   |
| Médio | R$ 100.001 – R$ 500.000 | 3 a 6 meses |
| Alto  | > R$ 500.000            | > 6 meses   |

> Qualquer critério que se enquadre em uma categoria superior eleva o risco.

---

## 👥 Regras de Membros

- Membros são criados/consultados via **API externa mockada** (`MemberApiClient`)
- Apenas membros com atribuição `FUNCIONARIO` podem ser alocados em projetos
- Cada projeto: **mínimo 1** e **máximo 10** membros
- Cada membro: no máximo **3 projetos ativos** simultaneamente (excluindo encerrado/cancelado)

---

## 🧪 Testes

```bash
# Rodar todos os testes
mvn test

# Gerar relatório de cobertura JaCoCo
mvn test jacoco:report

# Relatório em: target/site/jacoco/index.html
```

---

## 📦 Dados iniciais (Membros mockados)

| ID  | Nome              | Atribuição  |
| --- | ----------------- | ----------- |
| 1   | Carlos Gerente    | GERENTE     |
| 2   | Ana Funcionária   | FUNCIONARIO |
| 3   | Pedro Funcionário | FUNCIONARIO |
| 4   | Maria Funcionária | FUNCIONARIO |

---

## 🛠️ Stack Tecnológica

- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Data JPA + Hibernate**
- **Spring Security** (Basic Auth, In-Memory)
- **PostgreSQL 15**
- **MapStruct 1.5** (mapeamento de DTOs)
- **Lombok**
- **SpringDoc OpenAPI 2.5** (Swagger UI)
- **JaCoCo** (cobertura de testes)
- **JUnit 5 + Mockito** (testes unitários)
- **H2** (banco em memória para testes)
