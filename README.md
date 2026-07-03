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
Each app runs with the OpenTelemetry Java agent (attached in the Dockerfile) and `camel-opentelemetry-starter` (the agent provides the SDK, exporters and HTTP/Kafka client instrumentation; the starter adds Camel route spans on top of it, since the agent has no Camel 4 instrumentation of its own).

| Signal | Path |
|---|---|
| Traces | OTel Java agent → OTLP gRPC → OTel Collector → Jaeger |
| Metrics | Micrometer OTLP registry → OTLP HTTP → OTel Collector → Prometheus exporter (`:8889`) → Prometheus |
| Logs | stdout (logback pattern includes `trace_id`/`span_id` from MDC) → Filebeat → Elasticsearch |

Cross-signal correlation is preconfigured in Grafana: the Jaeger datasource links spans to logs (Elasticsearch query on `msg.trace_id`) and to metrics (via the `application` resource attribute, exposed as the `exported_job` label in Prometheus).

The agent's own metrics and logs exporters are disabled (`OTEL_METRICS_EXPORTER=none`, `OTEL_LOGS_EXPORTER=none`): metrics are owned by Micrometer (whose names the Grafana dashboards use) and logs are owned by Filebeat.

## Running
You may want to remove any old containers to start clean:
```
docker rm -f kafka prometheus grafana elasticsearch jaeger otel-collector filebeat tripbooking carbooking flightbooking hotelbooking
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
