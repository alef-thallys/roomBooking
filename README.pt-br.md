> 🇬🇧 English version available at [README.md](README.md)

# Aplicativo de Reserva de Salas

Uma **aplicação Spring Boot robusta e pronta para produção** para reservas de salas de forma eficiente. Este projeto expõe um conjunto completo de APIs RESTful para gerenciar usuários, salas e reservas, tudo protegido com autenticação e autorização JWT. A persistência dos dados é feita com MariaDB e as migrações são gerenciadas pelo Flyway, enquanto o RabbitMQ permite notificações assíncronas por e-mail para eventos importantes de reserva.

---

## Índice

- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Primeiros Passos](#primeiros-passos)
    - [Pré-requisitos](#pré-requisitos)
    - [Clonar e Configurar](#clonar-e-configurar)
    - [Configuração de Ambiente](#configuração-de-ambiente)
    - [Build & Execução com Docker Compose](#build--execução-com-docker-compose)
    - [Exportando Variáveis de Ambiente via Bash](#exportando-variáveis-de-ambiente-via-bash)
    - [Execução Local (Desenvolvimento)](#execução-local-desenvolvimento)
- [Documentação da API (Swagger UI)](#documentação-da-api-swagger-ui)
- [Esquema do Banco de Dados](#esquema-do-banco-de-dados)
- [Usuário Administrador Padrão](#usuário-administrador-padrão)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Tratamento de Erros](#tratamento-de-erros)
- [Testes](#testes)
- [Licença](#licença)

---

## Funcionalidades

- **Gestão de Usuários**: CRUD completo com acesso baseado em funções (ADMIN, USER).
- **Gestão de Salas**: Criação, atualização e remoção de salas, especificando capacidade, descrição e local.
- **Gestão de Reservas**: Realize, visualize, atualize e cancele reservas com prevenção automática de conflitos.
- **Autenticação & Autorização**: Endpoints seguros via JWT (tokens de acesso e refresh) e RBAC.
- **Notificações por E-mail**: Confirmações e atualizações de reservas via RabbitMQ.
- **Versionamento do Banco**: Evolução automática do esquema e dados iniciais com migrações Flyway.
- **Documentação Interativa da API**: Swagger UI via SpringDoc OpenAPI.
- **Auditoria**: Rastreamento automático de criação e atualização de entidades.
- **HATEOAS**: APIs REST orientadas a hipermídia.
- **Validação de Entrada**: Validação extensiva com mensagens de erro detalhadas.
- **Tratamento Global de Erros**: Respostas JSON uniformes para erros.

---

## Tecnologias

- **Spring Boot** `3.4.5`
- **Java** `17`
- **Maven**
- **MariaDB**
- **Flyway** (Migrações do banco)
- **Spring Data JPA**
- **Spring Security** (JWT)
- **RabbitMQ** (Mensageria assíncrona)
- **Spring AMQP**, **Spring Mail**
- **Lombok**
- **SpringDoc OpenAPI / Swagger UI**
- **Spring HATEOAS**
- **Docker & Docker Compose**

---

## Primeiros Passos

### Pré-requisitos

- **Java 17+**
- **Maven 3.x**
- **Docker & Docker Compose** (recomendado para facilitar o setup)

---

### Clonar e Configurar

```bash
git clone https://github.com/alef-thallys/roomBooking.git
cd roomBooking
```

---

### Configuração de Ambiente

Todas as variáveis de ambiente necessárias estão listadas em `.env.example`.

Copie o arquivo exemplo e preencha com suas credenciais:

```bash
cp .env.example .env
```

Atualize o `.env` com seus valores:

- Credenciais do banco de dados
- Credenciais de e-mail (`MAIL_USERNAME`, `MAIL_PASSWORD` — ex: senha de aplicativo do Gmail)
- Segredos fortes para `JWT_SECRET` e `JWT_REFRESHSECRET`

**Nunca suba credenciais reais para o controle de versão.**

---

### Build & Execução com Docker Compose

O Docker Compose irá provisionar o MariaDB, RabbitMQ, rodar as migrações Flyway e iniciar a aplicação.

1. **Build da Aplicação**

    ```bash
    mvn clean package -DskipTests
    ```

2. **Iniciar Todos os Serviços**

    ```bash
    docker-compose up --build
    ```

- O app estará disponível em [http://localhost:8080](http://localhost:8080)
- Documentação da API: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

### Exportando Variáveis de Ambiente via Bash (opcional)

Este passo é **apenas para quem deseja rodar a aplicação localmente** (sem Docker). Exportando as variáveis do `.env`, comandos como `mvn spring-boot:run` ou `java -jar ...` usarão sua configuração.

```bash
source .env
```

> **Dica:**
> - Não coloque espaços ao redor do `=`, e coloque entre aspas valores que possuem espaços ou caracteres especiais.
> - Alternativamente, use `export $(grep -v '^#' .env | xargs)`, mas `source .env` é mais robusto para valores entre aspas.
> - Nunca use este comando em produção ou compartilhe seu `.env` com dados sensíveis!

---

### Execução Local (Desenvolvimento)

1. **Inicie o MariaDB & RabbitMQ Manualmente**
    - MariaDB em `3306`, banco `room_booking`
    - RabbitMQ em `5672` (usuário: `guest`/`guest`)

2. **Configure o `src/main/resources/application.yml`** (ou utilize as variáveis de ambiente):

   ```yaml
   spring:
     datasource:
       url: jdbc:mariadb://localhost:3306/room_booking
       username: seu_usuario
       password: sua_senha
     rabbitmq:
       host: localhost
       port: 5672
       username: guest
       password: guest
     mail:
       username: seu_email@gmail.com
       password: senha_app_email

   jwt:
     secret: ${JWT_SECRET:seu_jwt_secret}
     refreshSecret: ${JWT_REFRESHSECRET:seu_jwt_refresh_secret}
   ```

3. **Aplique as Migrações Flyway** (executa no build Maven):

    ```bash
    mvn clean install
    ```

4. **Execute o App:**

    ```bash
    mvn spring-boot:run
    # ou
    java -jar target/roombooking-0.0.1-SNAPSHOT.jar
    ```

---

## Documentação da API (Swagger UI)

- [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## Esquema do Banco de Dados

Esquema gerenciado pelo Flyway. Principais tabelas:

- **users**: nome, email, senha, telefone, papel, campos de auditoria
- **rooms**: nome, descrição, capacidade, local, campos de auditoria
- **reservations**: data início/fim, usuário, sala, campos de auditoria

Dados iniciais carregados via:

- `V2__populate_user_table.sql`
- `V4__populate_room_table.sql`
- `V6__populate_reservation_table.sql`

---

## Usuário Administrador Padrão

Na primeira inicialização, uma conta admin é criada caso não exista:

- **Email:** `admin@admin.com`
- **Senha:** `admin123`
- **Papel:** `ADMIN`

> **Altere essa senha imediatamente em produção!**

---

## Estrutura do Projeto

```
roomBooking/
├── src/main/java/com/github/alefthallys/roombooking/
│   ├── RoomBookingApplication.java      # Ponto de entrada principal
│   ├── annotations/                     # Anotações customizadas
│   ├── assemblers/                      # Assemblers HATEOAS
│   ├── config/                          # Configurações
│   ├── controllers/                     # Controladores REST
│   ├── dtos/                            # Data Transfer Objects
│   ├── exceptions/                      # Exceções customizadas/globais
│   ├── mappers/                         # Mapeadores de entidades/DTOs
│   ├── messaging/                       # Produtores/consumidores RabbitMQ
│   ├── models/                          # Entidades JPA
│   ├── repositories/                    # Repositórios JPA
│   ├── security/                        # Lógica de segurança/JWT
│   ├── services/                        # Lógica de negócio
│   └── validadors/                      # Validadores
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/                    # Scripts de migração Flyway
├── .env.example                         # Exemplo de arquivo de ambiente
└── src/test/java/com/github/alefthallys/roombooking/
    ├── RoomBookingApplicationTests.java
    ├── controllers/
    ├── security/jwt/
    ├── services/
    ├── testBuilders/
    └── testUtils/
```

---

## Tratamento de Erros

Um handler global garante respostas JSON consistentes para erros:

- **400 Bad Request**: Validação ou entrada malformada
- **401 Unauthorized**: Falha na autenticação
- **403 Forbidden**: Acesso negado
- **404 Not Found**: Entidade não encontrada
- **409 Conflict**: Duplicidade/conflitos
- **500 Internal Server Error**: Erros não tratados

Exemplo de resposta:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Formato do corpo da requisição inválido ou conteúdo ausente",
  "path": "/api/v1/users",
  "timestamp": "2023-10-27T10:30:00.000Z",
  "fieldErrors": [
    {
      "field": "email",
      "message": "Formato de e-mail inválido",
      "rejectedValue": "invalid"
    }
  ]
}
```

---

## Testes

Os testes unitários e de integração estão em `src/test/java`. Para rodar:

```bash
mvn test
```

---

## Licença

Licenciado sob a Licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.