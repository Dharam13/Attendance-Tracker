FROM maven:3.8-openjdk-17 AS build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM selenium/standalone-chrome:latest

USER root
# Install Java
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    && rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]