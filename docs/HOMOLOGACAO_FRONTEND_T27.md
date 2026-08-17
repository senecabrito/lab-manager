# Homologação frontend/backend — T27

Data da auditoria: 17/08/2026.

O repositório `../lab-manager-frontend` foi inspecionado em modo somente leitura. A
fonte de verdade do backend é `GET /v3/api-docs`; o arquivo `openapi.yaml` do frontend
descreve um contrato anterior e não corresponde à API `/api/v1` atual.

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

## Funcionalidades fora do contrato atual

As telas ainda chamam `/reservas/recomendacao`, `/inventario`, `/acessos/checkin` e
`/acessos/checkout`. Essas rotas não existem no backend auditado. Classificação:
**DIVERGÊNCIA NO BACKEND em relação às telas legadas**, sem implementação na T27 por
estarem fora do contrato consolidado e exigirem novas regras de negócio.

## Ações pendentes no frontend

1. Configurar a base para incluir host/porta do backend e o prefixo `/api/v1`.
2. Gerar ou adaptar o cliente a partir de `/v3/api-docs`.
3. Migrar rotas, verbos, enums, campos e paginação conforme a matriz.
4. Obter o perfil em `/usuarios/me` depois do login, pois o token não inclui objeto
   `usuario` na resposta HTTP.
5. Implementar estados de carregamento, vazio e erro por tela.
6. Decidir em tarefa própria o destino das telas de recomendação, inventário e acesso.
