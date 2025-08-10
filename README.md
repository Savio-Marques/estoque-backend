# Controle de Estoque API

![Status do Projeto](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)

## 📝 Descrição

Este projeto é uma API RESTful de controle de estoque, desenvolvida para auxiliar na gestão de um pequeno comércio local. A aplicação permite o gerenciamento de usuários, produtos, categorias e o controle de movimentações no estoque, como entradas e saídas de itens.

O backend está sendo construído com **Java** e o framework **Spring Boot**. O frontend está em fase inicial de desenvolvimento utilizando a biblioteca **React**.

## ✨ Funcionalidades

O sistema oferece as seguintes funcionalidades principais:

* **Autenticação de Usuários:**
    * Cadastro de novos usuários no sistema.
    * Login seguro utilizando autenticação baseada em JSON Web Tokens (JWT).
* **Gerenciamento de Produtos:**
    * Cadastro de novos produtos com informações detalhadas.
    * Edição das informações dos produtos existentes.
* **Gerenciamento de Categorias:**
    * Criação de categorias para organizar os produtos.
    * Associação de produtos a uma ou mais categorias.
* **Controle de Estoque:**
    * Alteração da quantidade de produtos em estoque (entrada e saída).
    * (Futuro) Histórico de movimentações de estoque.

## 🚀 Tecnologias Utilizadas

Este projeto foi desenvolvido utilizando as seguintes tecnologias:

### Backend

* **Java 17+**
* **Spring Boot 3** - Framework principal para a construção da aplicação.
* **Spring Security** - Para a implementação da autenticação e autorização.
* **JSON Web Token (JWT)** - Para a geração de tokens de acesso seguros.
* **PostgreSQL** - Banco de dados relacional para a persistência dos dados.
* **Spring Data JPA** - Para a persistência de dados de forma simplificada.
* **Maven** - Gerenciador de dependências e build do projeto.

### Frontend (Em desenvolvimento)

* **React** - Biblioteca para a construção da interface de usuário.
* **Axios** - Para realizar as requisições HTTP para a API.

## Status do Projeto

O projeto encontra-se na seguinte situação:

* ✅ **Backend:** Quase finalizado. As principais funcionalidades de CRUD e segurança estão implementadas.
* ⏳ **Frontend:** Em fase inicial de desenvolvimento com React.

## Como Executar o Projeto (Backend)

Para executar o backend da aplicação em seu ambiente local, siga os passos abaixo:

### Pré-requisitos

* [Java Development Kit (JDK) 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) ou superior.
* [Apache Maven](https://maven.apache.org/download.cgi) instalado.
* Uma instância do [PostgreSQL](https://www.postgresql.org/download/) em execução.

### Passos

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/Savio-Marques/estoque-backend.git
    ```

2.  **Configure o banco de dados:**
    * Abra o arquivo `src/main/resources/application.properties`.
    * Altere as propriedades `spring.datasource.url`, `spring.datasource.username` e `spring.datasource.password` com as credenciais do seu banco de dados PostgreSQL.

3.  **Instale as dependências e execute a aplicação:**
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

A API estará disponível em `http://localhost:8080`.

## Endpoints da API (Exemplos)

Abaixo estão alguns dos principais endpoints disponíveis na API:

* `POST /auth/register` - Para registrar um novo usuário.
* `POST /auth/login` - Para realizar o login e obter um token JWT.
----------------

* `GET /products` - Para listar todos os produtos (requer autenticação).
* `POST /products` - Para cadastrar um novo produto (requer autenticação).
* `PUT /products/{id}` - Para atualizar um produto existente (requer autenticação).
* `DELETE /products/{id}` - Para deletar um produto existente (requer autenticação).

---------------
* `GET /category` - Para listar todas as categorias (requer autenticação).
* `POST /category` - Para cadastrar uma nova categoria (requer autenticação).
* `PUT /category/{id}` - Para atualizar uma categoria existente (requer autenticação).
* `DELETE /category/{id}` - Para deletar uma categoria existente (requer autenticação).

## ✒️ Autor

* **Sávio Marques* - [Perfil no GitHub](https://github.com/Savio-Marques)
