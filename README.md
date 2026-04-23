# 🐾 PetOS Challenge — Backend API REST

> Plataforma de cuidado contínuo para pets. Backend desenvolvido em Java 17 com Spring Boot 3, seguindo arquitetura em camadas, boas práticas de POO, coesão e desacoplamento.

---

## 📋 Problema que resolve

O PetOS centraliza o histórico de saúde, vacinas, rotinas e alertas dos pets, permitindo que tutores e clínicas veterinárias acompanhem a saúde dos animais de forma longitudinal, com notificações automáticas de vacinas vencidas ou próximas do vencimento.

---

## 🏗️ Arquitetura

```
project/
└── src/main/java/br/com/petos/project/
    ├── config/          → Configurações de Cache e Swagger
    ├── controller/      → Endpoints REST (HTTP layer)
    ├── service/         → Regras de negócio
    ├── repository/      → Acesso a dados (Spring Data JPA)
    ├── entity/          → Entidades JPA mapeadas
    ├── dto/             → Request/Response DTOs
    ├── mapper/          → Conversão Entity ↔ DTO
    ├── enums/           → Enumerações do domínio
    └── exception/       → Tratamento global de erros
```

---

## 🚀 Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 3.4.4 |
| Spring Web | — |
| Spring Data JPA | — |
| Spring Cache | — |
| Bean Validation | — |
| H2 Database | — |
| SpringDoc OpenAPI | 2.8.6 |
| Lombok | 1.18.38 |
| Maven | 3.x |

---

## ▶️ Como rodar

### Pré-requisitos
- Java 17+
- Maven 3.x (ou usar o `mvnw` incluído)

### Rodando localmente

```bash
# Clone o repositório
git clone https://github.com/gugomesx10/PetOS-Java.git
cd PetOS-Java/project

# Execute com Maven Wrapper
./mvnd spring-boot:run        # Linux/Mac
mvnd.cmd spring-boot:run      # Windows
```

A aplicação sobe na porta **8080** com dados de teste já inseridos automaticamente.

---

## 📌 Rotas principais

### 🐶 Pets
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/pets` | Listar pets ativos (paginado) |
| GET | `/pets/{id}` | Buscar pet por ID |
| GET | `/pets/search?name=` | Buscar por nome |
| GET | `/pets/species/{species}` | Filtrar por espécie |
| GET | `/pets/{id}/history` | Histórico consolidado do pet |
| GET | `/pets/vaccines/expiring` | Pets com vacinas vencidas/próximas |
| POST | `/pets` | Cadastrar pet |
| PUT | `/pets/{id}` | Atualizar pet |
| DELETE | `/pets/{id}` | Inativar pet (soft delete) |

### 💉 Vacinas
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/vaccines` | Listar todas (paginado) |
| GET | `/vaccines/{id}` | Buscar por ID |
| GET | `/pets/{petId}/vaccines` | Vacinas do pet |
| GET | `/pets/{petId}/vaccines/pending` | Vacinas pendentes do pet |
| POST | `/vaccines` | Registrar vacina (gera alerta automático) |
| PUT | `/vaccines/{id}` | Atualizar vacina |
| DELETE | `/vaccines/{id}` | Remover vacina |

### 📅 Rotinas
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/routines` | Listar todas (paginado) |
| GET | `/routines/{id}` | Buscar por ID |
| GET | `/pets/{petId}/routines` | Rotinas do pet |
| POST | `/routines` | Registrar rotina |
| PUT | `/routines/{id}` | Atualizar rotina |
| DELETE | `/routines/{id}` | Remover rotina |

### 🔔 Alertas
| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/alerts` | Listar todos (paginado) |
| GET | `/alerts/{id}` | Buscar por ID |
| GET | `/alerts/pending` | Todos os alertas pendentes |
| GET | `/pets/{petId}/alerts` | Alertas do pet |
| GET | `/pets/{petId}/alerts/pending` | Alertas pendentes do pet |
| POST | `/alerts` | Criar alerta manual |
| PUT | `/alerts/{id}` | Atualizar alerta |
| PATCH | `/alerts/{id}/mark-sent` | Marcar alerta como enviado |
| DELETE | `/alerts/{id}` | Remover alerta |

---

## 📖 Swagger / OpenAPI

Após subir a aplicação, acesse:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **API Docs (JSON):** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## 🗄️ H2 Console

Banco em memória disponível para consulta durante o desenvolvimento:

- **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL:** `jdbc:h2:mem:petosdb`
- **Usuário:** `sa`
- **Senha:** *(vazio)*

---

## 🧠 Regras de negócio

- Ao registrar uma vacina com data de vencimento futura, **um alerta é gerado automaticamente**
- O status da vacina é calculado automaticamente: `PENDING`, `APPLIED`, `EXPIRING_SOON`, `OVERDUE`
- Pets são removidos com **soft delete** (campo `active = false`)
- Histórico consolidado retorna vacinas + rotinas + alertas do pet em uma única chamada

---

## 🌿 Git Flow

```
main           → entrega estável / produção
develop        → integração contínua
feature/*      → desenvolvimento de módulos
release/v1.0.0 → preparação para produção
```

Branches utilizadas:
- `feature/pet-crud`
- `feature/vaccine-module`
- `feature/routine-module`
- `feature/alert-module`
- `feature/exception-handler`
- `feature/swagger-config`
- `feature/cache`
- `release/v1.0.0`

---

## 📦 Enums disponíveis

- **Species:** `DOG`, `CAT`, `BIRD`, `RABBIT`, `FISH`, `REPTILE`, `OTHER`
- **VaccineStatus:** `PENDING`, `APPLIED`, `EXPIRING_SOON`, `OVERDUE`
- **RoutineType:** `WALK`, `FEEDING`, `MEDICATION`, `BATHING`, `GROOMING`, `VET_VISIT`, `TRAINING`, `OTHER`
- **AlertType:** `VACCINE_DUE`, `VACCINE_OVERDUE`, `ROUTINE_REMINDER`, `HEALTH_CHECK`, `WEIGHT_CHECK`, `OTHER`

---

## 👨‍💻 Autor

Desenvolvido como desafio técnico PetOS — Java 17 + Spring Boot 3.
