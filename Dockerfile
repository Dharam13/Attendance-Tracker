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
    && mkdir -p /etc/apt/keyrings \
    && wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc \
    && echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list \
    && apt-get update \
    && apt-get install -y temurin-17-jdk \
    && rm -rf /var/lib/apt/lists/*

# Set up app directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /build/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the app with specific system properties for WebDriver
CMD ["java", "-Dwebdriver.chrome.driver=/usr/bin/chromedriver", "-jar", "app.jar"]