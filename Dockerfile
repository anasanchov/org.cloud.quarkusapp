# Usa una imagen base de OpenJDK 21 y Maven para la fase de construcción
FROM openjdk:21-slim AS build
WORKDIR /project
COPY pom.xml .
COPY . .
RUN apt-get update && apt-get install -y maven && mvn -B clean package -DskipTests

# Usa una imagen base de OpenJDK 21 para el entorno de ejecución
FROM openjdk:21-slim AS runtime
WORKDIR /project
COPY --from=build /project/target/quarkus-app ./quarkus-app
EXPOSE 8080
CMD ["java", "-jar", "quarkus-app/quarkus-run.jar"]
