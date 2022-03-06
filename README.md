# Apache Camel: Telemetry demo
This repository contains sources of demo apps based on Apache Camel with Spring Boot. 
There are the following options:
- [OpenTelemetry](https://github.com/stn1slv/demo-apps/tree/opentelemetry)
- [OpenTracing with Jaeger](https://github.com/stn1slv/demo-apps/tree/jaeger)
- [Zipkin](https://github.com/stn1slv/demo-apps/tree/zipkin)

## Demo case
This is a simple trip booking aggregator app where the services communicates via HTTP and Kafka protocols. 
The main goal of the repo is to demonstate telemetry capabilities of populer open source components. That's why only happy path functionality of the services are implemented.

The list of infrastructure components for all cases:
- Apache Kafka
- Prometheus
- Grafana
- FileBeat
- ElasticSearch
- Kibana

### Jaeger
The source codes and readme are available in [jaeger](https://github.com/stn1slv/demo-apps/tree/jaeger) branch of the repo.
![Jaeger](https://raw.githubusercontent.com/stn1slv/demo-apps/jaeger/.img/telemetry.png)

### OpenTelemetry
The source codes and readme are available in [opentelemetry](https://github.com/stn1slv/demo-apps/tree/opentelemetry) branch of the repo.
![OpenTelemetry](https://raw.githubusercontent.com/stn1slv/demo-apps/opentelemetry/.img/telemetry.png)

### Zipkin
The source codes and readme are available in [zipkin](https://github.com/stn1slv/demo-apps/tree/zipkin) branch of the repo.
![Zipkin](https://raw.githubusercontent.com/stn1slv/demo-apps/zipkin/.img/telemetry.png)
