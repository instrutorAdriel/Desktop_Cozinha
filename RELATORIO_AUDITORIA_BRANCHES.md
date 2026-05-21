# Relatório Técnico — Auditoria Multi-Branch

## Status da Auditoria

- Repositório analisado: `Desktop_Cozinha`
- Branch principal usada como referência: `origin/main`
- Arquivos alterados durante a auditoria: **nenhum**
- Branch `main` alterada: **não**
- Correções implementadas: **não**
- Correções propostas: **sim**

A análise foi feita em modo somente leitura, inspecionando as branches remotas e comparando diferenças com a `origin/main`.

---

## Log de Erros e Correções Propostas

| Branch | Falha encontrada | Como reproduzir | Como corrigir | Status |
|---|---|---|---|---|
| `feature/back-AlterarSenha` | A aplicação inicia direto em `AlterarSenha.fxml`, pulando o fluxo normal de login | Executar a branch e observar a tela inicial | Fazer a aplicação iniciar pelo `login.fxml` e acessar alteração de senha somente pelo fluxo correto | Identificado |
| `feature/back-AlterarSenha` | Senha pode estar sendo atualizada sem seguir o mesmo padrão de criptografia usado no login/cadastro | Alterar senha e depois tentar logar com ela | Usar o mesmo serviço de hash/criptografia usado no restante do sistema | Identificado |
| `feature/back-AlterarSenha` | Nome de tela/controller diverge do padrão da `main` | Integrar com a `main` e testar navegação | Padronizar nomes: `alterar-senha.fxml`, `AlteraSenhaController` ou outro padrão único | Identificado |
| `feature/back-EsqueciSenha` | Pacotes `Services` e `services` estão inconsistentes | Compilar em ambiente sensível a maiúsculas/minúsculas | Padronizar tudo para `services` em minúsculo | Identificado |
| `feature/back-EsqueciSenha` | Controllers foram renomeados com inicial minúscula, como `alteraSenhaController` | Abrir FXML que espera controller com outro nome | Usar PascalCase: `AlteraSenhaController`, `ConfirmaEmailController` | Identificado |
| `feature/back-EsqueciSenha` | Fluxo pode continuar mesmo após erro de validação de senha | Informar senhas diferentes | Adicionar `return` após cada validação inválida | Identificado |
| `feature/back-NotificaçãoDeAlerta` | Branch remove ou substitui arquivos importantes de login/cadastro | Comparar diff com `origin/main` | Reintegrar sem deletar telas, DAOs e services existentes | Identificado |
| `feature/back-NotificaçãoDeAlerta` | `MainApplication` inicia em tela de confirmação de e-mail, não no login | Executar branch | Restaurar inicialização pelo `login.fxml` | Identificado |
| `feature/back-NotificaçãoDeAlerta` | Possível uso incorreto de `java.awt.event.ActionEvent` em vez de JavaFX | Compilar a branch | Trocar para `javafx.event.ActionEvent` | Identificado |
| `feature/back-cadastro+` | Cadastro não valida cargo corretamente | Cadastrar usuário sem cargo | Validar todos os campos obrigatórios antes de inserir | Identificado |
| `feature/back-cadastro+` | Erro de e-mail duplicado pode aparecer como exceção técnica | Cadastrar e-mail já existente | Capturar erro SQL e mostrar mensagem amigável | Identificado |
| `feature/back-cadastroProduto` | `pom.xml` usa Java `25`, podendo falhar em máquinas com JDK 21 | Rodar `mvn compile` com JDK 21 | Padronizar versão Java do projeto, preferencialmente JDK 21 | Identificado |
| `feature/back-cadastroProduto` | Produto é cadastrado com campos numéricos tratados como texto | Cadastrar produto e verificar inserção no banco | Converter quantidade/estoque mínimo para tipos numéricos antes do insert | Identificado |
| `feature/back-cadastroProduto` | Não há validação de data vencida | Cadastrar produto perecível com data passada | Bloquear datas anteriores à data atual | Identificado |
| `feature/back-edicaoProdutos` | Filtros usam textos diferentes dos valores do banco | Marcar filtros de tipo de produto | Mapear textos da interface para os enums reais do banco | Identificado |
| `feature/back-edicaoProdutos` | Edição de produto não registra histórico | Editar produto e consultar histórico | Inserir movimentação na tabela de histórico após edição | Identificado |
| `feature/back-historico` | Colunas “Tipo” e “Quantidade” podem ficar vazias | Abrir tela de histórico com dados | Ajustar `PropertyValueFactory` para bater com os getters do model | Identificado |
| `feature/back-historico` | Arquivos locais/IDE foram adicionados indevidamente | Ver diff da branch | Remover arquivos `.idea` e artefatos locais antes do merge | Identificado |
| `feature/back-home` | Link “Esqueci senha?” aponta para tela possivelmente inexistente | Clicar em “Esqueci senha?” | Apontar para `esqueceu-senha.fxml` ou adicionar a tela correta | Identificado |
| `feature/back-home` | `fx:id` do botão login diverge entre FXML e controller | Abrir login e inspecionar injeção FXML | Padronizar `fx:id` e nome do atributo Java | Identificado |
| `feature/back-listaDoEstoque` | Filtros usam valores como `Perecível`, mas banco usa enum como `PERECIVEL` | Filtrar produtos por tipo | Criar mapeamento entre texto da tela e valores do banco | Identificado |
| `feature/back-listaDoEstoque` | Tela inicia direto na lista, pulando login | Executar branch | Integrar lista ao menu/home após login | Identificado |
| `feature/back-removerProdutos` | FXML chama `onRemoverProduto`, mas controller possui outro método | Clicar em “remover” na lista | Ajustar FXML para o método correto ou renomear o método | Identificado |
| `feature/back-removerProdutos` | Link “editar” abre tela de remoção | Clicar em “editar” | Separar corretamente ação de editar e ação de remover | Identificado |
| `feature/back-removerProdutos` | Remoção não pede confirmação | Clicar em remover produto | Adicionar alerta de confirmação antes do delete | Identificado |
| `feature/back-removerProdutos` | Remoção não atualiza lista nem registra histórico | Remover item e observar tabela/histórico | Atualizar tabela após remoção e salvar movimentação no histórico | Identificado |
| `feature/front-alterar-senha` | Tela de alteração de senha usa controller errado ou incompleto | Abrir tela e clicar em confirmar | Criar/restaurar controller próprio da alteração de senha | Identificado |
| `feature/front-alterar-senha` | Botão confirmar não possui ação funcional | Clicar em “confirmar alteração” | Adicionar `onAction` e validar senha/confirmação | Identificado |
| `feature/front-esqueceu-senha` | Botões “redefinir”, “confirmar” e “voltar” não possuem ação | Abrir tela e clicar nos botões | Criar controller e ligar os botões com `onAction` | Identificado |
| `feature/front-home-admin` | Tela administrativa foi salva como `login.fxml` | Executar branch | Renomear para `home-admin.fxml` ou `home.fxml` | Identificado |
| `feature/front-home-admin` | Menu lateral não possui ações de navegação | Clicar nos botões do menu | Criar controller e métodos de navegação | Identificado |
| `feature/front-home-admin` | Caminhos de imagem usam `Imagens` e `imagens` misturados | Rodar em ambiente case-sensitive | Padronizar uma única pasta e atualizar referências | Identificado |
| `feature/front-principal-buscar-produtos` | Tela de busca não possui controller | Clicar em buscar ou nos menus | Criar controller, campo de busca e integração com DAO | Identificado |
| `feature/front-tela-cadastrar-produtos` | `Cadastrar_produtos.fxml` não possui controller | Clicar para cadastrar produto | Ligar a tela ao `CadastroProdutoController` | Identificado |
| `feature/front-tela-cadastrar-produtos` | Campos não possuem `fx:id` | Tentar capturar dados no controller | Adicionar `fx:id` nos campos necessários | Identificado |
| `feature/front-tela-cadastrar-produtos` | Há labels genéricas como `Label` | Abrir tela | Remover placeholders ou substituir por textos reais | Identificado |
| `feature/front-tela-configuracao` | Tela de configuração está no arquivo `login.fxml` | Executar aplicação | Renomear para `configuracao.fxml` e ajustar rota | Identificado |
| `feature/front-tela-configuracao` | Popups `AdicionarPoup.fxml` e `EditarPoup.fxml` não têm controller | Abrir popups | Adicionar `fx:controller`, `fx:id` e ações de salvar/voltar | Identificado |
| `feature/front-tela-de-cadastro` | Tela de cadastro não possui controller funcional | Preencher cadastro e clicar cadastrar | Restaurar/criar `CadastroController` | Identificado |
| `feature/front-tela-de-cadastro` | Branch remove services e DAOs importantes da `main` | Comparar com `origin/main` | Não integrar remoções; manter services existentes | Identificado |
| `feature/front-tela-editar-produtos` | `MainApplication` tenta carregar `Login.fxml`, mas arquivo está em outro caminho | Executar branch | Corrigir caminho para o FXML real ou mover o arquivo | Identificado |
| `feature/front-tela-editar-produtos` | Branch não contém tela real de editar produtos | Abrir aplicação | Implementar tela de edição real, controller e rota | Identificado |
| `feature/front-tela-inicial` | Arquivo tem espaço no nome: `tela inicial.fxml` | Referenciar a tela no código | Renomear para `tela-inicial.fxml` ou `home.fxml` | Identificado |
| `feature/front-tela-inicial` | Tela não possui controller | Clicar nos botões | Criar controller e rotas para botões | Identificado |
| `feature/front-tela-inicial` | Textos de usuário/data aparecem fixos | Abrir tela inicial | Buscar dados da sessão e data atual dinamicamente | Identificado |
| `feature/front-tela-lista-produtos` | `listaProdutos.fxml` não possui controller | Abrir lista e tentar interagir | Criar controller e ligar tabela, busca, edição e remoção | Identificado |
| `feature/front-tela-relatorio` | Tela de relatório também foi colocada em `login.fxml` | Executar branch | Renomear para `relatorio.fxml` | Identificado |
| `feature/front-tela-relatorio` | Popups de entrada, saída e solicitação não possuem controller | Abrir os popups | Adicionar controllers e handlers de salvar/voltar | Identificado |
| `feature/front-tela-relatorio` | Menus possuem opções placeholder como `Action 1` e `Action 2` | Abrir os menus | Substituir por opções reais de tipo/unidade | Identificado |
| `feature/fr` | Login foi substituído por template “Hello!” | Executar branch | Restaurar login real da `main` | Identificado |
| `origin/login` | Login aponta para `home.fxml`, mas a tela pode não existir na branch | Fazer login com sucesso | Criar `home.fxml` ou apontar para tela existente | Identificado |
| `origin/login` | “Esqueci minha senha” não possui ação | Clicar no texto | Usar `Hyperlink` com `onAction` ou adicionar handler | Identificado |
| `origin/login` | Mensagem de erro de login é confusa para usuário inexistente | Tentar logar com e-mail não cadastrado | Separar validação de campos vazios, usuário inexistente e senha incorreta | Identificado |

