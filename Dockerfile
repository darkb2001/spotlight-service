FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG JAR_FILE=target/spotlight-service-*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

# Health: hit /actuator/health from orchestrator (image has no wget/curl by default)
ENTRYPOINT ["java", "-jar", "app.jar"]
