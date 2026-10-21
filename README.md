## Objetivo

API REST para gerenciamento de chamados técnicos, permitindo abrir chamados, listar os chamados de um usuário, consultar detalhes por identificador e registrar a resolução por um técnico. Os dados são persistidos no MongoDB e o acesso é protegido por autenticação.

## Endpoints

| Método | Endpoint | Funcionalidade |
| --- | --- | --- |
| `POST` | `/api/chamados` | Abre um chamado com `titulo` e `descricao`. O usuário autenticado é registrado como solicitante e o chamado inicia com situação `ABERTO`. |
| `GET` | `/api/chamados/usuario/{id}` | Lista os chamados do usuário informado, ordenados pela data de abertura. O próprio usuário, técnicos e administradores podem consultar. |
| `GET` | `/api/chamados/{id}` | Consulta os detalhes de um chamado. Permitido ao solicitante, ao técnico responsável e a administradores. |
| `PUT` | `/api/chamados/{id}/resolver` | Registra a `solucao`, o técnico responsável e a data de resolução, alterando a situação para `RESOLVIDO`. Permitido a técnicos e administradores; chamados já resolvidos não podem ser alterados. |

## Stack

- Java 21
- Spring Boot 3.4.0
- Spring Web (API REST)
- Spring Data MongoDB
- Spring Security (HTTP Basic e autenticação de usuários)
- MongoDB Atlas
- Thymeleaf
- Jakarta Validation
- Lombok
- Maven
- JUnit 5, Mockito e Spring Boot Test
