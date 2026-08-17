# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-alpine@sha256:1ff763083f2993d57d0bf374ab10bb3e2cb873af6c13a04458ebbd3e0337dc76 AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && ./mvnw -B -ntp dependency:go-offline

COPY src/ src/

RUN ./mvnw -B -ntp package -DskipTests


FROM eclipse-temurin:21-jre-alpine@sha256:3f08b13888f595cc49edabea7250ba69499ba25602b267da591720769400e08c AS runtime

RUN apk add --no-cache --upgrade libexpat p11-kit p11-kit-trust \
    && addgroup -S app \
    && adduser -S -G app -h /app app

WORKDIR /app

COPY --from=build --chown=app:app /workspace/target/*.jar app.jar

USER app:app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
