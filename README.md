# Apache Camel: Telemetry demo
This directory contains sources of demo apps based on Apache Camel with OpenTelemetry.
![Demo case](.img/telemetry-light.png#gh-light-mode-only)
![Demo case](.img/telemetry-dark.png#gh-dark-mode-only)

The environment is the following:
-  Demo apps:
    - [Trip booking app](TripBooking)
    - [Flight booking app](FlightBooking)
    - [Hotel booking app](HotelBooking)
    - [Car booking app](CarBooking)
- Apache Kafka
- Jaeger
- OpenTelemetry Collector
- Prometheus
- Grafana
- FileBeat
- ElasticSearch
- Grafana, including:
    - Preconfigured datasources for Jaeger, Prometheus and ElasticSearch
    - Dashboard for Apache Camel apps
    - Dashboard for Logs from ElasticSearch
    - Dashboard for Jaeger

## How the telemetry is wired
Each app runs with the OpenTelemetry Java agent and `camel-opentelemetry-starter`. The agent provides the SDK, the exporters and the HTTP and Kafka client instrumentation; the starter adds Camel route spans.

| Signal | Path |
|---|---|
| Traces | OTel Java agent → OTLP gRPC → OTel Collector → Jaeger |
| Metrics | Micrometer OTLP registry over HTTP (`:4318`: JVM, process, system, HTTP server, Camel, logback) and OTel Java agent over gRPC (`:4317`: Kafka client, HTTP) → OTel Collector → Prometheus exporter (`:8889`) → Prometheus |
| Logs | stdout (logback pattern includes the trace and span IDs) → Filebeat → Elasticsearch |

The agent's runtime (JVM) metrics are turned off with `OTEL_INSTRUMENTATION_RUNTIME_TELEMETRY_ENABLED=false`, because Micrometer already exports JVM metrics under the same Prometheus names.

Prometheus scrapes the collector, not the apps, so the app's `service.name` (`OTEL_SERVICE_NAME`) appears as the `exported_job` label. The dashboard panels and the Jaeger trace-to-metrics link filter on this label. The dashboard's Application variable and the Jaeger link take their value from the `application` resource attribute (`OTEL_RESOURCE_ATTRIBUTES`). So `OTEL_SERVICE_NAME` and `application` must stay equal for each app; if they differ, the whole dashboard and the Jaeger link show no data.

## Running
You may want to remove any old containers to start clean:
```
docker rm -f kafka prometheus grafana elasticsearch jaeger otel-collector filebeat trip-booking-app car-booking-app flight-booking-app hotel-booking-app
```
We suggest using two terminal windows to start the following components: 
- infrastructure components
- demo apps
### Startup infrastructure components
```
docker-compose -f compose.yml -f compose.infra.yml up --remove-orphans
```
### Startup demo apps
```
docker-compose -f compose.yml -f compose.demo-apps.yml up
```
## Testing
Testing tools are following:
- Any HTTP client (web browser, curl, httpie, postman etc.)
- Apache JMeter for generation load 
#### cURL
Sync communication (over HTTP):
```
curl http://127.0.0.1:8080/camel/bookTrip
```
Async communication (over Kafka):
```
curl http://127.0.0.1:8080/camel/asyncBookTrip
```
#### Apache JMeter
You can find JMeter project by [the link](TripBooking/Demo.jmx).
