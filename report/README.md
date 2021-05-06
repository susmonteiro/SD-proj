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

_(que correções ou melhorias foram feitas ao código da primeira parte -- incluir link para commits no Git onde a alteração foi feita)_

**TODO**
- melhorar o guia
- introduzir link na operacao `at` da app
- utf-8 nos testes

- [Hub e Rec passam a registar-se no ZooKeeper](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/d9f77e55729f6c39ceb35eca45159849c7bc15a4)(https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/822b7d218fadbd5b35805e1917f207536f8679c0)(https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/d727f5557fc721cd2d66a3d3262d4584d0ea8743)

- [Foi criado um RecordFrontendReplicationWrapper que encontra todas as réplicas do rec e cria um RecordFrontend para cada uma delas](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/5260836765bacc04144c6764a970f64315f2e23d)

- [Foi adicionado um timeout às operações do Record Frontend](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/72d2665a6fdad0815a44c5c843d824776f7e5c2)

- [Foram atualizados os testes (do rec) para passarem a utilizar os métodos do RecordFrontendReplicationWrapper](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/7a71d562c0e41351957ec7a4a47dcc7c866a647f)

- [App passa a contactar o Hub utilizando o serviço de nomes](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/5b432fcd693fd06ddbf1deb0aa0d0c5b785404d7)

- [Passam a ser permitidas chamadas assíncronas com mecanismos de controlo de concorrência e tolerância a faltas](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/961e691c9ed6d52ace18cd9e1894a1e367606c2a)(https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/5b432fcd693fd06ddbf1deb0aa0d0c5b785404d7)



## Modelo de faltas

_(que faltas são toleradas, que faltas não são toleradas)_

Sendo a nossa implementação baseada num protocolo de registo distribuído, adotou-se um modelo de faltas semelhante ao deste protocolo. Assim assume-se que:
- o sistema é assíncrono (tanto a comunicação como o processamento podem demorar um tempo arbitrário)
- a comunicação é fiável (as mensagens são sempre recebidas desde que nem o cliente nem o servidor falhem), mas a ordem de chegada das mensagens não é necessariamente igual à ordem de envio das mesmas
- podem ocorrer faltas silenciosas/crash, mas nunca faltas bizantinas
- para N réplicas, no máximo N/2 - 1 réplicas falham

Dito de outra forma, a nossa implementação tolera *f* faltas silenciosas/crash quando o grau de replicação é *2f+1*. Quando o número destas faltas ultrapassa o *f*, a chamada remota não poderá ser concluída com sucesso, caso contrário a coerência sequencial seria prejudicada. Assim, se tal acontecer, o nosso hub apercebe-se disso e consulta o ZooKeeper para verificar se houve mudança do *ip* e/ou *port* de uma ou mais réplicas e tenta executar a operação de novo. Para que não fique indefinidamente em ciclo, após 5 tentativas é lançada uma exceção e o hub desiste desta operação.

É de notar que o número de réplicas inicial do Rec nunca pode ser *zero*. Para impedir esta situação, o hub não começará a sua execução enquanto não encontrar pelo menos uma réplica do Rec registada no ZooKeeper. Isto é verdade tanto para o número de Recs como para o número de Hubs pelo que, da mesma forma, a App não começará a sua execução enquanto não encontrar nenhum Hub registado no ZooKeeper.


## Solução

_(Figura da solução de tolerância a faltas)_

_(Breve explicação da solução, suportada pela figura anterior)_


## Protocolo de replicação

_(Explicação do protocolo)_
Segue-se uma abordagem de replicação ativa, implementando uma variante do protocolo registo distribuído coerente para coordenar as leituras e escritas concorrentes nas réplicas.

Cada réplica guarda o valor do registo e uma tag composta por um seqNumber (número de sequência da escrita que deu origem à versão) e um clientID (identificador do cliente que escreveu essa versão). Uma tag é mais recente do que outra se o seu seqNumber for maior ou, caso os seqNumber sejam iguais, se o seu clientID for maior. É de notar que, devido ao hub não ser replicado, o clientID é sempre o mesmo. No entanto, a nossa implementação tem em conta o clientID de forma a prever uma futura possibilidade de replicar o hub.

Para que a coerência sequencial seja garantida, a nossa versão base conta com um quórum de N/2 + 1 réplicas, tanto para escritas como para leituras (sendo N o número de réplicas total). Desta forma, garante-se que os quóruns de escrita e de leitura têm pelo menos uma réplica em comum.

Para o hub não ficar bloqueado à espera das respostas do rec, as chamadas remotas são assíncronas. Isto é feito com recurso a um stub não bloqueante. Sendo assim, na chamada é passado um objeto do tipo StreamObserver, o ResponseObserver, que se comporta por um lado como um **ResponseCollector** permitindo receber os resultados da chamada e ao mesmo tempo, como **objeto de callback** que é chamado quando se recebe uma resposta utilizando os métodos onNext, onError e onCompleted. 
 
_(descrição das trocas de mensagens)_

**Leituras**
1. Cliente
1.1 Cliente envia um read() para todas as réplicas.
1.2 Aguarda por respostas de um quórum (N/2 + 1 respostas).
1.3 Retorna o valor que recebeu associado à maior tag.

2. Réplica 
2.1 Cada réplica ao receber um read() responde com o valor do registo e a tag.

**Escritas**
1. Cliente
1.1 Cliente faz uma leitura a todas as réplicas para descobrir a maior tag.
1.2 Cria uma nova tag com o seqNumber + 1 e o seu clientID.
1.3 Envia um write com o valor do registo e a nova tag a todas as réplicas.
1.4 Espera por resposta de confirmação de um quórum
1.5 Retorna confirmação ao cliente

2. Réplica
2.1 Ao receber um write com um valor e uma tag.
2.2 Se a tag recebida for maior, atualiza o valor do registo e da tag com os recebidos.
2.3 Responde com uma confirmação 

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


_(Tabela-resumo)_

_(explicação)_

## Opções de implementação

Para implementar as chamadas remotas assíncronas era necessário criar um StreamObserver e um ResponseCollector. Como já foi apontado anteriormente, optou-se por criar uma única classe, chamada `ResponseObserver` que reúne as qualidades de objeto de callback e de armazenamento das respostas das várias réplicas. Cada operação usa a mesma instância para todas as réplicas, guardando nesta toda a informação. 

**TODO - razoes para a opcao anterior**
- primeira ideia
- menos uma classe
- funciona na mesma desde que a coerencia seja bem feita


_(Descrição de opções de implementação, incluindo otimizações e melhorias introduzidas)_

_(Justificar as otimizações com as medições efetuadas -- antes e depois)_

## Notas finais

_(Algo mais a dizer?)_
