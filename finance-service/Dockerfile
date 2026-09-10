# Multi-stage build for Finance Service
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY proto/pom.xml proto/
COPY proto/src proto/src
COPY finance-service/pom.xml finance-service/
COPY finance-service/src finance-service/src

RUN mvn -f proto/pom.xml install -DskipTests --no-transfer-progress
RUN mvn -f finance-service/pom.xml package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /build/finance-service/target/*.jar app.jar

EXPOSE 8082 9090

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
