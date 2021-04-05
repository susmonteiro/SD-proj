# Bicloin
**Objetivo:** desenvolver o sistema *Bicloin* (*bicla* + *coin*) para gestão de uma plataforma de partilha de bicicletas.

# [Parte 1](https://github.com/tecnico-distsys/Bicloin/blob/main/part1.md)
- [Utilizadores](#1.1-utilizadores)
- [Estações](#1.2-Estações)
- [Aluguer de Bicicletas](#1.3-aluguer-de-bicicletas)
- [Arquitetura do sistema](#2-arquitetura-do-sistema)
    - [Hub](#2.1-servidor-hub)
    - [Rec](#2.2-servidor-rec)
    - [Registry](#2.3-serviço-de-nomes)
    - [App](#2.4-app)
- [Tecnologia](#3-tecnologia)
- [Resumo](#4-resumo)
- [Avaliação](#5-avaliação)
- [Perguntas](#Perguntas)

---

### 1.1 Utilizadores

Cada utilizador tem uma **conta**:
- ID: alfanumérico, 3 a 10
- Nome: max 30 caracteres
- Número de telemóvel (com o código do país)

    | Utilizador | Nome             | Telemóvel     |
    |------------|------------------|---------------|
    | alice      | Alice Andrade    | +35191102030  |

- saldo (começa com 0) em BICS

Carregamentos de *bicloins*:
- 1 EUR = 10 BIC
- carregamentos: 1€ a 20€ (inteiro)

---

### 1.2 Estações
- Número de docas (cada uma pode estar *ocupada* ou *livre*)
- Número de bicicletas
- Nome
- Abreviatura: 4 caracteres alfanuméricos (ID único)
- Coordenadas geográficas: notação decimal
- Prémio de entrega

| Nome da estação   | Abrev. | Coordenadas      | Nr. docas | Nr. bicicletas | Prémio |
|-------------------|:------:|------------------|----------:|---------------:|-------:|
| Oceanário         | ocea   | 38.7633, -9.0950 | 20        | 15             | 2      |
| IST Alameda       | ista   | 38.7369, -9.1366 | 20        | 19             | 3      |

---

### 1.3 Aluguer de bicicletas
- custo: 10 BIC, debitados no momento
    > o saldo do utilizador não pode ficar negativo
- devolver numa estação que tenha pelo menos uma doca livre
- utilizador recebe um prémio sempre que devolve a bicicleta, sempre positivos

---

## 2 Arquitetura do sistema
- [App](#2.4-app)
- [Hub](#2.1-servidor-hub): oferece procedimentos remotos. Contém apenas informação imutável.
- [Rec](#2.2-servidor-rec) (*record*): oferece procedimentos remotos básicos de r/w de registos. Cada registo tem um ID único (textual, sem espaços) e um valor
- [Registry](#2.3-serviço-de-nomes): serviço de nomes (conhece-se o **endereço** e **porto** apenas e obtém-se os outros a partir desse dinamicamente)

---

### 2.1 Servidor Hub
Procedimentos remotos:
- [ ] `balance(nome_utilizador) -> saldo`
- [ ] `top_up(nome_utilizador, montante, num_telemovel) -> saldo_após_carregamento` - assume-se que o carregamento é sempre bem sucedido e não existe a necessidade de contactar um serviço de pagamentos. É preciso verificar se o número de telemóvel está correto
- [ ] `info_station(id_estação) -> nome_estação, coordenadas, capacidade_docas, prémio, número_bicicletas_disponíveis, número_levantamentos, número_entregas`
- [ ] `locate_station(coordenadas_utilizador, k) -> k_estações_mais_próximas` - as estações são retornadas por ordem *crescente* de distância. Para cada estação é enviado apenas o seu id. Usar a [fórmula de haversine](https://pt.wikipedia.org/wiki/F%C3%B3rmula_de_haversine)  
- [ ] `ola susikissss`
- [ ] `bike_up(nome_utilizador, coordenadas, estação_levantar)` - um utilizador deve estar a menos de 200m da estação e só pode levantar uma bicicleta de cada vez 
- [ ] `bike_down(nome_utilizador, coordenadas, estação_entregar)` - um utilizador deve estar a menos de 200m da estação
- [ ] `ping(pedido_is_alive) -> estado_servidor`
- [ ] `sys_status(pedido_ponto_situação) -> info_servidores:[path][up|down]` - *hubs* e *rec*

<u>**Ver parte dos Comandos da App**</u>

> *hub-tester* - testes de integração

> réplicas do *hub*

Argumentos do hub

    $ hub servidor_ZooKeeper porto_ZooKeeper servidor_próprio porto_próprio número_instância [ficheiros_dados]* [opção_inicialização_registos]

Por exemplo:

    $ hub localhost 2181 localhost 8081 1 users.csv stations.csv initRec

> cancelar arranque caso haja um problema com um dos ficheiros de dados 

Caso a opção initRec seja indicada, o hub deve contactar o rec para criar todos os registos mutáveis necessários, com valores iniciais, com base nos valores por omissão ou nos valores indicados nos ficheiros de dados. Caso contrário, os registos existentes no rec devem ser mantidos. Isto permite distinguir quando se está a fazer o lançamento inicial ou quando se está a lançar ou recuperar uma réplica.

---
### 2.2 Servidor Rec
3 procedimentos remotos:
- [ ] `read(nome_registo) -> valor_atual`
- [ ] `write(nome_registo) -> valor_a_escrever`
- [ ] `ping(pedido_de_sinal) -> estado_servidor`

Os registos são implicitamente criados da primeira vez que existe um acesso com o seu nome, seja uma escrita ou uma leitura.
Quando um registo é criado por uma leitura, deve ser preenchido com um valor por omissão que simbolize *vazio*.

> *rec-tester* - testes de integração

Argumentos do rec

    $ rec servidor_ZooKeeper porto_ZooKeeper servidor_próprio porto_próprio número_instância

---
### 2.3 Serviço de nomes
A usar: [*ZooKeeper*](https://zookeeper.apache.org/)
Cabe a cada servidor, quando lançado, registar-se a si próprio no serviço de nomes.
Os nomes a usar para registar os `hub` são do tipo `/grpc/bicloin/hub/[serial_id]`. 
O `rec` deve ser registado com o nome: `/grpc/bicloin/rec/1`.

> A informação no *ZooKeeper* pode estar desatualizada

Na `app` deve ser apresentada uma mensagem de erro por cada tentativa falhada (1 linha de texto). Estas tentativas devem ser feitas de forma transparente para o utilizador.

---
### 2.4 App
1 linha de texto = 1 comando (ler linha a linha do terminal)

Argumentos

    $ app servidor_ZooKeeper porto_ZooKeeper id_utilizador telemóvel coordenadas

Exemplo

    $ app localhost 2181 alice +35191102030 38.7380 -9.3000

Depois de lançada, aparece a *prompt* `>`

> Mensagens de confirmação -> 1 linha só: [OK|ERRO] (text)*

### Redirecionamento de dados
    $ app localhost 2181 bruno 38.737613 -9.303164 < commandos.txt
- `#`: comentário
- `zzz [msecs]`: sleep com respetiva duração

Comandos:
- `ping`: chama procedimento do hub
- `sys_status`: chama procedimento do hub
- `help`: lista e explica comandos
> Podemos adicionar mais comandos desde que explicados no `help`

---
## 3. Tecnologia

###  Validações
> Argumentos devem ser obrigatoriamente validados pelos servidores
### Faltas
> Programa deve apanhar exceção e imprimir informação (1 linha de texto) e para a execução do procedimento
### Persistência
> Não necessário
---
## 4. Resumo
A entregar: o `hub`, `hub-tester`, `rec`, `rec-tester` e a `app`.

---
## 5. Avaliação
- Foto
- ID de grupo: **A24**
    - colocar no README na **raiz do projeto**
    - alterar nos `pom.xlm` e source code


---
## Perguntas
- No rec porque recebemos o ip do próprio? Não tem que ser sempre `localhost`?
    > nope, vamos usar mais tarde
- É suposto termos uma thread para dar *shutdown* do server como no exemplo desta aula?
    > sim, é giro
- É suposto metermos os default arguments no pom?
    > yep, é boa ideia. Ou então usar [AppAssembler](https://github.com/tecnico-distsys/example_java-app/tree/appassembler)
