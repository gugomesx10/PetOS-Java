FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY project/pom.xml .
COPY project/src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]