---

## Problemas Gerais Encontrados

### 1. Muitas telas estão somente visuais

Várias branches possuem FXML pronto, mas sem:

- `fx:controller`
- `fx:id`
- `onAction`
- integração com DAO
- validação de campos
- navegação entre telas

### 2. Rotas inconsistentes

Algumas branches usam nomes diferentes para a mesma tela:

- `login.fxml`
- `Login.fxml`
- `home.fxml`
- `login.fxml` usado como tela de configuração
- `login.fxml` usado como tela de relatório
- `tela inicial.fxml`

Isso pode quebrar ao empacotar o projeto ou rodar em outro sistema.

### 3. Assets inconsistentes

Há mistura de caminhos como:

- `Imagens`
- `imagens`
- `imagem`
- arquivos com espaços
- arquivos com acentos
- caminhos com `../../../`

Correção recomendada:

```text
src/main/resources/com/example/desktop_cozinha/imagens
src/main/resources/com/example/desktop_cozinha/css
```

E usar sempre nomes simples, sem acento e sem espaço.

### 4. Layout pouco responsivo

Muitas telas usam:

```xml
prefWidth
prefHeight
layoutX
layoutY
maxWidth="-Infinity"
maxHeight="-Infinity"
```

Isso prejudica responsividade.

Correção recomendada:

