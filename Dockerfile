# Use a lightweight OpenJDK image for the runtime
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR file to the container
COPY target/cards-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8081

# Run the application with the live database configuration
ENTRYPOINT ["java", "-jar", "/app/app.jar"]