# Apache Camel: Telemetry demo
This directory contains sources of demo apps based on Apache Camel with OpenTelemetry.
![Demo case](.img/telemetry.png?raw=true)
Also, there are the following alternative options:
- [OpenTracing with Jaeger](#OpenTracing)
- [Zipkin](#Zipkin)

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
- Kibana
- Grafana, including:
    - Preconfigured datasources for Jaeger, Prometheus and ElasticSearch
    - Dashboard for Apache Camel apps
    - Dashboard for Logs from ElasticSearch
    - Dashboard for Jaeger
## Preparing
Clone [docker-envs](https://github.com/stn1slv/docker-envs) repo:
```
git clone https://github.com/stn1slv/docker-envs.git
```
Go to root directory of the repo:
```
cd docker-envs
```
All the following docker-compose commands should be run from this directory.
## Running
You may want to remove any old containers to start clean:
```
docker rm -f kafka zookeeper prometheus grafana kibana elasticsearch jaeger otel-collector filebeat tripbooking carbooking flightbooking hotelbooking
```
We suggest using two terminal windows to start the following components: 
- infrastructure components
- demo apps
#### Startup infrastructure components
```
docker-compose -f compose.yml -f kafka/compose-cp.yml -f jaeger/streaming.yml -f otel-collector/compose.yml -f elasticsearch/compose.yml -f prometheus/compose.yml -f filebeat/compose.yml -f kibana/compose.yml -f grafana/compose.yml up
```
### Startup demo apps
```
docker-compose -f compose.yml -f demo-apps/compose-otel.yml up
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

## Alternative options
### OpenTracing
The source codes and readme are available in [jaeger](https://github.com/stn1slv/demo-apps/tree/jaeger) branch of the repo.
![OpenTelemetry](https://raw.githubusercontent.com/stn1slv/demo-apps/jaeger/.img/telemetry.png)

### Zipkin
The source codes and readme are available in [zipkin](https://github.com/stn1slv/demo-apps/tree/zipkin) branch of the repo.
![Zipkin](https://raw.githubusercontent.com/stn1slv/demo-apps/zipkin/.img/telemetry.png)
