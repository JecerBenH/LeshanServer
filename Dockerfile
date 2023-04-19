FROM openjdk:8-jre-alpine
EXPOSE 8081/tcp
EXPOSE 5683/udp
MAINTAINER jecer
COPY target/LeshanServer-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
