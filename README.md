# API de Livros

## Solução da atividade

Refatoração do [projeto original](https://github.com/gabrielmqc/atividade-refac)
com separação de responsabilidades, políticas extensíveis de desconto, injeção
por construtor e testes de integração. Veja [RELATORIO.md](RELATORIO.md) para
o diagnóstico e a aplicação de cada princípio SOLID.

### Executar

Requer **JDK 21**, com `JAVA_HOME` apontando para ele. O Gradle Wrapper está incluído;
na primeira execução é necessária internet para baixar o Gradle e as dependências.

```powershell
# Windows
.\gradlew.bat test
.\gradlew.bat bootRun
```

```bash
# Linux / macOS
sh gradlew test
sh gradlew bootRun
```

A API fica em `http://localhost:8080/livros`. Para gerar um executável:

```powershell
.\gradlew.bat bootJar
java -jar build/libs/atividade-refac-0.0.1-SNAPSHOT.jar
```

O banco H2 é temporário: os dados desaparecem quando a aplicação é encerrada.
As respostas de erro são 400 para dados inválidos, 404 para livro inexistente
e 409 ao tentar remover um livro raro. As respostas de sucesso mantêm HTTP 200.

### Organização

- `controllers`: endpoints HTTP e tradução de exceções.
- `contracts`: contrato de operações de livros.
- `services`: casos de uso, relatório e simulação de e-mail.
- `discounts`: contrato de desconto, regra percentual e registro das categorias.
- `repositories` e `entities`: persistência JPA e entidade original.
- `src/test`: testes de inicialização e integração da API.

Para acrescentar uma categoria, registre outro bean `PoliticaDesconto`. Use
`DescontoPercentual` para descontos percentuais ou implemente o contrato para
outra fórmula. Cada categoria deve ter uma única política. O serviço descobre
as políticas por injeção, sem alterações no CRUD.

---

O enunciado original está preservado abaixo.

## Objetivo

Esta API foi desenvolvida propositalmente com diversos problemas de arquitetura e violações dos princípios SOLID estudados em aula.

O objetivo da atividade é analisar o código, identificar os problemas existentes e realizar as refatorações necessárias para tornar a aplicação mais organizada, flexível e aderente às boas práticas de desenvolvimento.

Obs. A API foi construída usando Java 21, caso não consigam compilar o código verifiquem o JDK instalado na máquina.
## O que deve ser feito

* Identificar violações dos princípios SOLID presentes no projeto.
* Refatorar a aplicação corrigindo os problemas encontrados.
* Manter o funcionamento da API após as alterações.
* Documentar brevemente quais problemas foram identificados e como foram corrigidos.

## Endpoints

### Listar livros

```http
GET /livros
```

### Cadastrar livro

```http
POST /livros
```

Exemplo de corpo da requisição:

```json
{
  "titulo": "Clean Code",
  "autor": "Robert C. Martin",
  "preco": 120.0,
  "categoria": "TECNOLOGIA"
}
```

### Atualizar livro

```http
PUT /livros/{id}
```

Exemplo:

```http
PUT /livros/1
```

```json
{
  "titulo": "Clean Architecture",
  "autor": "Robert C. Martin",
  "preco": 150.0,
  "categoria": "TECNOLOGIA"
}
```

### Remover livro

```http
DELETE /livros/{id}
```

Exemplo:

```http
DELETE /livros/1
```

## Banco de Dados

O projeto utiliza H2 Database em memória.

Console H2:

```text
http://localhost:8080/h2-console
```

Configurações padrão:

```text
JDBC URL: jdbc:h2:mem:refacdb
User Name: sa
Password:
```

## Entrega

Enviar:

* Link do repositório contendo a solução.
* Documento curto explicando quais princípios SOLID foram identificados e aplicados durante a refatoração.

O foco da atividade não é apenas fazer a API funcionar, mas demonstrar compreensão dos conceitos de arquitetura e orientação a objetos estudados em aula.
