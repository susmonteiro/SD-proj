# Guião de Demonstração

## 1. Preparação do sistema

Para testar o sistema e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.1. Lançar o *registry* 

*Unimplemented*


### 1.2. Compilar o projeto

Primeiramente, é necessário compilar e instalar todos os módulos e suas dependências --  *rec*, *hub*, *app*, etc.
Para isso, basta ir à pasta *root* do projeto e correr o seguinte comando:

```sh
$ mvn clean install -DskipTests
```

### 1.3. Lançar e testar o *rec*

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor *rec* .
Para isso:

-  Basta ir à pasta *rec* e executar:
    ```sh
    $ mvn compile exec:java [-Ddebug] [-Dexec.args="ZooKeeper_IP ZooKeeper_PORT IP PORT instance_num"]
    ```

    Este comando vai colocar o *rec* no endereço *localhost*, na porta *8091* e com número de instância *1*.

    > Opção '-Ddebug' opcional ativa método de depuramento com *logs* de execução.

    > Opção '-Dexec.args="..."' também é opcional e permite especificar argumentos. Quando se usa esta opção, é necessário passar todos os argumentos.

- Ou diretamente fazendo `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.
    ```sh
    $ rec servidor_ZooKeeper porto_ZooKeeper servidor_próprio porto_próprio número_instância
    ```

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd rec-tester
$ mvn compile exec:java
```

> É possivel editar facilmente `rec-tester` para testar todos os outros metodos do *Rec* (descomentar templates).

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

> Para manter a consistência dos testes, é necessário reiniciar o servidor a para cada execução total da bateria de testes de integração.

Todos os testes devem ser executados sem erros.


### 1.4. Lançar e testar o *hub*

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor *hub* .
Para isso:

-  Basta ir à pasta *hub* e executar:
    ```sh
    $ mvn compile exec:java [-Ddebug] [-Dexec.args="recIP recPORT IP PORT instance_num users.csv stations.csv [initRec]"]
    ```

    Este comando vai colocar o *hub* no endereço *localhost*, na porta *8081* e com número de instância *1*.

    > Opção "-Ddebug" opcional ativa método de depuramento com *logs* de execução
    
    > Opção '-Dexec.args="..."' também é opcional e permite especificar argumentos. Quando se usa esta opção, é necessário passar todos os argumentos.

- Ou diretamente fazendo `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.
    ```sh
    $ hub servidor_Rec porto_Rec servidor_próprio porto_próprio número_instância users.csv stations.csv [initRec]
    ```
    > Opção "initRec" opcional força a inicialização dos registos (escreve por cima se Rec já em funcionamento) 

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd hub-tester
$ mvn compile exec:java
```

> É possivel editar facilmente `hub-tester` para testar todos os outros metodos do *Hub* (descomentar templates)

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

> Para manter a consistência dos testes, é necessário reiniciar o servidor a para cada execução total da bateria de testes de integração.

Os ficheiros a usar para que os testes corram bem são os `demo/data/users.csv` e `demo/data/stations.csv` para os users e as estações, respetivamente.
É de notar que ao executar o hub com o comando `mvn compile exec:java`, estes são os ficheiros passados como argumento.200m

Em relação a alterações aos ficheiros iniciais:

- `users.csv` - foram adicionados novos users, sem nenhuma característica particular. A razão para esta adição é que diferentes ficheiros de testes usem sempre diferentes users, garantindo independência entre os testes

- `stations.csv` - foram adicionadas duas estações:
    - `empt` - estação com zero bicicletas disponíveis. Permite testar o caso em que um user tenta fazer um bikeUp quando não há bicicletas disponíveis.
    - `full` - estação com todas as docas ocupadas. Permite testar o caso em que um user tenta fazer um bikeDown quando não há docas disponíveis.

Todos os testes devem ser executados sem erros.

### 1.5. *App*

Iniciar a aplicação com a utilizadora alice:

```sh
$ app localhost 2181 alice +35191102030 38.7380 -9.3000 [-Ddebug]
```

> Opção "-Ddebug" opcional ativa método de depuramento com *logs* de execução
> Também é possível enviar o conteúdo de um ficheiro de texto para o programa, com o operador de redirecionamento < da shell do sistema operativo. Para isso pode utilizar o nosso ficheiro de demonstração comandos.txt adicionando `< ../demo/comandos.txt`.

Também pode ser executado com Maven:

