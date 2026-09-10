# Multi-stage build for API Gateway
# Stage 1: Build with Maven
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy parent POM and proto module first (leverages Docker layer caching)
COPY pom.xml .
COPY proto/pom.xml proto/
COPY proto/src proto/src

# Copy api-gateway module
COPY api-gateway/pom.xml api-gateway/
COPY api-gateway/src api-gateway/src

# Build proto module first, then api-gateway
RUN mvn -f proto/pom.xml install -DskipTests --no-transfer-progress
RUN mvn -f api-gateway/pom.xml package -DskipTests --no-transfer-progress

# Stage 2: Runtime image — use smaller JRE image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /build/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
