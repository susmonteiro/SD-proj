# Guião de Demonstração

## 1. Preparação do sistema

Para testar o sistema e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.1. Lançar o *registry* 

Para lançar o servidor ZooKeeper, navegue até à pasta zookeeper/bin e corra o comando ./zkServer.sh start em Linux, ou zkServer.cmd em Windows. No final para encerrar o servidor, corra o comando ./zkServer.sh stop em Linux, ou faça Ctrl+C na consola em Windows.


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
    $ mvn compile exec:java [-Ddebug|-DdebugDemo] [-Dexec.args="ZooKeeper_IP ZooKeeper_PORT IP PORT instance_num"]
    ```

    Este comando vai colocar o *rec* no endereço *localhost*, na porta *8091* e com número de instância *1*.

    > Opção '-Ddebug|-DdebugDemo' opcional ativa método de depuramento com *logs* de execução.

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
    $ mvn compile exec:java [-Ddebug|-DdebugDemo] [-Dexec.args="ZooKeeper_IP ZooKeeper_PORT IP PORT instance_num users.csv stations.csv [initRec]"]
    ```

    Este comando vai colocar o *hub* no endereço *localhost*, na porta *8081* e com número de instância *1*.

    > Opção "-Ddebug|-DdebugDemo" opcional ativa método de depuramento com *logs* de execução
    
    > Opção '-Dexec.args="..."' também é opcional e permite especificar argumentos. Quando se usa esta opção, é necessário passar todos os argumentos.

- Ou diretamente fazendo `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.
    ```sh
    $ hub servidor_ZooKeeper porto_ZooKeeper servidor_próprio porto_próprio número_instância users.csv stations.csv [initRec]
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
$ mvn compile exec:java [-Ddebug] [-Dexec.args="ZooKeeper_IP ZooKeeper_PORT username phoneNumber latitude longitude"]
```


**Nota:** Para poder correr o script *app* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.

Abrir outra consola, e iniciar a aplicação com o utilizador bruno.

Depois de lançar todos os componentes, tal como descrito acima, já temos o que é necessário para usar o sistema através dos comandos.

Quando quiser sair da aplicação, introduza o comando 'exit'.


## 2. Teste dos comandos

Nesta secção vamos correr os comandos necessários para testar todas as operações do sistema.
Cada subsecção é respetiva a cada operação presente no *hub*.

Em qualquer momento, na sequência de qualquer comando, podem ocorrer 2 erros:
- O Hub não conseguir comunicar com o Rec, o que resulta em: `ERRO - UNAVAILABLE:<mensagem_hub>`
- A App não consegue comunicar com o Hub, o que resulta em: `ERRO - UNAVAILABLE:<mensagem_grpc>`

### 2.1 *balance*
- casos normais:

    ```> balance```

    Devolve o balance do user: `alice 0 BIC`

- casos de erro:
    - caso o user usado para correr a app não esteja registado, é devolvido: `ERRO - INVALID_ARGUMENT:<mensagem_hub>`

### 2.2 *top-up*
- casos normais:

    ```> top-up 10```

    Devolve o balance atualizado do user: `alice 100 BIC`

- casos de erro:
    - caso o user usado para correr a app não esteja registado, é devolvido: `ERRO - INVALID_ARGUMENT:<mensagem_hub>`

    - caso não seja introduzido um valor, ou este não seja um número, é devolvido: `ERRO - Argumentos incorretos para comando top-up!`


### 2.3 *tag*
- casos normais:

    ```> tag 38.7376 -9.3031 loc1```

    Devolve: `OK`

- casos de erro:
    - caso não seja introduzido algum dos valores ou este não seja coordenadas ou nome da tag: `ERRO - Argumentos incorretos para comando tag!`

### 2.4 *move*
- casos normais:

    ```> move loc1```

    Devolve a nova localização do utilizador: `alice em https://www.google.com/maps/place/38.7376,-9.3031`

    ```> move 38.6867 -9.3117``` 

    Devolve a nova localização do utilizador: `alice em https://www.google.com/maps/place/38.6867,-9.3117`

- casos de erro:
    - caso não seja introduzido algum dos valores ou este não seja coordenadas ou nome da tag: `ERRO - Argumentos incorretos para comando move!`

### 2.5 *at*
- casos normais:
    
    ```> at```

    Devolve a localização atual do utilizador: `alice em https://www.google.com/maps/place/38.7376,-9.3031`

- casos de erro: não existem

### 2.6 *scan*
- casos normais:

    ```> scan 2```

    Devolve a informação das 2 estações mais próximas: `istt, lat 38.7372, -9.3023 long, 20 docas, 4 BIC prémio, 12 bicicletas, a 218 metros`
    `stao, lat 38.6867, -9.3124 long, 30 docas, 3 BIC prémio, 20 bicicletas, a 5805 metros`

- casos de erro:
    - caso não seja introduzido um número de estações, devolve: `ERRO - Argumentos incorretos para comando scan!`

    - caso seja introduzido um número negativo, devolve: `ERRO - INVALID_ARGUMENT: Invalid number, please scan 0 or higher.`

### 2.7 *info*
- casos normais:

    ```> info istt```

    Devolve a informação da estação: `IST Taguspark, lat 38.7372, -9.3023 long, 20 docas, 4 BIC prémio, 12 bicicletas, 0 levantamentos, 0 devoluções.`

- casos de erro:
    - caso a estação não exista, devolve: `ERRO - INVALID_ARGUMENT: Invalid station.`

    - caso não seja introduzido um identificador, devolve: `ERRO - Argumentos incorretos para comando info!`

### 2.8 *bike-up*
- casos normais:

    ```> bike-up istt```

    Devolve: `OK`

