FROM maven:3.9.9-amazoncorretto-21 AS build

WORKDIR /home/app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /home/app

COPY --from=build /home/app/target/containerView-0.0.1-SNAPSHOT.jar containerView.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/home/app/containerView.jar"]

