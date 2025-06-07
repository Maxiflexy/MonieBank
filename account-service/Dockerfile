#FROM maven:3.9-eclipse-temurin-17 as builder
#WORKDIR /app
#COPY . .
#RUN mvn package -DskipTests
#
#FROM eclipse-temurin:17-jre
#WORKDIR /app
#COPY --from=builder /app/target/account-service-1.0.1.jar app.jar
#EXPOSE 8082
#ENTRYPOINT ["java", "-jar", "app.jar"]


FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/account-service-1.0.1.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]