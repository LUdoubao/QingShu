FROM openjdk:8-jre-alpine
VOLUME /tmp
COPY target/oss-service-server-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]