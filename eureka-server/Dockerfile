FROM maven:3.9-eclipse-temurin-17 as builder
WORKDIR /app
COPY . .
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/eureka-server-1.0.1.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]