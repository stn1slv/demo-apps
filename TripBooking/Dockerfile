FROM maven:3.9-eclipse-temurin-17 as app-builder
# Build app
WORKDIR /var/src/app
COPY . .
RUN mvn -DskipTests clean package && mv target/*.jar target/app.jar
# Download OpenTelemetry Java Agent
ENV OTEL_AGENT_VERSION=v1.33.2
RUN wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/$OTEL_AGENT_VERSION/opentelemetry-javaagent.jar -P /var/src/app/target

FROM eclipse-temurin:17-jre
COPY --from=app-builder /var/src/app/target/app.jar /opt
COPY --from=app-builder /var/src/app/target/opentelemetry-javaagent.jar /opt
CMD ["java", "-javaagent:/opt/opentelemetry-javaagent.jar", "-jar", "/opt/app.jar"]