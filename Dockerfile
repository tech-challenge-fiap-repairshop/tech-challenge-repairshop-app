# ==========================================
# Stage 1: Build (Maven + JDK)
# ==========================================
FROM maven:3-eclipse-temurin-24 AS builder
WORKDIR /app

# Copia arquivos e empacota a aplicação
COPY pom.xml .
COPY src ./src
RUN mvn clean package -B -ntp -DskipTests

# Baixa o agente OpenTelemetry oficial usando curl (seguindo redirects do GitHub)
RUN curl -fsSL -o /app/opentelemetry-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# ==========================================
# Stage 2: Runtime (JRE minimal)
# ==========================================
FROM eclipse-temurin:24-jre

# Atualiza pacotes do SO para mitigar vulnerabilidades do Ubuntu (Trivy)
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Cria usuário não-root (UID 10001 padronizado com Kubernetes SecurityContext)
RUN useradd -m -u 10001 springuser

# Copia os JARs compilados com ownership explícito para o usuário não-root
COPY --from=builder --chown=springuser:springuser /app/target/repairshop-0.0.1-SNAPSHOT.jar /app/app.jar
COPY --from=builder --chown=springuser:springuser /app/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Garante permissão de leitura para o agente e a aplicação
RUN chmod 644 /app/app.jar /app/opentelemetry-javaagent.jar

USER springuser

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "/app/app.jar"]
