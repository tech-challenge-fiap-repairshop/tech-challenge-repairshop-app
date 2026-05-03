FROM eclipse-temurin:24-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY pom.xml ./
RUN sed -i 's/\r$//' .mvn/mvnw && chmod +x .mvn/mvnw
RUN ./.mvn/mvnw dependency:go-offline -q
COPY src src
RUN ./.mvn/mvnw package -DskipTests -q
FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/target/repairshop-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
