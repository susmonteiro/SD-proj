# Guião de Demonstração

## 1. Preparação do sistema

Para testar o sistema e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.2. Compilar o projeto

Primeiramente, é necessário compilar e instalar todos os módulos e suas dependências --  *rec*, *hub*, *app*, etc.
Para isso, basta ir à pasta *root* do projeto e correr o seguinte comando:

```sh
$ mvn clean install -DskipTests
```

### 1.3. Lançar e testar o *rec*

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor *rec* .
Para isso basta ir à pasta *rec* e executar:

```sh
$ mvn compile exec:java [-Ddebug]
```

Este comando vai colocar o *rec* no endereço *localhost*, na porta *8091* e com número de instância *1*.
> Opção "-Ddebug" opcional ativa método de depuramento com *logs* de execução

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd rec-tester
$ mvn compile exec:java
```

> É possivel editar facilemento `rec-tester` para testar todos os outros metodos do *Rec* (descomentar templates)

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

Todos os testes devem ser executados sem erros.


### 1.4. Lançar e testar o *hub*

TODO

### 1.5. *App*

Iniciar a aplicação com a utilizadora alice:

```sh
$ app localhost 2181 alice +35191102030 38.7380 -9.3000 [-Ddebug]
```

> Opção "-Ddebug" opcional ativa método de depuramento com *logs* de execução
> Também é possível enviar o conteúdo de um ficheiro de texto para o programa, com o operador de redirecionamento < da shell do sistema operativo. Para isso pode utilizar o nosso ficheiro de demonstração comandos.txt adicionando `< ../demo/comandos.txt`.

**Nota:** Para poder correr o script *app* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.

Abrir outra consola, e iniciar a aplicação com o utilizador bruno.

Depois de lançar todos os componentes, tal como descrito acima, já temos o que é necessário para usar o sistema através dos comandos.

Quando quiser sair da aplicação, introduza o comando 'exit'.


## 2. Teste dos comandos

Nesta secção vamos correr os comandos necessários para testar todas as operações do sistema.
Cada subsecção é respetiva a cada operação presente no *hub*.

### 2.1. *balance*

TODO

(mostrar casos normais e casos de erro)

### 2.2 *top-up*

TODO

(idem)

### 2... TODO

(idem)

----

## 3. Considerações Finais

Estes testes não cobrem tudo, pelo que devem ter sempre em conta os testes de integração e o código.
