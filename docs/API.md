# API do Lab Manager

Esta documentação cobre os módulos de laboratórios, calendário, reservas, aprovação,
reclamações, dashboard e relatórios. Todas as rotas abaixo usam o prefixo `/api/v1` e,
salvo login/cadastro, exigem `Authorization: Bearer <token>`.

## Configuração da agenda

A agenda usa uma única configuração central no backend. Os valores padrão são:

| Variável | Padrão | Finalidade |
| --- | --- | --- |
| `LAB_MANAGER_TIME_ZONE` | `America/Sao_Paulo` | Fuso usado para data atual e antecedência |
| `LAB_MANAGER_OPENING_TIME` | `07:30` | Início do expediente |
| `LAB_MANAGER_CLOSING_TIME` | `18:00` | Fim do expediente |
| `LAB_MANAGER_SLOT_MINUTES` | `30` | Duração dos slots do calendário |
| `LAB_MANAGER_MINIMUM_ADVANCE_HOURS` | `72` | Antecedência mínima, em horas reais |

Os intervalos são semiabertos: `[início, fim)`. Assim, `10:00–11:00` e
`11:00–12:00` são adjacentes e não conflitam. Uma reserva deve estar dentro do
expediente, ter início anterior ao fim, respeitar a capacidade do laboratório e ser
criada com pelo menos 72 horas reais de antecedência.

## Laboratórios

| Método e rota | Acesso | Descrição |
| --- | --- | --- |
| `POST /laboratorios` | Administração | Cria laboratório |
| `GET /laboratorios` | Autenticado | Lista e filtra, com paginação |
| `GET /laboratorios/{id}` | Autenticado | Obtém detalhes |
| `PATCH /laboratorios/{id}` | Administração | Atualiza parcialmente |
| `DELETE /laboratorios/{id}` | Administração | Exclui se não houver referência impeditiva |
| `GET /laboratorios/{id}/calendario?data=AAAA-MM-DD` | Autenticado | Gera os slots e sua ocupação |

Filtros de listagem: `capacidadeMinima`, `localizacao` e `recursos`. Recursos repetidos
na query representam condição **E**: o laboratório precisa possuir todos eles. A
capacidade é comparada por `>=`; localização não diferencia maiúsculas/minúsculas.
Também são aceitos os parâmetros padrão de página `page`, `size` e `sort`.

Exemplo de criação:

```json
{
  "nome": "Laboratório 01",
  "capacidade": 40,
  "quantidadeComputadores": 30,
  "status": "DISPONIVEL",
  "tipoLaboratorio": "INFORMATICA",
  "localizacao": "Bloco A",
  "recursos": ["PROJETOR", "AR CONDICIONADO"]
}
```

O calendário é derivado das reservas `PENDENTE` e `APROVADA`; não há uma segunda
fonte de verdade persistida. Cancelamento e rejeição liberam imediatamente os slots.
Um dia livre contém 21 slots de 30 minutos, de `07:30` a `18:00`.

## Reservas

| Método e rota | Acesso | Descrição |
| --- | --- | --- |
| `POST /reservas` | Autenticado | Cria como `PENDENTE`, para o usuário do JWT |
| `GET /reservas` | Administração | Lista todas, com filtros e paginação |
| `GET /reservas/me` | Autenticado | Lista somente as próprias reservas |
| `GET /reservas/{id}` | Dono ou administração | Obtém detalhes |
| `PATCH /reservas/{id}` | Dono ou administração | Atualiza uma reserva pendente |
| `PATCH /reservas/{id}/cancelamento` | Dono ou administração | Cancela |
| `PATCH /reservas/{id}/aprovacao` | Administração | Aprova uma pendente |
| `PATCH /reservas/{id}/rejeicao` | Administração | Rejeita uma pendente |

Filtros de listagem: `laboratorioId`, `data` e `status`, além de paginação. Corpo de
criação:

```json
{
  "dataReserva": "2026-09-10",
  "horarioInicio": "10:00",
  "horarioFim": "11:00",
  "laboratorioId": "00000000-0000-0000-0000-000000000000",
  "quantidadeAlunos": 20,
  "observacao": "Aula prática"
}
```

O cliente não envia `usuarioId`, papel ou status. A identidade sempre vem do token.
Os estados são `PENDENTE`, `APROVADA`, `REJEITADA` e `CANCELADA`. Aprovação e rejeição
só partem de `PENDENTE`; estados finais não são reabertos.

A criação, alteração de horário/laboratório e aprovação usam transação
`READ_COMMITTED` e adquirem bloqueio pessimista no laboratório antes de consultar
conflito. Isso serializa decisões concorrentes e garante uma leitura atual depois da
espera pelo bloqueio, inclusive quando ainda não existe reserva na faixa. Um conflito
de horário responde `409` e não persiste uma segunda reserva.

## Reclamações

