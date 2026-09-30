# Step 1: Use an official, lightweight Java 17 runtime image
FROM eclipse-temurin:17-jre-alpine

# Step 2: Create and move into an application working directory inside the container
WORKDIR /app

# Step 3: Copy the compiled .jar file produced by Maven into the container as "app.jar"
COPY target/*.jar app.jar

# Step 4: Document that Tomcat inside this container listens on port 8080
EXPOSE 8080

# Step 5: Define the command that executes when the container boots up
ENTRYPOINT ["java", "-jar", "app.jar"]