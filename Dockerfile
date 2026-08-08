# ==========================================
# Stage 1: Build (Maven + JDK)
# ==========================================
FROM maven:3-eclipse-temurin-24 AS builder
WORKDIR /app

# Copia arquivos e empacota a aplicação
COPY pom.xml .
COPY src ./src
RUN mvn clean package -B -ntp -DskipTests

# Baixa o agente OpenTelemetry para instrumentação automática
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

# ==========================================
# Stage 2: Runtime (JRE minimal)
# ==========================================
FROM eclipse-temurin:24-jre

# Atualiza pacotes do SO para mitigar vulnerabilidades do Ubuntu (Trivy)
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia apenas o JAR compilado do builder e o agente OTel
COPY --from=builder /app/target/repairshop-0.0.1-SNAPSHOT.jar app.jar
COPY --from=builder /app/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

# Configuração DevSecOps: usar usuário não-root
RUN useradd -m springuser
USER springuser

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:opentelemetry-javaagent.jar", "-jar", "app.jar"]
