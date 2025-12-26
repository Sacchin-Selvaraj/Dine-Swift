# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy only wrapper + build files first (cache optimization)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build JAR
RUN ./gradlew bootJar --no-daemon

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
