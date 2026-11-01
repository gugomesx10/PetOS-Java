# 🐾 PetOS — Caderneta digital de saúde animal

> **FIAP · Challenge 2026 · Sprint 4 · Java Advanced**  
> Aplicação web desenvolvida com **Java 21, Spring Boot 3, Spring MVC e Thymeleaf**, integrada a uma API REST documentada com OpenAPI.

O PetOS reúne pets, registros de vacinação, rotinas de cuidado e alertas preventivos em uma caderneta digital. Tutores acompanham a saúde dos próprios animais; profissionais com perfil de clínica gerenciam os registros de vacinas.

## 👥 Integrantes — Turma 2TDSPO

| Nome | RM |
| --- | --- |
| Gustavo Gomes Martins | 555999 |
| Pedro dos Anjos | 563832 |
| Matheus de Mattos Vecchi | 561716 |
| Nicholas Albuquerque Buzo | 561082 |
| Nicholas Camillo Canadas de Paula | 561262 |

## 🎯 Problema e solução

Informações de saúde dos pets muitas vezes ficam espalhadas em papéis, mensagens e anotações. Isso dificulta a consulta ao histórico e o acompanhamento de vacinas prestes a vencer ou já vencidas.

O PetOS oferece:

- Cadastro e consulta de pets por tutor.
- Registro, atualização e exclusão de vacinas pelo perfil **CLINICA**.
- Classificação preventiva das doses: pendente, próxima do vencimento, vencida ou aplicada.
- Geração e atualização de alertas **dentro da aplicação**, conforme o estado da vacinação.
- Envio automático de lembretes de vacinação por **e-mail (Gmail SMTP)** aos tutores cadastrados, quando habilitado.
- Registro de rotinas, como consultas veterinárias, e histórico consolidado por animal.
- Autenticação, autorização por perfil e isolamento dos dados entre tutores.

## 🖥️ Funcionalidades demonstráveis

| Área | Fluxo | Perfil |
| --- | --- | --- |
| Acesso | Cadastro, login e logout | TUTOR e CLINICA |
| Pets | Cadastro, busca e visualização da caderneta | TUTOR (seus pets) |
| Vacinação | Cadastro, edição e remoção de doses | CLINICA |
| Vacinação | Consulta de doses e alertas | TUTOR (seus pets) e CLINICA |
| Notificações | Recebimento de lembretes por e-mail para vacinas próximas do vencimento ou atrasadas | TUTOR cadastrado com e-mail válido |
| Histórico | Timeline de vacinas, rotinas e alertas; filtro por categoria | Conforme acesso ao pet |
| Rotinas | Registro de cuidados e consultas | TUTOR (seus pets) |
| API | Operações REST documentadas no Swagger/OpenAPI | Conforme autenticação e perfil |

### Regras preventivas

O sistema utiliza uma janela de **30 dias** para alertar sobre vacinas não aplicadas próximas do vencimento.

| Status | Situação |
| --- | --- |
| `PENDING` | Dose pendente, ainda fora da janela de aviso |
| `EXPIRING_SOON` | Dose não aplicada com vencimento dentro dos próximos 30 dias |
| `OVERDUE` | Dose não aplicada com vencimento ultrapassado |
| `APPLIED` | Dose registrada como aplicada |

As situações são avaliadas durante o cadastro/atualização das vacinas e por uma rotina de atualização **na inicialização da aplicação e diariamente às 00h05, no fuso do servidor**. Quando o status muda, os alertas preventivos são sincronizados com a caderneta.

### Notificações por e-mail

O PetOS utiliza **Spring Mail + Gmail SMTP** para enviar lembretes aos endereços informados pelos tutores no cadastro. O agendamento consulta alertas de vacinas **próximas do vencimento ou vencidas** que ainda não tiveram o e-mail enviado.

- **Horário padrão:** diariamente às **09h00**, fuso `America/Sao_Paulo`.
- **Ativação:** `PETOS_EMAIL_ENABLED=true` (desabilitado por padrão).
- **Destinatários:** e-mail do tutor proprietário do pet; **não existe uma lista fixa** de destinatários autorizados.
- **Controle de reenvio:** a coluna `alerts.email_sent_at` registra o envio para que o mesmo alerta não seja enviado novamente em execuções seguintes.
- **Validação realizada:** cadastro de um tutor, registro de vacina vencida pela clínica e recebimento do lembrete por Gmail.

O envio ocorre no horário do agendamento, **não imediatamente no cadastro da vacina**. Para funcionar, a aplicação precisa permanecer em execução no horário agendado. O envio automático por WhatsApp **não faz parte desta implementação**.

## 🧱 Arquitetura

