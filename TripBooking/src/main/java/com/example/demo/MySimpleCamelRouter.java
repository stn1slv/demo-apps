package com.example.demo;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import org.springframework.stereotype.Component;

@Component
public class MySimpleCamelRouter extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json);

        rest().get("/bookTrip")
                .to("direct:bookTrip");

        from("direct:bookTrip")
                .routeId("bookTrip-http")
                .routeDescription("This is demo service for demonstration telemetry aspects")
                .log(LoggingLevel.INFO, "New book trip request with traceId=${header.x-b3-traceid}")
                .multicast(new MergeAggregationStrategy()).parallelProcessing()
                        // .to("http://localhost:8081/camel/bookCar?bridgeEndpoint=true")
                        // .to("http://localhost:8082/camel/bookFlight?bridgeEndpoint=true")
                        // .to("http://localhost:8083/camel/bookHotel?bridgeEndpoint=true")
                        .to("http://carbooking:8080/camel/bookCar?bridgeEndpoint=true")
                        .to("http://flightbooking:8080/camel/bookFlight?bridgeEndpoint=true")
                        .to("http://hotelbooking:8080/camel/bookHotel?bridgeEndpoint=true")
                .end()
                .log(LoggingLevel.INFO,"Response: \n${body}")
                .unmarshal().json(JsonLibrary.Jackson);
    }
}