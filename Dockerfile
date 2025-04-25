FROM maven:3.8-openjdk-17 AS build

# Copy the project files
WORKDIR /build
COPY pom.xml .
COPY src ./src

# Build the project
RUN mvn package -DskipTests

FROM selenium/standalone-chrome:latest

USER root

# Install Java
RUN apt-get update && apt-get install -y wget apt-transport-https \
    && wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - \
    && echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list \
    && apt-get update \
    && apt-get install -y temurin-17-jdk \
    && rm -rf /var/lib/apt/lists/*

# Set up app directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /build/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Add a health check script
RUN echo '#!/bin/bash\nwget -q --spider http://localhost:8080/health || exit 1' > /healthcheck.sh \
    && chmod +x /healthcheck.sh

# Run the app with specific profiles and system properties
CMD ["java", "-Dspring.profiles.active=railway", "-Dwebdriver.chrome.driver=/usr/bin/chromedriver", "-jar", "app.jar"]