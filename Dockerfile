# Build Stage
# Use official Gradle image to avoid 'gradlew' script issues (CRLF, permissions)
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# Copy project files
COPY --chown=gradle:gradle . .

# Build the application using the installed gradle
# --no-daemon to save memory in CI environment
RUN gradle clean build -x test --no-daemon

# Runtime Stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built artifact from the builder stage
# Note: Path might vary slightly depending on project name, using wildcard to be safe
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
