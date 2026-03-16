# 1. Use an official, lightweight Java 17 image
FROM eclipse-temurin:17-jdk-alpine

# 2. Set the working directory inside the container
WORKDIR /app

# 3. Copy the jar file. Using *.jar makes it dynamic so you don't have to guess the exact snapshot version!
COPY target/*.jar app.jar

# 4. Expose the port your Spring Boot app runs on
EXPOSE 8080

# 5. Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]