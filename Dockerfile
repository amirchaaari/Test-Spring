# Dockerfile for Spring Boot Backend
# Build the JAR locally first with: mvn clean package

FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the pre-built JAR file
COPY target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Set environment variables (can be overridden in docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]