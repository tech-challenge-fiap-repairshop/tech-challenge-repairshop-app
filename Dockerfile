FROM maven:3-eclipse-temurin-24

WORKDIR /app

# Copia o pom.xml e o código fonte
COPY pom.xml .
COPY src ./src

# Realiza o build da aplicação pulando os testes
RUN mvn clean package -DskipTests

EXPOSE 8080

# O ENTRYPOINT continua rodando o .jar gerado, mas agora em um container único usando a imagem do Maven
ENTRYPOINT ["java", "-jar", "target/repairshop-0.0.1-SNAPSHOT.jar"]
