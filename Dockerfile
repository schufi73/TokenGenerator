# Use an official JDK runtime as a parent image
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Download dependencies (cache layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/build/libs/TokenGenerator-1.0-SNAPSHOT.jar app.jar

# Generate a self-signed certificate for HTTPS support
RUN keytool -genkeypair -alias tomcat -keyalg RSA -keysize 2048 -storetype PKCS12 \
    -keystore keystore.p12 -validity 3650 -dname "CN=TokenGenerator" \
    -storepass password -keypass password

# Create a volume for the SQLite database
VOLUME /app/db

# Expose the application port
EXPOSE 8080

# Environment variable for the database path
ENV DB_PATH=/app/db/codes.db

# Run the application
ENTRYPOINT ["java", "-Dspring.datasource.url=jdbc:sqlite:${DB_PATH}", "-jar", "app.jar"]
