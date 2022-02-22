FROM maven:3.8-jdk-11 as app-builder
# Build app
WORKDIR /var/src/app
COPY . .
RUN mvn -DskipTests clean package && mv target/*.jar target/app.jar
# Download OpenTelemetry Java Agent
ENV OTEL_AGENT_VERSION=v1.11.1
RUN wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/$OTEL_AGENT_VERSION/opentelemetry-javaagent.jar -P /var/src/app/target

FROM adoptopenjdk:11-jre-hotspot
COPY --from=app-builder /var/src/app/target/app.jar /opt
COPY --from=app-builder /var/src/app/target/opentelemetry-javaagent.jar /opt
CMD ["java", "-javaagent:/opt/opentelemetry-javaagent.jar", "-jar", "/opt/app.jar"]