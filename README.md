# Container View - Backend

## Descrição do Projeto

O Container View é um aplicativo desenvolvido para a empresa Arimava, focado em armazenar e organizar fotos de operações de estufagem de containers. O sistema permite captura e upload de fotos, organização por número de operação, data e container, além de facilitar a geração de relatórios e auditoria das operações.

## Tecnologias Utilizadas

- **Spring Boot 3.4.3**: Framework base para o desenvolvimento do backend
- **Spring Security**: Implementação de autenticação e autorização
- **Spring Data JPA**: Camada de persistência
- **PostgreSQL**: Banco de dados principal para produção
- **H2**: Banco de dados em memória para testes
- **JWT**: Tokens para autenticação
- **Autenticação de Dois Fatores**: Segurança adicional através de códigos enviados por email
- **Amazon S3**: Armazenamento de imagens na nuvem
- **Flyway**: Migração e controle de versão do banco de dados
- **Lombok**: Redução de código boilerplate
- **Maven**: Gerenciamento de dependências

## Configuração e Execução

### Pré-requisitos

- Java 17 ou superior
- Maven
- PostgreSQL
- Conta AWS para o S3 (para armazenamento de imagens)
- SMTP para envio de emails (autenticação de dois fatores)

### Execução do Projeto

1. Clone o repositório
2. Configure as variáveis de ambiente
3. Execute no terminal:

```bash
mvn clean install
mvn spring-boot:run
```

Para execução em ambiente de teste:

```bash
mvn spring-boot:run -Dspring.profiles.active=test
```

## Estrutura do Projeto

```
src/main/java/com/ftc/containerView/
├── controller/          # Controladores REST
├── model/               # Entidades e DTOs
│   ├── auth/            # Classes para autenticação
│   ├── container/       # Classes para containers
│   ├── operation/       # Classes para operações
│   └── user/            # Classes para usuários
├── infra/               # Infraestrutura
│   ├── aws/             # Configuração AWS S3
│   └── security/        # Configuração de segurança
├── repositories/        # Interfaces de repositório
├── service/             # Camada de serviço
└── ContainerViewApplication.java
```

## Principais Funcionalidades

1. **Autenticação e Segurança**
    - Login com autenticação de dois fatores
    - Geração e validação de JWT
    - Controle de acesso baseado em perfis (ADMIN, GERENTE, INSPETOR)

2. **Gerenciamento de Containers**
    - Cadastro e consulta de containers
    - Upload e armazenamento de imagens no Amazon S3
    - Organização das imagens por container

3. **Operações**
    - Registro de operações vinculadas a containers e usuários
    - Controle de data e hora das operações
    - Associação de imagens às operações

4. **Usuários**
    - Cadastro e gerenciamento de usuários
    - Diferentes perfis de acesso

## Endpoints da API

### Autenticação
- `POST /auth/login`: Login de usuário
- `POST /auth/verify`: Verificação de código 2FA
- `POST /auth/register`: Registro de novo usuário (somente ADMIN)

### Containers
- `GET /containers`: Lista todos os containers
- `GET /containers/{id}`: Obtém container por ID
- `DELETE /containers/{id}`: Remove container

### Operações
- `GET /operations`: Lista todas as operações
- `GET /operations/{id}`: Obtém operação por ID
- `POST /operations`: Cria nova operação (multipart/form-data para upload de imagens)
- `DELETE /operations/{id}`: Remove operação

### Usuários
- `GET /users`: Lista todos os usuários
- `GET /users/{id}`: Obtém usuário por ID
- `PUT /users/{id}`: Atualiza usuário
- `DELETE /users/{id}`: Remove usuário

## Banco de Dados

O projeto utiliza Flyway para migração de banco de dados. As tabelas principais são:

- `users`: Armazena informações dos usuários
- `containers`: Armazena informações dos containers
- `container_images`: Armazena links para as imagens de cada container
- `operations`: Registra operações vinculando containers e usuários
- `verification_codes`: Armazena códigos temporários para autenticação de dois fatores

## Logs

O sistema utiliza um esquema de logs estruturado com três appenders principais:
- Console: Exibe logs no terminal
- Arquivo de log geral: `logs/containerView.log`
- Arquivo de log de erros: `logs/error.log`

Níveis de log configurados para diferentes componentes do sistema.

## Time de Desenvolvimento

Este projeto está sendo desenvolvido por estudantes do 5° ciclo do curso de Análise e Desenvolvimento de Sistemas na FATEC, com a orientação de professores.

- Olavo B. - Líder do Projeto
- Alan M. - Desenvolvedor Front-end
- Eduardo C. - Desenvolvedor Back-end

---

*Projeto desenvolvido para a empresa Arimava - 2025*