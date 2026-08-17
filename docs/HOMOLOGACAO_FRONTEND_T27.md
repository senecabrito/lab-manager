# Homologação frontend/backend — T27

Data da auditoria: 17/08/2026.

O repositório `../lab-manager-frontend` foi inspecionado em modo somente leitura. A
fonte de verdade do backend é `GET /v3/api-docs`; o arquivo `openapi.yaml` do frontend
descreve um contrato anterior e não corresponde à API `/api/v1` atual.

## Auditoria A/B/C/D após a complementação funcional

| Fluxo frontend | Chamada atual | Situação backend | Classe | Decisão |
| --- | --- | --- | --- | --- |
| Login | `POST /autenticacao/login` com matrícula | Login oficial por email e senha | A | Manter backend; migrar payload e prefixo no frontend |
| Logout | `POST /autenticacao/logout` | JWT stateless não mantém sessão de servidor | D | Remover chamada e limpar token no cliente |
| Perfil | GET/PUT `/autenticacao/perfil` | GET/PATCH `/api/v1/usuarios/me` | C | Consumir equivalente oficial |
| Usuários | `GET /usuarios` e envelopes antigos | CRUD oficial paginado, com listagem mínima B03 | C | Adaptar `PagedModel` e DTOs |
| Laboratórios | CRUD `/labs` com PUT | CRUD `/api/v1/laboratorios` com PATCH | C | Migrar rota, verbo e DTO |
| Calendário | `GET /calendario` global | Calendário por laboratório | C | Selecionar laboratório e usar rota oficial |
| Reservas | CRUD antigo e GET global para professor | CRUD oficial, `/me` e ações de estado | C | Migrar rotas, verbos, campos e paginação |
| Aprovação | sufixos `/aprovar` e `/rejeitar` | `/aprovacao` e `/rejeicao` | A | Migrar sufixos no frontend |
| Reclamações | CRUD `/problemas` | CRUD/estado `/api/v1/reclamacoes` | C | Consumir equivalente oficial |
| Dashboard/relatórios | rotas e envelopes antigos | dashboard e três relatórios oficiais | C | Migrar parâmetros, rotas e respostas |
| Recomendação | `GET /reservas/recomendacao` sem critérios | `POST /api/v1/reservas/recomendacoes` estruturado | B | Funcionalidade implementada; migrar frontend |
| Inventário | `GET /inventario` | CRUD `/api/v1/inventario` | B | Funcionalidade implementada; migrar DTO/paginação |
| Check-in/out | `POST /acessos/{tipo}` com `labId` | ações orientadas por reserva e histórico | B | Funcionalidade implementada; enviar `reservaId` na rota |
| Loading/vazio/erros visuais | estado local incompleto | Não é responsabilidade HTTP do backend | D | Implementar exclusivamente no frontend |

## Classificação por fluxo

