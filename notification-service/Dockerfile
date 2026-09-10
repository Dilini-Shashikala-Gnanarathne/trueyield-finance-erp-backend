# Multi-stage build for Notification Service
# notification-service has no dependency on proto (no gRPC), so the build is simpler.
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy only what notification-service needs (no proto module required)
COPY pom.xml .
COPY notification-service/pom.xml notification-service/
COPY notification-service/src notification-service/src

RUN mvn -f notification-service/pom.xml package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /build/notification-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
