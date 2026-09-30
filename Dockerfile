# Change from:
# FROM eclipse-temurin:17-jre-alpine

# To the fully qualified registry address:
FROM docker.io/library/eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