- usar `BorderPane`, `VBox`, `HBox`, `GridPane`
- evitar posições fixas
- usar `hgrow`, `vgrow` e constraints adequadas

### 5. Arquivos de IDE versionados

Algumas branches adicionam arquivos como:

```text
.idea/dataSources.xml
.idea/misc.xml
.idea/sqldialects.xml
```

Correção recomendada:

- remover esses arquivos do versionamento
- adicionar `.idea/` no `.gitignore`, se o grupo decidir não versionar configurações da IDE

---

## Funcionalidades Pendentes

As funcionalidades abaixo aparecem iniciadas, mas ainda não estão totalmente operacionais:

1. Cadastro de produto
   - Precisa validação completa, controller conectado e tipos corretos no banco.

2. Lista de estoque
   - Precisa corrigir filtros e ligar corretamente busca/tabela.

3. Edição de produto
   - Precisa integrar com histórico e corrigir filtros.

4. Remoção de produto
   - Precisa corrigir método chamado pelo FXML, confirmação e atualização da tabela.

5. Histórico
   - Precisa corrigir nomes das colunas/propriedades.

6. Home/admin
   - Precisa controller e rotas funcionais.

7. Configuração
   - Precisa controllers nos popups e ações nos botões.

8. Relatórios/movimentações
   - Precisa controller, validação e gravação no banco.

9. Esqueci senha / alterar senha
   - Precisa padronizar services, controllers e fluxo de sessão/token.

---

## Conclusão

O projeto tem várias branches com telas e funcionalidades iniciadas, mas muitas estão incompletas ou divergentes da `main`.

Os principais problemas antes de fazer merge são:

1. FXML sem controller
2. Botões sem ação
3. Rotas de tela quebradas
4. Nome de arquivos com maiúsculas/minúsculas inconsistentes
5. Pacotes `Services` e `services` misturados
6. Campos do banco sem validação correta
7. Filtros usando valores diferentes dos enums do banco
8. Arquivos `.idea` adicionados ao Git
9. Branches removendo arquivos importantes da `main`
10. Telas visuais sem backend funcional

Recomendação final:

```text
Não fazer merge direto dessas branches na main.
Primeiro corrigir cada branch isoladamente, padronizar nomes/rotas/controllers e só depois integrar na main.
```