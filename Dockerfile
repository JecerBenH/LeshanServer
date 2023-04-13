FROM openjdk:8-jre-alpine
MAINTAINER jecer
COPY target/LeshanServer-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
