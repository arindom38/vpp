# Stage 1: Build the application
FROM maven:3.9-amazoncorretto-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
RUN mkdir -p /opt/tomcat/logs

# Install curl for healthcheck
RUN apk add --no-cache curl

EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=dev", "-jar", "app.jar"]