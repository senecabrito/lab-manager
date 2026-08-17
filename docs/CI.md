# CI do backend no Azure DevOps

O arquivo `azure-pipelines.yml` define a CI exclusiva do backend. Ele usa o repositorio
GitHub da propria pipeline como `self`, dispara em commits e pull requests destinados a
`main` e executa em agente Microsoft hospedado `ubuntu-latest`.

## Fluxo

A pipeline mantem as validacoes no mesmo job para reutilizar o JAR produzido por
`./mvnw clean verify` e impedir publicacao antes de todos os gates:

1. seleciona Java 21 e valida o Maven Wrapper;
2. restaura `~/.m2/repository`, cuja chave inclui o SO, `pom.xml` e a configuracao do
   wrapper;
3. executa `./mvnw clean verify` e publica os XMLs JUnit do Surefire/Failsafe;
4. sobe MySQL 8.4 descartavel, aguarda o healthcheck, inicia a API e confirma Flyway
   V1-V7, Hibernate `validate` e `GET /v3/api-docs`;
5. executa SpotBugs e Trivy sobre dependencias e secrets do repositorio;
6. constroi a imagem com tags baseadas em `Build.BuildId` e no commit;
7. executa Trivy na imagem;
8. somente depois dos scans, publica JAR, imagem exportada e relatorios.

Qualquer falha de compilacao, teste, startup, migration, analise estatica, dependencia,
secret, build Docker ou scan bloqueia os artefatos finais. Os relatorios de seguranca
ja gerados sao publicados tambem em execucoes com falha para permitir diagnostico; isso
nao torna o gate nao bloqueante.

## Banco e configuracao sensivel

O banco usa `mysql:8.4`, porta dinamica e `tmpfs`; nenhum volume persistente e montado.
Senha do banco, senha de root e `JWT_KEY` sao geradas aleatoriamente em memoria para cada
execucao e nunca sao impressas. Portanto, a pipeline atual nao exige secret variables.
O cleanup e executado pelo script e novamente por uma etapa `always()` idempotente.

Nao configure a CI para usar `DB_URL`, `DB_PASSWORD` ou `JWT_KEY` de desenvolvimento,
staging ou producao. Caso um servico externo seja adicionado no futuro, seus valores
devem vir de secret variables, variable groups ou service connections.

## Scanners e politica

- SpotBugs Maven Plugin `4.10.3.0`: o relatorio usa esforco maximo e threshold
  `Default`; o gate usa `High`. Na auditoria inicial foram encontrados 41 achados de
  prioridade 2 e nenhum de prioridade 1. Os achados medios continuam no XML/SARIF,
  enquanto novos ou existentes de prioridade alta bloqueiam a pipeline.
- Trivy `0.72.0`: o binario oficial e baixado com versao fixa e validado pelo checksum
  oficial antes da execucao.
- O Maven Wrapper preenche `~/.m2/repository` antes do Trivy. Os scans Java usam
  `--offline-scan`, evitando rate limit do Maven Central sem congelar a base de CVEs,
  que continua sendo atualizada e cacheada separadamente pelo Trivy.
- Dependencias e imagem: findings `HIGH` ou `CRITICAL` corrigiveis falham a pipeline.
  Findings sem correcao permanecem no relatorio para tratamento, mas nao sao ocultados.
- Secrets: findings `HIGH` ou `CRITICAL` falham a pipeline. O relatorio bruto de secrets
  nao e publicado para evitar reproduzir um valor sensivel no artefato.

Nao havia JaCoCo ou outra ferramenta de cobertura configurada no projeto. A T34 nao
introduz percentual ou gate arbitrario; a cobertura pode ser adotada posteriormente
quando houver uma meta acordada.

Na homologacao inicial, o Trivy encontrou CVE-2026-56408 em `libexpat` e CVE-2026-2100
em `p11-kit`/`p11-kit-trust`, todas HIGH e com versao corrigida disponivel. O Dockerfile
foi atualizado para instalar as correcoes do repositorio Alpine; nenhuma suppression foi
criada.

## Artefatos

- `backend-jar`: JAR executavel e `build-info.txt` com run e commit;
- `backend-image`: imagem Docker em `.tar` e metadados de imagem/run;
- `security-reports`: SpotBugs, vulnerabilidades do filesystem/dependencias, imagem e
  versao do Trivy.

As tags da imagem sao `lab-manager-api:<Build.BuildId>` e
`lab-manager-api:<12 primeiros caracteres do commit>`. Nao e criada tag `latest`.

## Registry opcional

Nenhum registry ou service connection foi identificado, portanto a pipeline nao faz
push e publica `backend-image` como Pipeline Artifact. Para habilitar push futuramente:

1. criar ou selecionar o registry real;
2. criar uma Docker Registry service connection com apenas permissoes de push/pull;
3. autorizar explicitamente a pipeline a usa-la;
4. adicionar login e push somente depois do scan da imagem;
5. registrar repository, tags e digest no resumo do run.

Nao conceda permissao de owner da subscription apenas para publicar uma imagem.

## Configuracao no Azure DevOps

1. conectar a organizacao/projeto Azure DevOps ao repositorio GitHub do backend;
2. criar uma pipeline do tipo YAML apontando para `/azure-pipelines.yml`;
3. autorizar o acesso ao repositorio `self` e as tasks oficiais;
4. executar a primeira run e revisar Tests, logs e Pipeline Artifacts;
5. configurar branch protection/status checks no GitHub para exigir a validacao de PR.

Nao ha deploy, checkout do frontend, ACR inventado ou credencial Azure nesta pipeline.
