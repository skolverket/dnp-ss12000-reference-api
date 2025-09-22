FROM registry.access.redhat.com/ubi9/openjdk-21:1.22-1.1744796719
COPY target/provisioning-reference-api-1.0.0-SNAPSHOT.jar /app.jar
COPY src/main/resources/pkcs/ss12k-ref.p12 /cert/ss12k-ref.p12
EXPOSE 8888
EXPOSE 8889
# Test cert. Override this env.
ENV AUTH_PKCS_PATH="/cert/ss12k-ref.p12"
CMD ["java", "-jar", "/app.jar"]
