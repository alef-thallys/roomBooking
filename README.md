# Sistema de Gerenciamento de Reservas - Spring Boot

Este projeto é um sistema completo para gerenciamento de reservas, com foco em segurança, escalabilidade, arquitetura em
camadas e comunicação assíncrona.  
Desenvolvido com **Spring Boot 3**, integra diversas boas práticas de mercado e simula um ambiente de produção robusto.

---

## Visão Geral

A aplicação gerencia reservas de forma segura, auditável e com limitação inteligente por usuário.  
Também conta com comunicação assíncrona para envio de e-mails, autenticação JWT com refresh tokens e um ambiente
completo com CI/CD, Docker e deploy em nuvem.

**Repositório privado** — disponível mediante solicitação.

---

## Arquitetura

O projeto segue uma estrutura em camadas inspirada na Clean Architecture, com separação clara de responsabilidades:

---

## Segurança

- Autenticação com **JWT**
- **Refresh Tokens**
- Controle de acesso com **Spring Security + `@PreAuthorize`**
- Proteção por roles e permissões
- Endpoints 100% protegidos

---

## Funcionalidades

- Reserva de recursos com checagem de conflitos
- Limitação de **10 horas semanais por usuário**
- Interface RESTful com documentação via Swagger
- Respostas estruturadas e tratamento global de erros

---

## Comunicação Assíncrona

- Integração com **RabbitMQ** para envio de e-mails
- Processamento assíncrono com listeners dedicados
- Estratégia desacoplada de envio de notificações

---

## Auditoria

- Todas as ações relevantes são registradas em tabela `audit_log`
- Registro de IP, usuário, timestamp e tipo de ação
- Visibilidade total de operações críticas

---

## Testes & Qualidade

- **Testes unitários** com JUnit 5 e Mockito
- **Testes de integração** com Testcontainers
- Aplicação dos princípios **SOLID** e **Clean Code**
- Estratégia de **TDD**
- Cobertura monitorada com relatórios

---

## Stack & Ferramentas

- **Java 17**
- **Spring Boot 3**
- **PostgreSQL + JPA (Hibernate)**
- **RabbitMQ**
- **Flyway** para versionamento de banco
- **Docker & Docker Compose**
- **Logback** com logs estruturados
- **Spring Actuator** para métricas

---

## CI/CD & Deploy

- **Dockerfile** e `docker-compose.yml` para ambientes locais e testes
- **GitHub Actions** para build, testes e deploy
- Deploy automatizado em **Railway**
- **PostgreSQL** e variáveis sensíveis gerenciadas pela própria plataforma
- Imagens publicadas em **Docker Hub** (privado)

---

## Documentação

- **Swagger UI** acessível com todos os endpoints REST documentados
- Uso de `@Operation`, `@Schema`, e `@SecurityRequirement` para clareza total
- README técnico detalhado, com visão de arquitetura e decisões de projeto

---

## Sobre o Desenvolvedor

Olá! Me chamo **Alef**, sou estudante de Análise e Desenvolvimento de Sistemas com foco em backend Java.  
Este projeto foi idealizado como um **case técnico completo**, com o objetivo de demonstrar minhas habilidades em:

- Arquitetura limpa e escalável
- Segurança com JWT + RBAC
- Testes e boas práticas
- CI/CD moderno
- Deploy e observabilidade

---

## Contato

Se quiser ver o projeto em ação, colaborar ou marcar uma call técnica, me chama:

- [LinkedIn](https://linkedin.com/in/alef-thallys)
- alef.thallys22@gmail.com

---

> “Código de verdade não é só o que roda, é o que sobrevive à próxima sprint.”
