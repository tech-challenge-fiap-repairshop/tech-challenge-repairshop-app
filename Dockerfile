# ==========================================
# ESTÁGIO 1: BUILD (Compilação)
# ==========================================
FROM maven:3-eclipse-temurin-24 AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ==========================================
# ESTÁGIO 2: RUN (Execução)
# ==========================================
# Usamos apenas o JRE (Java Runtime Environment), muito mais leve!
FROM eclipse-temurin:24-jre

WORKDIR /app

# Criamos o usuário não-root
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copiamos APENAS o .jar gerado no estágio "builder" e já renomeamos
COPY --from=builder /build/target/repairshop-0.0.1-SNAPSHOT.jar app.jar

# Damos a propriedade do arquivo para o appuser
RUN chown appuser:appuser app.jar

# Trocamos para o usuário seguro
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]