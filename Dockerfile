# Build Stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy all project files
COPY . .

# Fix line endings consistently (replace CRLF with LF)
RUN sed -i 's/\r$//' gradlew

# Grant execution permissions
RUN chmod +x gradlew

# Build the application
# -x test to speed up build (tests can be run in CI/CD pipeline)
RUN ./gradlew clean build -x test

# Runtime Stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built artifact from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port 8080 (CloudType default)
EXPOSE 8080

# Run the application
# Use shell info to allow variable expansion if needed, but array form is safer for signal handling
ENTRYPOINT ["java", "-jar", "app.jar"]
