# Build de múltiplos estágios: nada precisa estar instalado na máquina host
# além do Docker. O contexto de build é a RAIZ do repositório (necessário
# para o Maven reactor enxergar o módulo "common").
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY services services
RUN mvn -q -pl services/common,services/catalog-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/services/catalog-service/target/catalog-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
