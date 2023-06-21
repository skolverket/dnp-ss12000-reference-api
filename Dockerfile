FROM docker.io/library/openjdk:11
COPY target/provisioning-reference-api-1.0.0-SNAPSHOT.jar /app.jar
EXPOSE 8888
EXPOSE 8889
CMD ["java", "-jar", "/app.jar"]
