FROM eclipse-temurin:17-jdk-alpine AS builder

# embedCdsLibrary task requires git
RUN apk add --no-cache git bash

WORKDIR /CRD

# Copy the Gradle wrapper
COPY gradlew .
COPY gradle ./gradle
RUN chmod +x gradlew

# Copy the rest of the source code
COPY . .

# Build the application skipping checkstyle tasks
RUN ./gradlew :server:embedCdsLibrary && ./gradlew build -x checkstyleMain -x checkstyleTest

# Use a smaller image for the final stage
FROM eclipse-temurin:17-jre-alpine

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder --chown=appuser:appgroup /CRD/server/build/libs/*.jar app.jar
COPY --from=builder --chown=appuser:appgroup /CRD/server/CDS-Library ./CDS-Library/

# Create the ValueSetCache directory
RUN mkdir -p /app/ValueSetCache && chown appuser:appgroup /app/ValueSetCache

# Expose the application port
EXPOSE 8090

# Run the application as the non-root user
USER appuser

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]