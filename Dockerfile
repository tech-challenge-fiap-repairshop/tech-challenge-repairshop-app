# ==========================================
# Stage 1: Build (Maven + JDK)
# ==========================================
FROM maven:3-eclipse-temurin-24 AS builder
WORKDIR /app

# Copia arquivos e empacota a aplicação
COPY pom.xml .
COPY src ./src
RUN mvn clean package -B -ntp -DskipTests

# ==========================================
# Stage 2: Runtime (JRE minimal)
# ==========================================
FROM eclipse-temurin:24-jre

WORKDIR /app

# Copia apenas o JAR compilado do builder
COPY --from=builder /app/target/repairshop-0.0.1-SNAPSHOT.jar app.jar

# Configuração DevSecOps: usar usuário não-root
RUN useradd -m springuser
USER springuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
