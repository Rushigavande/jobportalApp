# Use Eclipse Temurin JDK 17 as the base image
FROM maven:3.9.6-eclipse-temurin-17 as build

# Set working directory
WORKDIR /app

# Copy the project files
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built artifact from build stage
COPY --from=build /app/target/*.jar app.jar

# Set environment variables
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=UTC

# Expose the port
EXPOSE 8080

# Run the application with production profile
CMD ["java", \
     "-jar", \
     "-Dspring.profiles.active=prod", \
     "-Dserver.port=${PORT}", \
     "-Duser.timezone=${TZ}", \
     "app.jar"] 