| Fluxo/tela | Classificação | Evidência e pendência |
| --- | --- | --- |
| Autenticação | **DIVERGÊNCIA NO FRONTEND** | Envia `{matricula, senha}` e espera `{token, usuario}`; o backend recebe `{email, senha}` e retorna `{token, expiresIn}`. O frontend também tenta `/autenticacao/logout`, inexistente no backend stateless. |
| Perfil | **DIVERGÊNCIA NO FRONTEND** | Usa GET/PUT `/autenticacao/perfil` e envelope `{usuario}`; o contrato oficial usa GET/PATCH `/usuarios/me` com DTO direto. |
| Usuários/professores | **DIVERGÊNCIA NO FRONTEND** | Espera `{usuarios: Usuario[]}` com `id`, `matricula` e `role`; o backend retorna `PagedModel<UsuarioMinDTO>` (`content` e `page`) e a B03 limita a listagem a `nome`/`curso`. |
| Laboratórios | **DIVERGÊNCIA NO FRONTEND** | Usa `/labs`, PUT, envelopes e campo `tipo`; o backend usa `/laboratorios`, PATCH, DTO direto/paginado e `tipoLaboratorio`. O filtro correto é `capacidadeMinima`, não `capacidade`. |
| Calendário | **DIVERGÊNCIA NO FRONTEND** | Solicita `/calendario?data=...` para todos os labs e espera `slot.status`; o backend exige `/laboratorios/{id}/calendario?data=...` e retorna `ocupado: boolean`, fuso e 21 slots. |
| Reservas | **DIVERGÊNCIA NO FRONTEND** | Professor chama GET `/reservas` (rota admin), usa PUT/DELETE e campos `data`, `inicio`, `termino`, `labId`; deve usar `/reservas/me`, PATCH/cancelamento e os nomes `dataReserva`, `horarioInicio`, `horarioFim`, `laboratorioId`. |
| Aprovação | **DIVERGÊNCIA NO FRONTEND** | Usa `/aprovar` e `/rejeitar`; o backend expõe `/aprovacao` e `/rejeicao`. |
| Reclamações | **DIVERGÊNCIA NO FRONTEND** | Usa `/problemas`, PUT/DELETE, envelopes e estados minúsculos; o backend usa `/reclamacoes`, PATCH, cancelamento/status e enums `PENDENTE`, `EM_ANALISE`, `CONCLUIDA`, `CANCELADA`, `IMPROCEDENTE`. |
| Dashboard | **DIVERGÊNCIA NO FRONTEND** | Usa `/dashboard/resumo` e chaves de contadores diferentes; o backend expõe `/dashboard` com `DashboardResumoDTO`. |
| Relatórios | **DIVERGÊNCIA NO FRONTEND** | Omite `dataInicial`/`dataFinal`, espera envelopes e usa `/labs-mais-utilizados`/`/problemas`; o backend retorna agregações diretas em `/historico`, `/utilizacao` e `/ranking-laboratorios`. |

## Pontos compatíveis

- O cliente envia `Authorization: Bearer <token>`, `Accept: application/json` e
  `Content-Type: application/json`, todos permitidos pelo CORS do backend.
- O cliente trata `204` sem tentar interpretar JSON.
- Erros do backend que contêm `message` são exibidos corretamente pelo cliente.
- Datas JSON usam ISO `YYYY-MM-DD`.

Esses pontos isolados são **COMPATÍVEIS**, mas não tornam os fluxos completos
homologáveis enquanto rotas, verbos e payloads acima permanecerem divergentes.

## Carregamento, vazio e falha

Classificação: **DIVERGÊNCIA NO FRONTEND**.

- Não foram encontrados estados explícitos de carregamento nas consultas das telas.
- Listas vazias resultam em `innerHTML` vazio após `map(...).join("")`, sem mensagem de
  estado vazio.
- Falhas chegam ao aviso genérico da página/formulário; não há tratamento visual por
  `400`, `403`, `404`, `409` ou `422`.
- `401` é tratado: a sessão local é limpa e o usuário volta ao login.

Carregamento e estado vazio são responsabilidades do frontend e nenhuma mudança foi
feita no backend para mascarar sua ausência.

## Funcionalidades adicionadas depois da T27

As telas ainda chamam `/reservas/recomendacao`, `/inventario`, `/acessos/checkin` e
`/acessos/checkout`. Recomendação, inventário e acesso foram confirmados como requisitos
reais e agora existem no contrato oficial em `POST /reservas/recomendacoes`, CRUD de
`/inventario`, `POST /reservas/{reservaId}/check-in`,
`POST /reservas/{reservaId}/check-out` e consultas de `/acessos`. As chamadas e DTOs
antigos continuam classificados como divergência do frontend; nenhum alias foi criado.

## Ações pendentes no frontend

1. Configurar a base para incluir host/porta do backend e o prefixo `/api/v1`.
2. Gerar ou adaptar o cliente a partir de `/v3/api-docs`.
3. Migrar rotas, verbos, enums, campos e paginação conforme a matriz.
4. Obter o perfil em `/usuarios/me` depois do login, pois o token não inclui objeto
   `usuario` na resposta HTTP.
5. Implementar estados de carregamento, vazio e erro por tela.
6. Migrar recomendação, inventário e acesso para os novos contratos oficiais orientados
   à reserva e para seus DTOs/paginação.