| Método e rota | Acesso | Descrição |
| --- | --- | --- |
| `POST /reclamacoes` | Autenticado | Cria como `PENDENTE`, com autor e instante do servidor |
| `GET /reclamacoes` | Administração | Lista todas, com filtros e paginação |
| `GET /reclamacoes/me` | Autenticado | Lista somente as próprias |
| `GET /reclamacoes/{id}` | Dono ou administração | Obtém detalhes |
| `PATCH /reclamacoes/{id}` | Dono ou administração | Atualiza conteúdo permitido |
| `PATCH /reclamacoes/{id}/cancelamento` | Dono ou administração | Cancela uma pendente |
| `PATCH /reclamacoes/{id}/status` | Administração | Executa transição de estado |

Filtros: `laboratorioId`, `status` e `categoria`. Corpo de criação:

```json
{
  "descricao": "Falha no projetor",
  "categoriaProblema": "EQUIPAMENTO",
  "laboratorioId": "00000000-0000-0000-0000-000000000000"
}
```

Transições administrativas válidas:

- `PENDENTE` → `EM_ANALISE`, `CANCELADA` ou `IMPROCEDENTE`;
- `EM_ANALISE` → `CONCLUIDA`, `CANCELADA` ou `IMPROCEDENTE`;
- estados finais não admitem nova transição.

## Dashboard e relatórios

Todas as rotas desta seção são administrativas.

| Método e rota | Parâmetros | Resultado |
| --- | --- | --- |
| `GET /dashboard` | — | Totais e agrupamentos por status/categoria |
| `GET /relatorios/historico` | `dataInicial`, `dataFinal` | Reservas e reclamações por status no período |
| `GET /relatorios/utilizacao` | `dataInicial`, `dataFinal` | Minutos ocupados, disponíveis e percentual por laboratório |
| `GET /relatorios/ranking-laboratorios` | `dataInicial`, `dataFinal` | Mesmos dados, ordenados por ocupação decrescente |

As consultas fazem agregação no banco. `PENDENTE` e `APROVADA` bloqueiam a
disponibilidade, mas somente `APROVADA` conta como utilização real nos relatórios e no
ranking. Os minutos são calculados pelos horários reais; a disponibilidade diária é de
630 minutos. Laboratórios sem uso também aparecem. Empates no ranking são resolvidos
por nome e UUID para manter ordem estável.

## Respostas e erros

Criações retornam `201 Created` e cabeçalho `Location`; leituras/alterações retornam
`200 OK`; exclusão física de laboratório retorna `204 No Content`.

O formato de erro contém `status`, `message` e a lista `erro`; cada erro de campo usa
`campo` e `erro`. Códigos principais:

| Código | Uso |
| --- | --- |
| `400` | JSON, enum, parâmetro ou credencial malformada |
| `401` | Token ausente ou inválido |
| `403` | Papel ou propriedade insuficiente |
| `404` | Recurso inexistente |
| `409` | Sobreposição, transição inválida ou integridade referencial |
| `422` | Campo inválido ou regra de negócio violada |

## Contratos de entrada e saída

| Operação | Entrada | Saída |
| --- | --- | --- |
| Criar laboratório | `LaboratorioRequestDTO` | `201` + `Location` |
| Atualizar laboratório | `LaboratorioUpdateDTO` | `LaboratorioResponseDTO` |
| Listar/detalhar laboratório | Query/path | `PagedModel<LaboratorioListDTO>` / `LaboratorioResponseDTO` |
| Consultar calendário | `id` + `data` | `CalendarioResponseDTO` com `CalendarioSlotDTO` |
| Criar reserva | `ReservaRequestDTO` | `201` + `Location` |
| Atualizar reserva | `ReservaUpdateDTO` | `ReservaResponseDTO` |
| Consultar/cancelar/aprovar/rejeitar reserva | Query/path | `PagedModel<ReservaResponseDTO>` ou `ReservaResponseDTO` |
| Criar reclamação | `ReclamacaoRequestDTO` | `201` + `Location` |
| Atualizar reclamação | `ReclamacaoUpdateDTO` | `ReclamacaoResponseDTO` |
| Atualizar status da reclamação | `ReclamacaoStatusUpdateDTO` | `ReclamacaoResponseDTO` |
| Consultar reclamação | Query/path | `PagedModel<ReclamacaoResponseDTO>` / `ReclamacaoResponseDTO` |
| Dashboard | — | `DashboardResumoDTO` |
| Histórico | `dataInicial`, `dataFinal` | `HistoricoDTO` |
| Utilização/ranking | `dataInicial`, `dataFinal` | lista de `UtilizacaoLaboratorioDTO` |

## Migração de banco

`V5__support_scheduling_and_laboratory_filters.sql` adiciona localização e recursos aos
laboratórios, dados e status às reservas e índices de agenda/relatórios. Reservas já
existentes são retrocompatibilizadas como `APROVADA`, preservando a ocupação que antes
era implícita. As migrações `V1` a `V4` permanecem inalteradas.
