# Use official OpenJDK base image
FROM eclipse-temurin:17-jdk-jammy
# Set working directory
WORKDIR /app
# Copy the built JAR file and all necessary resources
COPY target/*.jar app.jar
COPY src/main/resources/sample-data.csv .
COPY src/main/resources/schema.sql .
# Create directory for H2 database files if using file-based persistence
RUN mkdir -p /data
# Expose ports (8080 for web, 9092 for H2 TCP if needed)
EXPOSE 8080
# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

