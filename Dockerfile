FROM openjdk:21-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENV DB_DDL_AUTO=create

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8080