FROM openjdk:8-jdk-alpine
MAINTAINER Mike Kezele
COPY target/export-converter.jar export-converter.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/scenedoc-server.jar"]
