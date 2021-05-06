# Relatório do projeto *Bicloin*

Sistemas Distribuídos 2020-2021, segundo semestre

## Autores

**Grupo A24**


| Número | Nome               | Utilizador                                     | Correio eletrónico                  |
| --------|-------------------|------------------------------------------------| ------------------------------------|
| 92437  | Catarina Gonçalves | <https://git.rnl.tecnico.ulisboa.pt/ist192437> | <mailto:catarina.g.goncalves@tecnico.ulisboa.pt>   |
| 92456  | Duarte Bento       | <https://git.rnl.tecnico.ulisboa.pt/ist192456> | <mailto:duarte.bento@tecnico.ulisboa.pt>     |
| 92560  | Susana Monteiro    | <https://git.rnl.tecnico.ulisboa.pt/ist192560> | <mailto:s.moreno.monteiro@tecnico.ulisboa.pt> |

![Catarina](ist192437.png) ![Duarte](ist192456.png) ![Susana](ist192560.png)


## Melhorias da primeira parte

<!-- _(que correções ou melhorias foram feitas ao código da primeira parte -- incluir link para commits no Git onde a alteração foi feita)_ -->

- [Melhorias no guia de demonstração](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/5b432fcd693fd06ddbf1deb0aa0d0c5b785404d7) (teste dos comandos)

- [Correção de um comando na App](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/6d1499e2a40c538249c7c59e3881e78e7ee9fb78) - adição do link para o google maps no comando `at`

- [Usar utf-8 encoding para os testes](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/7c8c9f3741915883587edff6ebd9ab886545fafb)




## Modelo de faltas

<!-- _(que faltas são toleradas, que faltas não são toleradas)_ -->

Sendo a nossa implementação baseada num protocolo de registo distribuído coerente, adotou-se um modelo de faltas semelhante ao deste protocolo. Assim assume-se que:
- o sistema é assíncrono (tanto a comunicação como o processamento podem demorar um tempo arbitrário)
- a comunicação é fiável (as mensagens são sempre recebidas desde que nem o cliente nem o servidor falhem), mas a ordem de chegada das mensagens não é necessariamente igual à ordem de envio das mesmas
- podem ocorrer faltas silenciosas/crash, mas nunca faltas bizantinas
- as faltas das réplicas do `rec` são transientes e não definitivas
- o `hub` não falha durante o processamento de procedimentos remotos, não deixando operações inacabadas 
- o conjunto de réplicas é estático (conhecido *a priori*), ainda que os seus endereços possam variar ao longo do tempo

