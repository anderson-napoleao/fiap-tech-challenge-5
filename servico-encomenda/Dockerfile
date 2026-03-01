# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom and download dependencies (layer caching)
COPY pom.xml ./pom.xml
COPY servico-encomenda/pom.xml ./servico-encomenda/pom.xml
RUN mvn -f servico-encomenda/pom.xml dependency:go-offline -B

# Copy source code
COPY servico-encomenda/src ./servico-encomenda/src

# Build without tests
RUN mvn -f servico-encomenda/pom.xml clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy executable jar from build stage
COPY --from=builder /app/servico-encomenda/target/*-exec.jar app.jar

# Create non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

EXPOSE 8083

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
