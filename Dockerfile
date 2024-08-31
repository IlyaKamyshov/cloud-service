FROM openjdk:17-jdk-alpine

EXPOSE 8080

ADD /target/cloudservice-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]