Assim, na nossa implementação base são toleradas *f* faltas silenciosas/crash quando o grau de replicação é *2f+1*. Após as otimizações este número é diferente para as operações de leitura e de escrita, sendo referido e explicado [aqui](#opções-de-implementação).

As faltas bizantinas/arbitrárias não são toleradas. Também não se tolera faltas no `hub` nem no `ZooKeeper`. toleradas. 

## Solução

Como referido, a nossa implementação base tolera *f* faltas silenciosas/crash quando o grau de replicação é *2f+1*. Isto acontece porque tanto nas operações de escrita como de leitura, é necessário obter apenas resposta de um quórum de réplicas (de tamanho *f+1*). Quando o número destas faltas ultrapassa o *f*, a chamada remota não poderá ser concluída com sucesso, caso contrário a coerência sequencial seria prejudicada. Assim, se tal acontecer, o nosso `hub` apercebe-se disso e consulta o ZooKeeper para verificar se houve mudança do *ip* e/ou *port* de uma ou mais réplicas e tenta executar a operação de novo. Isto repetir-se-à até que a operação seja concluída (ou até que o `hub` seja terminado pelo utilizador).

<!-- _(Figura da solução de tolerância a faltas)_
 -->
<!-- _(Breve explicação da solução, suportada pela figura anterior)_
 -->

## Protocolo de replicação

_(Explicação do protocolo)_
Segue-se uma abordagem de replicação ativa, implementando uma variante do protocolo registo distribuído coerente para coordenar as leituras e escritas concorrentes nas réplicas.

Cada réplica guarda o valor do registo e uma tag composta por um seqNumber (número de sequência da escrita que deu origem à versão) e um clientID (identificador do cliente que escreveu essa versão). Uma tag é mais recente do que outra se o seu seqNumber for maior ou, caso os seqNumber sejam iguais, se o seu clientID for maior. É de notar que, devido ao `hub` não ser replicado, o clientID é sempre o mesmo. No entanto, a nossa implementação tem em conta o clientID de forma a prever uma futura possibilidade de replicar o `hub`.

Para que a coerência sequencial seja garantida, a nossa versão base conta com um quórum de *f+1* réplicas, tanto para escritas como para leituras (sendo *f* o número de faltas silenciosas toleradas). Desta forma, garante-se que os quóruns de escrita e de leitura têm pelo menos uma réplica em comum.

Para o `hub` não ficar bloqueado à espera das respostas do rec, as chamadas remotas são assíncronas. Isto é feito com recurso a um stub não bloqueante. Sendo assim, na chamada é passado um objeto do tipo StreamObserver, o ResponseObserver, que se comporta por um lado como um **ResponseCollector** permitindo receber os resultados da chamada e ao mesmo tempo, como **objeto de callback** que é chamado quando se recebe uma resposta utilizando os métodos onNext, onError e onCompleted. 

Em relação à possibilidade de fazer um *writeback*, ainda que o `hub` seja um servidor gRPC *multi-threaded*, este é sincrozinado não permitindo que haja duas operações (escritas ou leituras) concorrentes no mesmo registo. Como se assume que o `hub` não falha durante o processamento de pedidos remotos, então não há operações inacabadas - nunca se dá o caso de uma escrita ter sido feita apenas num número de réplicas inferior ao quórum e portanto duas leituras seguidas nunca serão incoerentes.  Deste modo, a operação de *writeback* proposta no protocolo de registo coerente não é necessária na nossa implementação. 

<!-- _(descrição das trocas de mensagens)_
 -->
### Leituras
- Cliente (Hub)
    1. Cliente envia um `read()` para todas as réplicas.
    2. Aguarda pelas respostas dos servidores. Aqui podem ocorrer várias situações:
        - É recebida uma exceção lógica (por exemplo, os argumentos enviados no pedido serem inválidos). Neste caso, o stream observer notifica imediatamente a *thread* principal para que o erro seja reportado. Como as verificações feitas pelas réplicas são todas iguais, assume-se que nenhuma réplica terá executado a operação. 
        - Foi obtido um quórum de respostas bem-sucedidas ou uma resposta/erro de todas as réplicas. Em qualquer um dos casos, se foi obtido pelo menos um erro de comunicação com os servidores (`UNAVAILABLE` por não ter sido possível comunicar com esse servidor ou `DEADLINE_EXCEEDED` por ter ocorrido um *timeout* antes de uma resposta ter sido recebida) é chamada a função `rebuildReplicas()`. Esta função consiste em solicitar ao *ZooKeeper* os endereços de todas as réplicas conhecidas (através da função `lookUp(replica_path)`). Independentemente de ser ou não preciso chamar a função `rebuildReplicas()`, podem ocorrer dois casos:
            - Um quórum de pelo menos *f+1* respostas foi atingido e a operação dá-se por terminada
            - O número de erros é demasiado grande, não se tendo atingido um quórum. Neste caso é necessário repetir a operação de leitura (voltar ao ponto 1).
    3. Retorna o valor que recebeu associado à maior tag.

- Réplica (Rec)
    1. Cada réplica ao receber um `read()` confirma que os argumentos são válidos. Se sim, responde com o valor do registo e a tag. Se não, envia uma exceção.

### Escritas
- Cliente (Hub)
    1. Cliente faz uma leitura a todas as réplicas para descobrir a maior tag (ver [leituras](#leituras))
    2. Cria uma nova tag com o *maxSeqNumber + 1* e o seu *clientID*.
    3. Envia um `write()` com o valor do registo e a nova tag a todas as réplicas.
    4. Espera por resposta de confirmação de um quórum (ou volta a repetir a operação até que tal aconteça). Aqui podem ocorrer os casos semelhantes ao ponto **2** das leituras do Cliente (Hub), visto que a classe `StreamObserver` é a mesma.
    5. Retorna confirmação ao cliente

- Réplica (Rec)
    1. Recebe um `write()` com um valor e uma tag.
    2. Confirma se os argumentos da mensagem de pedido são corretos. Em caso afirmativo, prossegue. Em caso negativo, retorna uma exceção e não conclui a operação.
    3. Se a tag recebida for maior (*newSeqNum* > *oldSeqNum* ou *newSeqNum* == *oldSeqNum* && *newCID* > *oldCID*), atualiza o valor do registo e da tag com os recebidos.
    4. Quer tenha ou não atualizado o valor, responde com uma confirmação. 

## Medições de desempenho

**Rascunhooooo**

Por cada escrita, ha' uma leitura para obter o numero de sequencia maximo 
Por cada operacao, supondo que correm sem excecoes lancadas, tem-se:

- balance: 1 leitura
- top-up: 1 leitura (getBalance()), 1 escrita (setBalance()) 
- infoStation: 3 leituras (getNBikes(), getNPickUps(), getNDeliviries())
- locateStation: 0 leituras e 0 escritas
- bikeUp: 4 leituras (getBalance(), getNBikes(), getNPickUps(), getOnBike()) e 4 escritas (setNBikes(), setNPickUps(), setBalance(), setOnBike())
- bikeDown: 4 leituras (getBalance(), getNBikes(), getNDeliveries(), getOnBike()) e 4 escritas (setNBikes(), setNDeliveries(), setBalance(), setOnBike())


<!-- _(Tabela-resumo)_
 -->

---

 Um hub sem correr initRec, 3 Recs (todos up), correr app com `demo/commandsPerformance.txt`
 ```
 $$$
        Performance Logger Results:
$$$

    **Reads**
Number of reads: 712
Average time taken: 5.455056179775281
Values: [22, 9, 21, 18, 10, 14, 7, 7, 8, 11, 7, 12, 9, 16, 14, 15, 13, 5, 7, 8, 6, 9, 8, 5, 9, 8, 9, 5, 8, 7,
12, 9, 8, 6, 5, 5, 9, 5, 9, 7, 6, 4, 8, 9, 4, 4, 5, 4, 7, 11, 11, 4, 7, 8, 5, 12, 6, 4, 13, 7, 7, 4, 4, 6, 5,
5, 10, 11, 6, 9, 5, 5, 6, 7, 4, 5, 7, 5, 5, 10, 5, 22, 6, 4, 8, 7, 6, 9, 5, 7, 3, 8, 5, 3, 11, 5, 4, 6, 8, 5,
5, 9, 9, 4, 5, 7, 11, 4, 4, 6, 5, 19, 5, 4, 8, 8, 3, 4, 5, 5, 3, 3, 8, 6, 3, 3, 9, 4, 8, 8, 5, 7, 5, 3, 4, 4,
6, 5, 4, 3, 6, 7, 4, 3, 8, 7, 4, 5, 6, 4, 3, 4, 6, 4, 4, 7, 4, 6, 9, 4, 3, 3, 7, 4, 3, 5, 7, 12, 8, 10, 4, 7,
8, 8, 5, 8, 7, 4, 4, 7, 8, 5, 4, 4, 5, 5, 3, 4, 7, 9, 4, 5, 5, 10, 5, 4, 4, 12, 5, 3, 3, 5, 7, 2, 3, 10, 5, 4, 4, 11, 6, 5, 4, 9, 9, 7, 8, 5, 7, 3, 3, 3, 6, 3, 3, 4, 5, 5, 3, 3, 5, 4, 10, 5, 3, 4, 6, 3, 4, 5, 6, 4, 3, 3, 3, 3, 3, 7, 3, 7, 11, 3, 4, 10, 3, 5, 4, 4, 4, 9, 8, 7, 5, 10, 6, 3, 3, 4, 5, 4, 4, 8, 6, 6, 7, 13, 8, 6, 8,
3, 4, 6, 7, 10, 4, 4, 6, 5, 7, 5, 3, 8, 5, 3, 7, 6, 11, 4, 7, 15, 9, 4, 3, 4, 5, 5, 7, 3, 3, 5, 11, 4, 7, 7, 5, 7, 11, 7, 3, 9, 5, 6, 8, 9, 4, 7, 4, 5, 4, 10, 4, 3, 4, 3, 5, 4, 3, 4, 4, 5, 8, 3, 2, 3, 3, 5, 5, 8, 10, 5,
4, 3, 8, 6, 4, 4, 5, 3, 3, 14, 14, 21, 5, 13, 8, 5, 6, 6, 6, 5, 7, 6, 5, 5, 8, 6, 4, 6, 4, 5, 5, 7, 6, 4, 6, 6, 4, 4, 8, 6, 16, 4, 4, 14, 9, 4, 5, 8, 8, 4, 4, 7, 4, 5, 5, 11, 2, 4, 4, 3, 8, 5, 4, 7, 4, 10, 4, 3, 10, 5, 5, 7, 3, 3, 10, 5, 5, 3, 12, 9, 3, 3, 16, 4, 4, 5, 14, 3, 2, 3, 5, 6, 4, 8, 9, 9, 3, 4, 5, 4, 3, 6, 3, 3, 4, 7, 6, 7, 4, 3, 3, 5, 4, 2, 3, 4, 3, 3, 2, 3, 3, 3, 6, 3, 3, 3, 4, 3, 6, 3, 2, 3, 2, 6, 4, 4, 3, 10, 10, 3, 3, 15, 8, 4, 10, 5, 4, 4, 4, 2, 3, 7, 7, 2, 3, 4, 8, 6, 3, 3, 4, 4, 8, 3, 5, 6, 4, 3, 4, 7, 7, 4, 7, 4, 7, 4, 4, 7, 3, 4, 5, 4, 3, 4, 4, 4, 6, 3, 2, 2, 8, 3, 4, 7, 5, 2, 3, 4, 4, 7, 5, 3, 3, 4, 7, 3, 3, 4, 4, 3, 2, 3, 4, 3, 4, 3, 4, 3, 6, 6, 4, 2, 3, 3, 3, 3, 3, 3, 3, 4, 6, 6, 5, 5, 3, 5, 7, 2, 3, 8, 4, 6, 5, 8, 6, 3, 3, 6, 5, 4, 2,
3, 3, 3, 3, 5, 3, 3, 3, 3, 7, 3, 2, 3, 3, 7, 4, 3, 2, 2, 5, 3, 3, 3, 3, 6, 5, 4, 3, 2, 5, 4, 3, 2, 3, 2, 2, 2, 2, 4, 3, 4, 4, 2, 5, 4, 4, 2, 3, 2, 2, 7, 3, 3, 3, 6, 5, 4, 4, 3, 5, 5, 4, 3, 2, 4, 4, 9, 3, 3, 4, 4, 6, 3, 2, 2, 5, 6, 3, 3, 4, 7, 7, 6, 8, 9, 5, 5, 5, 4, 4, 3, 3, 3, 4, 4, 2, 6, 7, 3, 3, 3, 4, 3, 2, 3, 3, 3, 11, 2, 3, 4,
    **Writes**
Number of writes: 206
Average time taken: 4.849514563106796
Values: [16, 9, 5, 6, 10, 15, 12, 8, 5, 4, 10, 7, 6, 7, 5, 6, 10, 5, 4, 3, 5, 8, 5, 5, 3, 4, 5, 4, 4, 3, 4, 5, 4, 3, 10, 4, 3, 3, 7, 3, 6, 4, 2, 4, 15, 4, 4, 4, 21, 6, 4, 6, 2, 5, 4, 7, 4, 3, 4, 4, 5, 3, 4, 4, 3, 6, 4, 7, 2, 3, 12, 3, 8, 3, 4, 5, 14, 5, 6, 10, 2, 5, 4, 6, 4, 3, 8, 3, 3, 4, 4, 9, 6, 4, 8, 3, 3, 6, 3, 4, 5, 7, 8,
8, 3, 4, 5, 4, 11, 9, 5, 9, 6, 4, 4, 3, 6, 4, 3, 4, 2, 4, 10, 2, 5, 7, 11, 4, 3, 4, 3, 3, 2, 3, 3, 5, 3, 3, 3, 3, 3, 7, 3, 6, 5, 3, 12, 2, 3, 5, 3, 3, 3, 6, 4, 6, 3, 3, 4, 4, 3, 3, 3, 3, 2, 4, 4, 3, 5, 2, 8, 3, 5, 2, 2,
3, 4, 2, 2, 5, 3, 4, 2, 3, 3, 6, 3, 2, 5, 3, 4, 3, 3, 2, 4, 3, 5, 2, 5, 3, 3, 2, 2, 6, 3, 3,
$=$
        Performance Logger Results.
$=$


 ```
<!-- _(explicação)_
 -->
## Opções de implementação

Para implementar as chamadas remotas assíncronas era necessário criar um StreamObserver e um ResponseCollector. Como já foi apontado anteriormente, optou-se por criar uma única classe, chamada `ResponseObserver` que reúne as qualidades de objeto de callback e de armazenamento das respostas das várias réplicas. Cada operação usa a mesma instância para todas as réplicas, guardando nesta toda a informação. 

Foi adotada esta opção visto que, além de ter sido a primeira ideia do grupo, implica ter menos uma classe e apenas 1 objeto por cada leitura/escrita (em vez dos *2f+1* StreamObservers e *1* ResponseCollector necessários na outra opção). A opção adotada implica um maior investimento na coerência mas, sendo esta bem feita, permite obter os mesmos resultados. 

Em relação às melhorias, verificou-se que o número de leituras é muito superior ao número de escritas. Isto significa que ter um quórum menor para as operações de leitura permitiria que a nossa implementação fosse mais eficiente. Portanto, otimizando uma operação à custa da outra permite ter muitas operações de leituras baratas (mais rápidas) e poucas operações de escrita mais caras (mais lentas). É no entanto de notar que isto implica que as operações de escrita já não toleram *f* faltas mas antes *TODO* faltas (visto que é agora preciso obter resposta de *TODO* réplicas). Já as operações de leitura passam a tolerar *TODO* faltas (visto que são precisas apenas *TODO* respostas para se atingir o quórum).


<!-- _(Descrição de opções de implementação, incluindo otimizações e melhorias introduzidas)_ -->

<!-- _(Justificar as otimizações com as medições efetuadas -- antes e depois)_ -->

## Notas finais

_(Algo mais a dizer?)_

É de notar que o número de réplicas inicial do `rec` nunca pode ser *zero*. Para impedir esta situação, o `hub` não começará a sua execução enquanto não encontrar pelo menos uma réplica do `rec` registada no ZooKeeper. Isto é verdade tanto para o número de `recs` como para o número de `hubs` pelo que, da mesma forma, a App não começará a sua execução enquanto não encontrar nenhum `hub` registado no ZooKeeper.