
FROM maven:3.8.6-jdk-11 AS BUILD
RUN mkdir /build
COPY src /build/
COPY pom.xml /build
WORKDIR /build

RUN mvn package

FROM openjdk:buster

RUN mkdir /app
COPY --from=BUILD /build/target/bank_fargate-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT [ "java", "-jar", "app.jar" ]
