# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom and download dependencies (layer caching)
COPY pom.xml ./pom.xml
COPY servico-notificacao/pom.xml ./servico-notificacao/pom.xml
RUN mvn -f servico-notificacao/pom.xml dependency:go-offline -B

# Copy source code
COPY servico-notificacao/src ./servico-notificacao/src

# Build without tests
RUN mvn -f servico-notificacao/pom.xml clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy jar from build stage
COPY --from=builder /app/servico-notificacao/target/*.jar app.jar

# Create non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

EXPOSE 8084

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