```sh
$ mvn compile exec:java [-Ddebug] [-Dexec.args="hubIP hubPORT username phoneNumber latitude longitude"]
```


**Nota:** Para poder correr o script *app* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.

Abrir outra consola, e iniciar a aplicação com o utilizador bruno.

Depois de lançar todos os componentes, tal como descrito acima, já temos o que é necessário para usar o sistema através dos comandos.

Quando quiser sair da aplicação, introduza o comando 'exit'.


## 2. Teste dos comandos

Nesta secção vamos correr os comandos necessários para testar todas as operações do sistema.
Cada subsecção é respetiva a cada operação presente no *hub*.

### 2.1. *balance*

- Caso normal: 
    - Todas as validações passam:
        - Hub responde com o valor. 
        - App responde com `<username> <valor> BIC` 

- Casos de erro:
    1. Hub recebe id de utilizador inválido (não existe no ficheiro de utilizadores):
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub não consegue comunicar com servidor de registos (Rec):
        - Hub envia erro `UNAVAILABLE`.
        - App responde com `ERRO - UNAVAILABLE:<mensagem_hub>`.
    1. App não consegue comunicar com o Hub:
        - App responde com `ERRO - UNAVAILABLE:<mensagem_grpc>`

### 2.2 *top-up*

- Caso normal:
    - Todas as validações passam:
        - Hub responde com o novo valor.
        - App responde com `<username> <valor> BIC` 

- Casos de erro:
    1. Hub recebe id de utilizador inválido (não existe no ficheiro de utilizadores):
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub recebe número de telemóvel do utilizador que não corresponde com o registado:
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub recebe valor a carregar inválido (obrigatoriamente entre 1-20):
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub não consegue comunicar com servidor de registos (Rec):
        - Hub envia erro `UNAVAILABLE`.
        - App responde com `ERRO - UNAVAILABLE:<mensagem_hub>`.
    1. App não consegue comunicar com o Hub:
        - App responde com `ERRO - UNAVAILABLE:<mensagem_grpc>`

### 2.3 *info-station*

- Caso normal 
    - Todas as validações passam:
        - Hub responde com toda a informação sobre a estação (nome, coordenadas, prémio e número de docas, bicicletas, levantamentos e entregas)
        - App responde com `<StationName>, lat <latitude>, <longitude> long, <nDocas> docas, <reward> BIC prémio, <nBicycles> bicicletas, <nPickUps> levantamentos, <nDeliveries> devoluções.` 

- Casos de erro:
    1. Hub recebe id de estação inválido (não existe no ficheiro de utilizadores):
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub não consegue comunicar com servidor de registos (Rec):
        - Hub envia erro `UNAVAILABLE`.
        - App responde com `ERRO - UNAVAILABLE:<mensagem_hub>`.
    1. App não consegue comunicar com o Hub:
        - App responde com `ERRO - UNAVAILABLE:<mensagem_grpc>`

### 2.4 *locate-station*

- Caso normal 
    - Todas as validações passam:
        - Hub responde com o id das `k` estações mais próximas, sendo `k = min(valor pedido, número de estações existentes)`
        - App responde com `<StationName>, lat <latitude>, <longitude> long, <nDocas> docas, <reward> BIC prémio, <nBicycles> bicicletas, a <distance> metros` 

- Casos de erro:
    1. Hub recebe um valor de latitude (-90,90) ou longitude (-180,180) inválido:
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub recebe um `k` (número de estações a devolver) negativo:
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub não consegue comunicar com servidor de registos (Rec):
        - Hub envia erro `UNAVAILABLE`.
        - App responde com `ERRO - UNAVAILABLE:<mensagem_hub>`.
    1. App não consegue comunicar com o Hub:
        - App responde com `ERRO - UNAVAILABLE:<mensagem_grpc>`

### 2.5 *bike-up*

- Caso normal:
    - Todas as validações passam:
        - Hub responde com uma mensagem vazia.
        - App responde com `OK`

