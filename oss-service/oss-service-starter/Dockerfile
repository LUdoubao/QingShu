FROM openjdk:8-jre-alpine
VOLUME /tmp
COPY target/oss-service-starter-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]