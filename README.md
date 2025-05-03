# RoomyBooking Backend

**RoomyBooking** é um backend robusto e escalável para o gerenciamento de reservas de salas. Implementado com **Java 17+**, **Spring Boot 3.4.5** e Arquitetura Clean/Hexagonal, o projeto oferece autenticação segura, comunicação assíncrona e deploy local com Docker.

## Funcionalidades

- **Autenticação com JWT**: Login e registro de usuários com tokens de acesso e refresh tokens.
- **Autorização com Roles**: Controle de acesso com diferentes permissões (USER, ADMIN).
- **Gerenciamento de Reservas**: Cadastro de salas, regras de reservas e validação de conflitos de horário.
- **Fila de Mensagens Assíncrona**: Envio de e-mails de confirmação com RabbitMQ ou AWS SQS.
- **Auditoria**: Registro de ações relevantes no banco de dados para rastreamento e compliance.
- **Deploy Local com Docker**: Containerização do aplicativo e banco de dados para fácil execução local.

## Tecnologias

- **Java** 17+
- **Spring Boot** 3.4.5
- **Spring Security** (JWT)
- **RabbitMQ / AWS SQS** para mensagens assíncronas
- **MySQL** com JPA
- **Docker** para containerização
- **Swagger** para documentação da API