# ==============================================================================
# ExamSeat AI - Multi-Stage Production Dockerfile
# Compatible with Render, Railway, Fly.io, Koyeb, AWS, GCP, Azure
# ==============================================================================

# Stage 1: Build the Spring Boot application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Alpine JRE runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/examseat-ai-1.0.0.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]
