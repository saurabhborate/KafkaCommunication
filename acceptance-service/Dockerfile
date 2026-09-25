# syntax=docker/dockerfile:1.7
FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -B -pl acceptance-service -am package -DskipTests
FROM eclipse-temurin:26-jre
WORKDIR /app
COPY --from=build /workspace/acceptance-service/target/acceptance-service-1.0.0.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
