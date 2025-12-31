# Stage 1: Build Stage
FROM gradle:8-jdk21 AS build
WORKDIR /home/gradle

# 3. Build the Main Application
# Switch back to a separate directory for the main app
WORKDIR /home/gradle/app
# Copy everything from the main project (including build.gradle and src)
# The .dockerignore should ideally ignore the kafka-payloads folder here
# to keep this layer clean, but it's not strictly required.
COPY --chown=gradle:gradle . .

# Build the main service
RUN gradle build -x test --no-daemon

# Stage 2: Lightweight Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the jar from the /home/gradle/app/build/libs path
COPY --from=build /home/gradle/app/build/libs/*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]