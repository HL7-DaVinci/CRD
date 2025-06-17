FROM eclipse-temurin:11-jdk-alpine

# Install Gradle
RUN apk add --no-cache curl unzip bash git && \
	GRADLE_VERSION=8.14.2 && \
	curl -fsSL https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle.zip && \
	unzip gradle.zip -d /opt && \
	ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/bin/gradle && \
	rm gradle.zip

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy app files to container
COPY --chown=appuser:appgroup . /CRD/
# Set working directory so that all subsequent command runs in this folder
WORKDIR /CRD/server/
# Embed CDS Library
RUN gradle embedCdsLibrary
# Build the application
RUN gradle build
EXPOSE 8090
# Command to run our app
CMD ["gradle", "bootRun"]