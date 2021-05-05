# Relatório do projeto *Bicloin*

Sistemas Distribuídos 2020-2021, segundo semestre

## Autores

**Grupo A24**


| Número | Nome              | Utilizador                                   | Correio eletrónico                  |
| -------|-------------------|----------------------------------------------| ------------------------------------|
| 92437  | Catarina Gonçalves | <https://git.rnl.tecnico.ulisboa.pt/ist192437> | <mailto:catarina.g.goncalves@tecnico.ulisboa.pt>   |
| 92456  | Duarte Bento       | <https://git.rnl.tecnico.ulisboa.pt/ist192456> | <mailto:duarte.bento@tecnico.ulisboa.pt>     |
| 92560  | Susana Monteiro    | <https://git.rnl.tecnico.ulisboa.pt/ist192560> | <mailto:s.moreno.monteiro@tecnico.ulisboa.pt> |

![Catarina](ist192437.png) ![Duarte](ist192456.png) ![Susana](ist192560.png)


## Melhorias da primeira parte

_(que correções ou melhorias foram feitas ao código da primeira parte -- incluir link para commits no Git onde a alteração foi feita)_

- [Hub e Rec passam a registar-se no ZooKeeper](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/d9f77e55729f6c39ceb35eca45159849c7bc15a4)(https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/822b7d218fadbd5b35805e1917f207536f8679c0)(https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/d727f5557fc721cd2d66a3d3262d4584d0ea8743)

- [Foi criado um RecordFrontendReplicationWrapper que encontra todas as réplicas do rec e cria um RecordFrontend para cada uma delas](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/5260836765bacc04144c6764a970f64315f2e23d)

- [Foi adicionado um timeout às operações do Record Frontend](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/72d2665a6fdad0815a44c5c843d824776f7e5c2)

- [Foram atualizados os testes (do rec) para passarem a utilizar os métodos do RecordFrontendReplicationWrapper](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/7a71d562c0e41351957ec7a4a47dcc7c866a647f)

- [App passa a contactar o Hub utilizando o serviço de nomes](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/5b432fcd693fd06ddbf1deb0aa0d0c5b785404d7)

- [Passam a ser permitidas chamadas assíncronas com mecanismos de controlo de concorrência e tolerância a faltas](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/961e691c9ed6d52ace18cd9e1894a1e367606c2a)(https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/A24-Bicloin/commit/5b432fcd693fd06ddbf1deb0aa0d0c5b785404d7)

- [descrição da alteração](https://github.com/tecnico-distsys/CXX-Bicloin/commit/a70e690b3655e76a0a1e0ff1137c0cb28cfe26a7)


## Modelo de faltas

_(que faltas são toleradas, que faltas não são toleradas)_


## Solução

_(Figura da solução de tolerância a faltas)_

_(Breve explicação da solução, suportada pela figura anterior)_


## Protocolo de replicação

_(Explicação do protocolo)_

_(descrição das trocas de mensagens)_

## Medições de desempenho

_(Tabela-resumo)_

_(explicação)_

## Opções de implementação

_(Descrição de opções de implementação, incluindo otimizações e melhorias introduzidas)_

_(Justificar as otimizações com as medições efetuadas -- antes e depois)_

## Notas finais

_(Algo mais a dizer?)_