- casos de erro:
    - caso não seja introduzido um identificador de estação, devolve: `ERRO - Argumentos incorretos para comando bike-up!`

    - caso a estação introduzida não exista, devolve: `ERRO - INVALID_ARGUMENT: Invalid station.`

    - caso o utilizador já esteja numa bicicleta, devolve: `ERRO - FAILED_PRECONDITION: User already has a bicycle.`

### 2.9 *bike-down*
- casos normais:

    ```> bike-down istt```

    Devolve: `OK`

- casos de erro:
    - caso não seja introduzido um identificador de estação, devolve: `ERRO - Argumentos incorretos para comando bike-down!`

    - caso a estação introduzida não exista, devolve: `ERRO - INVALID_ARGUMENT: Invalid station.`

    - caso o utilizador não esteja numa bicicleta, devolve: `ERRO - FAILED_PRECONDITION: User doesnt have bicycle.`

    - caso a estação seja demasiado longe, devolve: `ERRO - FAILED_PRECONDITION: User is too far away from station to request a bicycle.`

### 2.10 *ping*
- casos normais:

    ```> ping ```

    Devolve: `Hello alice! Im Hub 1 at localhost:8081`

- casos de erro: não existem.

### 2.11 *sys-status*
- casos normais:

    ```> sys-status ```

    Devolve: `Path: localhost:8081 Status: true`
    `Path: localhost:8091 Status: true`

- casos de erro: não existem.

## 3. Teste da replicação e da tolerância a faltas

4 recs, 1 hub, 2 apps

Lançar o servidor de nomes ZooKeeper:

Navegue até à pasta zookeeper/bin e caso esteja:
    Em Linux: corra o comando ./zkServer.sh start
    Em Windows: corra o comando zkServer.cmd

No final para encerrar o servidor, corra o comando ./zkServer.sh stop em Linux, ou faça Ctrl+C na consola em Windows.

Instalar todos os módulos e as suas dependências, dentro da pasta A24-Bicloin, fazer:

```sh
mvn clean install -DskipTests
```

Em primeiro lugar, para lançar as réplicas (Rec), abrir 4 terminais, navegar até à pasta A24-Bicloin/rec e executar os comandos em cada um deles, respetivamente:

```sh
mvn compile exec:java -DdebugDemo -Dexec.args="localhost 2181 localhost 8091 1"
mvn compile exec:java -DdebugDemo -Dexec.args="localhost 2181 localhost 8092 2"
mvn compile exec:java -DdebugDemo -Dexec.args="localhost 2181 localhost 8093 3"
mvn compile exec:java -DdebugDemo -Dexec.args="localhost 2181 localhost 8094 4"
```

> Com 4 réplicas, a nossa implementação terá `ReadThreshold = 2` e `WriteThreshold = 3`. Consequentemente, uma leitura tolera `2` falta e uma escrita tolera `1` falta.

De seguida, para lançar o Hub, navegar até à pasta A24-Bicloin/hub e fazer noutro terminal:

```sh
mvn compile exec:java -DdebugDemo -Dexec.args="localhost 2181 localhost 8081 1 ../demo/data/users.csv ../demo/data/stations.csv initRec"
```

E finalmente, para lançar 2 aplicações móveis, navegar até à pasta A24-Bicloin/app e fazer noutros 2 terminais:

```sh
mvn compile exec:java -Dexec.args="localhost 2181 alice +35191102030 38.7380 -9.3000"
mvn compile exec:java -Dexec.args="localhost 2181 bruno +35193334444 38.7038 -9.1597"
```

Depois de estas componentes estarem todas a correr:

No terminal respetivo, executar:
- Caso normal:
```sh
#REC 1 - UP
#REC 2 - UP
#REC 3 - UP
#REC 4 - UP
ALICE > top-up 5
# observar sequência de pedidos e respostas
BRUNO > info istt
# observar número de bikes
ALICE > bike-up istt
# observar sequência de pedidos e respostas
BRUNO > info istt
# observar alterações feitas pela Alice
```

- Testar o comportamento do sistema no caso de **1** falhas para uma escrita:
```sh
REC 1 > [ctrl+z]
#REC 2 - UP
#REC 3 - UP
#REC 4 - UP
ALICE > top-up 5
# observar que uma escrita tolera 1 falta
# O valor é escrito nas 3 replicas *UP* -> REC 2, REC 3, REC 4
```

- Testar o comportamento do sistema no caso de **2** falhas para uma leitura:

```sh
REC 1 > fg
REC 2 > [ctrl+z]
REC 3 > [ctrl+z]
#REC 4 - UP
ALICE > balance
# observar que uma leitura tolera 2 faltas
# verificar que mesmo que algumas réplicas estejam em baixo, o valor obtido é o mais atualizado
# Rec 1 -> oldValue
# Rec 4 -> newValue      (top-up anterior)
```

- Testar o comportamento do sistema para *N-1* falhas:
```sh
#REC 1 - UP 
#REC 2 - DOWN
#REC 3 - DOWN
REC 4 > [ctrl+z]
ALICE > balance
BRUNO > top-up 10
# O sistema não responderá até obter respostas de um quórum.

# Ligar as réplicas e observar que o pedido é finalizado (leitura).
#REC 1 - UP  
REC 2 > fg  # resposta do balance recebida aqui

# Ligar as réplicas e observar que o pedido é finalizado (escrita).
#REC 1 - UP  
#REC 2 - UP  
REC 3 > fg  # resposta do topUp recebida aqui
REC 4 > fg  # não necessário
```

----

## 4. Considerações Finais

Estes testes não cobrem tudo, pelo que devem ter sempre em conta os testes de integração e o código.


**Nota:** Cálculo da distância com a [fórmula de haversine](https://github.com/jasonwinn/haversine)