```text
project/
├── src/main/java/br/com/petos/project/
│   ├── config/          # Configurações, segurança e Swagger
│   ├── controller/      # Controladores da API REST
│   ├── web/             # Controladores MVC das páginas Thymeleaf
│   ├── security/        # Autenticação JWT, sessão e regras de acesso
│   ├── domain/          # Políticas de negócio da vacinação
│   ├── service/         # Serviços e regras de negócio
│   ├── repository/      # Persistência com Spring Data JPA
│   ├── entity/          # Entidades do domínio
│   ├── dto/             # Objetos de entrada e saída da API
│   ├── mapper/          # Conversões entre entidades e DTOs
│   ├── enums/           # Tipos e estados de domínio
│   └── exception/       # Tratamento de erros
├── src/main/resources/
│   ├── templates/       # Interface web
│   ├── static/          # CSS, JavaScript e imagens
│   ├── db/migration/    # Migrações de banco com Flyway
│   └── db/dev/          # Dados de desenvolvimento
└── src/test/            # Testes unitários e de integração
```

As páginas são renderizadas pelo servidor com **Thymeleaf**, sem necessidade de iniciar Node.js ou um frontend separado. A API REST também é fornecida pelo mesmo projeto.

## 🛠️ Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Java 21 | Linguagem principal |
| Spring Boot 3.4.4 / Spring MVC | Aplicação, API REST e páginas |
| Thymeleaf | Renderização da interface |
| Spring Security / JWT | Segurança web e autenticação da API |
| Spring Data JPA / Hibernate | Persistência e consultas |
| Spring Mail / Gmail SMTP | Envio agendado de lembretes de vacinação |
| Flyway | Versionamento do esquema do banco |
| H2 | Desenvolvimento local |
| PostgreSQL | Driver disponível para banco externo |
| Bean Validation | Validação dos dados recebidos |
| SpringDoc OpenAPI 2.8.6 | Documentação interativa da API |
| Maven Wrapper | Build e execução |
| JUnit / Spring Boot Test | Testes automatizados |

## 🚀 Como executar localmente

### Pré-requisitos

- **JDK 21** instalado.
- Acesso à internet para baixar dependências na primeira execução.
- Não é necessário instalar Maven globalmente: o projeto inclui Maven Wrapper.

### Windows (PowerShell)

Na raiz do repositório:

```powershell
Set-Location .\project

# Defina senhas e segredos adequados para o ambiente local.
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:SPRING_DATASOURCE_PASSWORD = 'senha-local'
$env:PETOS_DEV_SEED_PASSWORD = 'senha-dos-usuarios-demo'
$env:PETOS_JWT_SECRET = 'substitua-por-um-segredo-aleatorio-de-no-minimo-32-caracteres'

.\mvnw.cmd -B clean verify
.\mvnw.cmd spring-boot:run
```

No Linux/macOS, execute `./mvnw` no lugar de `.\mvnw.cmd` e defina as variáveis de ambiente conforme seu shell.

Após iniciar:

- **Aplicação:** http://localhost:8080/web
- **Login:** http://localhost:8080/login
- **Cadastro:** http://localhost:8080/cadastro
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI (JSON):** http://localhost:8080/api-docs

Também é possível executar o artefato gerado em `project/target/` com `java -jar target/petos-challenge-1.0.0.jar` (a partir da pasta `project/`, depois do build).

### Configuração

| Variável de ambiente | Função |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo; padrão `dev` |
| `PORT` ou `SERVER_PORT` | Porta HTTP; padrão `8080` |
| `SPRING_DATASOURCE_URL` | URL JDBC; padrão `jdbc:h2:file:./data/petosdb` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco; padrão `sa` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco |
| `PETOS_DEV_SEED_PASSWORD` | Senha inicial das contas de demonstração no perfil `dev` |
| `PETOS_JWT_SECRET` | Segredo utilizado para assinar tokens da API |
| `PETOS_JWT_EXPIRATION_MINUTES` | Validade dos tokens; padrão `120` |
| `PETOS_CORS_ALLOWED_ORIGINS` | Origens autorizadas para acesso à API |
| `PETOS_MAIL_USERNAME` | Conta Gmail usada como remetente |
| `PETOS_MAIL_APP_PASSWORD` | Senha de app do Gmail (não é a senha normal da conta) |
| `PETOS_EMAIL_ENABLED` | Habilita notificações por e-mail; padrão `false` |
| `PETOS_EMAIL_CRON` | Sobrescreve o agendamento; padrão `0 0 9 * * *` (09h00 diárias, horário de São Paulo) |

Para habilitar o Gmail, ative a verificação em duas etapas na conta remetente, gere uma **senha de app** e configure as variáveis de ambiente. **Não inclua credenciais, tokens nem senhas reais no repositório.** As credenciais do remetente não precisam ser informadas pelos tutores.

O banco H2 local utiliza **arquivo** por padrão e pode persistir os dados entre reinicializações. O Flyway aplica as migrações de estrutura (`V1` a `V4`); a `V4__track_vaccine_email_delivery.sql` adiciona `email_sent_at` e o índice dos alertas pendentes de e-mail. O Hibernate valida o esquema. O console H2 (`/h2-console`) está habilitado apenas no perfil `dev` e não deve ser exposto em produção.

