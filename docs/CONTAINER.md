# Container da API

O `Dockerfile` da raiz usa build multi-stage com Java 21, a versão declarada no
`pom.xml`:

- build: `eclipse-temurin:21-jdk-alpine`, fixada por digest;
- runtime: `eclipse-temurin:21-jre-alpine`, fixada por digest.

Os digests no `Dockerfile` tornam a base reproduzivel. A atualizacao das imagens deve
ser deliberada, com novo build e homologacao, em vez de acontecer silenciosamente.

O estágio de build usa exclusivamente o Maven Wrapper do repositório. Primeiro copia
`pom.xml`, `mvnw` e `.mvn/` e executa `dependency:go-offline`; o código-fonte é copiado
depois para preservar o cache de dependências. O empacotamento executa
`./mvnw -B -ntp package -DskipTests` porque a sequência prevista para CI é:

```text
./mvnw clean verify
docker build -t lab-manager-api .
```

Os testes não foram removidos ou desabilitados no projeto; apenas não são repetidos
dentro do build da imagem.

## Build e execução

```bash
docker build -t lab-manager-api .
docker run --rm -p 8080:8080 \
  -e DB_URL=jdbc:mysql://mysql:3306/lab_manager \
  -e DB_USERNAME=usuario_do_ambiente \
  -e DB_PASSWORD=senha_do_ambiente \
  -e JWT_KEY=chave_forte_do_ambiente \
  -e JWT_EXPIRATION=900000 \
  -e CORS_ALLOWED_ORIGINS=https://frontend.exemplo \
  lab-manager-api
```

O hostname `mysql` no exemplo representa um container/servidor externo na mesma rede
Docker. `localhost` dentro da API apontaria para a própria API. O compose existente em
`docker/docker-compose.yml` continua exclusivo para o banco e não foi alterado.

Também podem ser fornecidas as configurações de agenda existentes:

- `LAB_MANAGER_TIME_ZONE`;
- `LAB_MANAGER_OPENING_TIME`;
- `LAB_MANAGER_CLOSING_TIME`;
- `LAB_MANAGER_SLOT_MINUTES`;
- `LAB_MANAGER_MINIMUM_ADVANCE_HOURS`.

Flyway permanece no lifecycle do Spring Boot e aplica V1–V7 antes do Hibernate
validar o schema. A imagem não executa migrations durante o build. Os logs continuam
em stdout/stderr e a aplicação roda como o usuário não privilegiado `app`.

Não foi adicionado `HEALTHCHECK`: a aplicação ainda não possui endpoint dedicado e
público de saúde, e endpoints de negócio/documentação não devem cumprir esse papel.
