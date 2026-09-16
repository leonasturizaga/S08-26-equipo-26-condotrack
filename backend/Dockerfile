# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Optional: cache dependencies better
# RUN mvn dependency:go-offline -B

RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the jar (wildcard is fine)
COPY --from=build /app/target/*.jar app.jar

# Render sets PORT at runtime – do NOT hard-code EXPOSE 8080 as the only port
EXPOSE 8080

# Force Spring Boot to use $PORT and bind to all interfaces
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080} --server.address=0.0.0.0"]
