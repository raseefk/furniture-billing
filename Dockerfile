# Build the application with Maven and package into a runnable Spring Boot jar
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

# Runtime image uses a smaller JRE base image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