No perfil `dev`, a migração de dados de demonstração usa a versão `V900`. Em bases de desenvolvimento criadas antes da V4, o Flyway pode exigir `spring.flyway.out-of-order=true` **somente nesse perfil** para aplicar a V4 sem recriar o banco.

### Contas de demonstração

No perfil `dev`, o inicializador pode criar as contas abaixo, usando a senha definida em `PETOS_DEV_SEED_PASSWORD`:

- `tutor@petos.local` — perfil **TUTOR**.
- `clinica@petos.local` — perfil **CLINICA**.

Essas contas são destinadas **exclusivamente ao ambiente local**. Em banco persistente, alterar a variável de senha não necessariamente altera contas já cadastradas.

## 🔐 Segurança e perfis

- A interface usa autenticação por **sessão**, formulários e proteção **CSRF**.
- A API REST utiliza **JWT Bearer**.
- O perfil **TUTOR** acessa os pets vinculados à sua conta. Tentativas de acessar um pet de outro tutor são bloqueadas no backend.
- O perfil **CLINICA** pode consultar pets ativos e administrar vacinas, conforme a política de acesso.
- O cadastro de pets e de rotinas de cuidado é destinado ao tutor, respeitando a propriedade do animal.
- A exclusão de pets segue o modelo de **soft delete** (`active = false`).

## 🧪 Testes

Execute a partir de `project/`:

```powershell
.\mvnw.cmd -B clean verify
```

A suíte inclui testes de regras de vacinação, persistência, autenticação, autorização, proteção CSRF, isolamento por tutor, fluxo de vacinas, histórico, tratamento de erros e páginas web.

Além da suíte automatizada, a Sprint 4 foi validada manualmente com o fluxo **cadastro de tutor → cadastro do pet → registro de vacina vencida pela clínica → geração de alerta → recebimento de e-mail real**. Após as últimas alterações, a execução local de `clean verify` foi confirmada com sucesso.

### Roteiro rápido para apresentação

1. Entrar como **CLINICA** e registrar uma vacina com vencimento próximo ou ultrapassado.
2. Exibir o status e o alerta preventivo correspondente.
3. Marcar a vacina como aplicada e mostrar a atualização do alerta.
4. Entrar como **TUTOR**, consultar a caderneta e registrar uma rotina.
5. Mostrar o **histórico consolidado** com filtros por categoria.
6. Demonstrar que um segundo tutor **não consegue acessar** pets de outra conta.
7. Abrir o Swagger para apresentar a API e executar `clean verify` para evidenciar os testes.
8. Mostrar o e-mail de lembrete de vacinação recebido pelo tutor cadastrado e explicar o agendamento diário às 09h00.

## 🔌 Principais endpoints REST

| Domínio | Exemplos |
| --- | --- |
| Autenticação | `POST /auth/register`, `POST /auth/login`, `GET /auth/me` |
| Pets | `GET /pets`, `GET /pets/{id}`, `POST /pets`, `PUT /pets/{id}`, `DELETE /pets/{id}` |
| Vacinas | `GET /pets/{petId}/vaccines`, `POST /vaccines`, `PUT /vaccines/{id}`, `DELETE /vaccines/{id}` |
| Rotinas | `GET /pets/{petId}/routines`, `POST /routines`, `PUT /routines/{id}`, `DELETE /routines/{id}` |
| Alertas | `GET /pets/{petId}/alerts`, `GET /alerts/pending`, `PATCH /alerts/{id}/mark-sent` |
| Histórico | `GET /pets/{id}/history` |

Confira parâmetros, contratos, exemplos e autenticação na documentação interativa em `/swagger-ui.html`. A rota `mark-sent` registra um estado de alerta no sistema; ela **não envia mensagens por WhatsApp**.

## 📦 Publicação e evidências

O endereço da implantação online e o vídeo demonstrativo da **Sprint 4** serão incluídos **após a publicação e a gravação**. O vídeo da Sprint 3 foi removido desta documentação. Conforme a orientação da entrega, o vídeo deve ter **até 15 minutos**, com a participação dos integrantes, apresentando o fluxo funcional, as decisões técnicas e a integração multidisciplinar.

Para implantação, configure um **banco persistente**, um perfil sem dados de demonstração e todos os segredos por **variáveis de ambiente**. Não publique senhas, tokens, arquivos de dados locais ou contas de demonstração.

**Atenção ao Render gratuito:** serviços Web gratuitos podem suspender sua execução após períodos de inatividade e usam armazenamento local temporário. Por isso, o agendamento interno das 09h00 **não é garantido nesse plano**, e um H2 armazenado em arquivo pode perder dados. Para uma demonstração online com dados persistentes, utilize um banco externo (por exemplo, PostgreSQL); para entregas pontuais, verifique que o serviço está ativo antes do teste.

## 🌱 Próximos passos

- Opcional: integrar notificações por WhatsApp em uma evolução futura, com provedor e configurações adequados.
- Finalizar a publicação online e anexar o vídeo demonstrativo atualizado.

---

**Projeto acadêmico PetOS — FIAP · Java Advanced · Sprint 4.**
