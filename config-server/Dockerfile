FROM openjdk:17-jdk-slim

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x ./mvnw
RUN ./mvnw package -DskipTests

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "target/config-server-1.0.1.jar"]