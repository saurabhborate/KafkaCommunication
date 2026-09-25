# syntax=docker/dockerfile:1.7
FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -B -pl notification-service -am package -DskipTests
FROM eclipse-temurin:26-jre
WORKDIR /app
COPY --from=build /workspace/notification-service/target/notification-service-1.0.0.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
