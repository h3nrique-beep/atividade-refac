# Refatoração da API de Livros — SOLID

## Objetivo e diagnóstico

O projeto original concentrava no `LivroController` o atendimento HTTP, a validação,
os descontos, a persistência, o relatório e a simulação de e-mail. A interface
`LivroOperations` misturava CRUD com PDF, ERP, etiquetas e outras responsabilidades.
Três dessas operações lançavam `UnsupportedOperationException`.

## Princípios identificados e aplicados

| Princípio | Problema identificado | Solução aplicada |
| --- | --- | --- |
| **SRP — Responsabilidade única** | O controller tinha vários motivos para mudar: contrato HTTP, regras de negócio, relatório e e-mail. | `LivroController` apenas recebe e encaminha requisições; `LivroService` coordena os casos de uso; `RelatorioService` e `EmailService` cuidam de suas responsabilidades; `ApiExceptionHandler` traduz erros para HTTP. A persistência continua no `LivroRepository`. |
| **OCP — Aberto/fechado** | Cada categoria nova exigia alterar a sequência de `if/else` dentro do cadastro. | O serviço recebe políticas `PoliticaDesconto` pelo construtor. As quatro regras percentuais são registradas como beans em `DescontoConfig`, reutilizando `DescontoPercentual`. Outra política pode ser registrada como bean sem modificar o controller ou o serviço. |
| **LSP — Substituição de Liskov** | A implementação de `LivroOperations` anunciava capacidades que recusava executar. Se o contrato promete essas operações, substituí-lo pelo controller resulta em falhas inesperadas. | O contrato passa a declarar somente as quatro operações efetivamente suportadas, implementadas por `LivroService`. As políticas de desconto também são intercambiáveis pelo contrato comum. Não havia uma hierarquia de subclasses de domínio que exigisse outra correção de LSP. |
| **ISP — Segregação de interfaces** | Um consumidor de CRUD dependia também de métodos de PDF, ERP, etiquetas, relatório e e-mail. | `LivroOperations` foi reduzida ao CRUD. Relatório e e-mail têm serviços próprios; métodos não implementados e sem chamadas foram removidos, sem criar interfaces vazias para funcionalidades inexistentes. |
| **DIP — Inversão de dependência** | A camada HTTP orquestrava diretamente a persistência e as regras de desconto estavam fixas no controller. A injeção em campo ocultava a dependência obrigatória. | O controller depende de `LivroOperations`; o serviço depende de `PoliticaDesconto` e do contrato Spring Data `LivroRepository`. As dependências são explícitas e injetadas por construtor. O repositório original já era uma interface; a refatoração aproveita essa abstração existente. |

## Comportamento preservado

- Endpoints: `GET /livros`, `POST /livros`, `PUT /livros/{id}` e `DELETE /livros/{id}`.
- Mesmo formato de livro: `id`, `titulo`, `autor`, `preco` e `categoria`.
- Descontos **apenas no cadastro**: ROMANCE 10%, TECNOLOGIA 20%, INFANTIL 30% e HQ 40%.
- Categorias desconhecidas ou nulas continuam sem desconto; a comparação é sensível a maiúsculas.
- A atualização usa o preço informado, sem reaplicar desconto, como no original.
- Livros da categoria RARO não podem ser excluídos.
- Cadastro continua gerando a contagem de livros e simulando o e-mail em log. Não há envio real.
- H2 em memória, Java 21 e dependências originais foram mantidos; respostas de sucesso continuam com HTTP 200.

## Correções complementares

Título obrigatório e preço positivo são verificados tanto no cadastro quanto na
atualização. Preços nulos ou não finitos também são rejeitados. Dados inválidos
retornam **400**, IDs inexistentes retornam **404**, e exclusão de livro raro retorna
**409**, com detalhes no formato `ProblemDetail`. Essas respostas corrigem erros
que antes se tornavam falhas genéricas do servidor.

O cadastro ignora IDs fornecidos pelo cliente para impedir a alteração acidental
de outro registro. A atualização usa o ID da URL. As operações de escrita são
transacionais. O relatório usa `count()` em vez de carregar todos os livros, e
`System.out.println` foi substituído pelo logger já disponível no projeto.
O console H2 anunciado no README foi habilitado.

## Verificação

`LivroApiTests` executa testes de integração com Spring, MockMvc e H2: CRUD completo,
persistência, descontos, categoria desconhecida/nula, validação no POST e no PUT,
IDs inexistentes, proteção de livros raros e proteção contra sobrescrita por ID.
Uma política de teste aplica desconto fixo à categoria ESPECIAL, demonstrando a
extensão por outro bean sem editar o serviço. Essa categoria existe somente nos testes.
O teste original de inicialização da aplicação foi mantido.

Execute com JDK 21:

```powershell
.\gradlew.bat test
```

## Escopo

Não foram implementados PDF, integração ERP ou etiquetas: eram apenas métodos
sem implementação, sem endpoints ou chamadas. O e-mail continua sendo simulado.
O tipo `Double` foi preservado para manter o modelo original; uma evolução para
uso financeiro real deve definir arredondamento e migrar valores monetários para
`BigDecimal`. Nenhuma biblioteca nova foi adicionada.

## Resultado da validação

Validação concluída em **09/10/2026**, no GitHub Actions, com **Java 21** e
**Gradle 9.7.1**, executando `sh gradlew test bootJar --no-daemon`.

- **18 testes aprovados**: 17 de integração da API e 1 de inicialização.
- **Zero falhas, zero erros e zero testes ignorados.**
- Compilação e geração do executável `bootJar` concluídas com sucesso.
- Workflow incluído em `.github/workflows/test.yml` para verificações futuras.

[Consultar a execução aprovada no GitHub Actions](https://github.com/h3nrique-beep/atividade-refac/actions/runs/37923826785).

A validação em Linux no GitHub resolveu a pendência de execução causada por
`AccessDeniedException` no ambiente Windows local restrito.