- Casos de erro:
    1. Hub recebe id de utilizador inválido (não existe no ficheiro de utilizadores):
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub recebe coordenadas inválidas (valor invalido):
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub recebe id da estação inválido (não existe no ficheiro de estações):
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub recebe coordenadas do utilizador que estão muito longe da estação (distância de *haversine* superior a 200m):
        - Hub envia erro `FAILED_PRECONDITION`. 
        - App responde com `ERRO - FAILED_PRECONDITION:<mensagem_hub>`.
    1. Hub recebe id de utilizador que já tem na sua posse uma bicicleta:
        - Hub envia erro `FAILED_PRECONDITION`. 
        - App responde com `ERRO - FAILED_PRECONDITION:<mensagem_hub>`.
    1. Hub recebe id de utilizador que não possui saldo suficiente (pelo menos 10 Bics para levantar):
        - Hub envia erro `FAILED_PRECONDITION`. 
        - App responde com `ERRO - FAILED_PRECONDITION:<mensagem_hub>`.
    1. Hub recebe id de uma estação que nao possui bicicletas disponíveis:
        - Hub envia erro `FAILED_PRECONDITION`. 
        - App responde com `ERRO - FAILED_PRECONDITION:<mensagem_hub>`.
    1. Hub não consegue comunicar com servidor de registos (Rec):
        - Hub envia erro `UNAVAILABLE`.
        - App responde com `ERRO - UNAVAILABLE:<mensagem_hub>`.
    1. App não consegue comunicar com o Hub:
        - App responde com `ERRO - UNAVAILABLE:<mensagem_grpc>`

### 2.6 *bike-down*

- Caso normal:
    - Todas as validações passam:
        - Hub responde com uma mensagem vazia.
        - App responde com `OK`

- Casos de erro:
    1. Hub recebe id de utilizador inválido (não existe no ficheiro de utilizadores):
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub recebe coordenadas inválidas (valor invalido):
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub recebe id da estação inválido (não existe no ficheiro de estações):
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub recebe coordenadas do utilizador que estão muito longe da estação (distância de *haversine* superior a 200m):
        - Hub envia erro `FAILED_PRECONDITION`. 
        - App responde com `ERRO - FAILED_PRECONDITION:<mensagem_hub>`.
    1. Hub recebe id de utilizador que não tem na sua posse uma bicicleta:
        - Hub envia erro `FAILED_PRECONDITION`. 
        - App responde com `ERRO - FAILED_PRECONDITION:<mensagem_hub>`.
    1. Hub recebe id de uma estação que nao possui docas de estacionamento disponíveis:
        - Hub envia erro `FAILED_PRECONDITION`. 
        - App responde com `ERRO - FAILED_PRECONDITION:<mensagem_hub>`.
    1. Hub não consegue comunicar com servidor de registos (Rec):
        - Hub envia erro `UNAVAILABLE`.
        - App responde com `ERRO - UNAVAILABLE:<mensagem_hub>`.
    1. App não consegue comunicar com o Hub:
        - App responde com `ERRO - UNAVAILABLE:<mensagem_grpc>`

### 2.7 *ping*

- caso normal:
    - Todas as validações passam e a app consegue comunicar com o hub
        - Hub responde com uma mensagem a identificar-se
        - App responde com `<Mensagem de resposta do Hub>` 

- Casos de erro:
    1. Hub recebe um valor de input vazio
        - Hub envia erro `INVALID_ARGUMENTS`. 
        - App responde com `ERRO - INVALID_ARGUMENT:<mensagem_hub>`.
    1. Hub não consegue comunicar com servidor de registos (Rec):
        - Hub envia erro `UNAVAILABLE`.
        - App responde com `ERRO - UNAVAILABLE:<mensagem_hub>`.
    1. App não consegue comunicar com o Hub:
        - App responde com `ERRO - UNAVAILABLE:<mensagem_grpc>`

### 2.8 *sys-status*

- caso normal:
    - Todas as validações passam. App consegue comunicar com o hub e este, por sua vez, consegue comunicar com o rec
        - Hub responde com a mensagem de greeting que recebeu do rec, à qual junta a sua própria mensagem
        - App responde com `Path:<HubPath> Status:<HubStatus>` 

- Casos de erro:
    1. Hub não consegue comunicar com servidor de registos (Rec):
        - Hub envia erro `UNAVAILABLE`.
        - App responde com `ERRO - UNAVAILABLE:<mensagem_hub>`.
    1. App não consegue comunicar com o Hub:
        - App responde com `ERRO - UNAVAILABLE:<mensagem_grpc>`

----

## 3. Considerações Finais

Estes testes não cobrem tudo, pelo que devem ter sempre em conta os testes de integração e o código.


**Nota:** Cálculo da distância com a [fórmula de haversine](https://github.com/jasonwinn/haversine)