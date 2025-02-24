package com.example.demo;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class MySimpleCamelRouter extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json);

        rest()
                .get("/bookTrip").to("direct:bookTrip")
                .get("/asyncBookTrip").to("direct:asyncBookTrip");

        from("direct:bookTrip")
                .routeId("bookTrip-http")
                .routeDescription("This is demo service for demonstration telemetry aspects")
                .log(LoggingLevel.INFO, "New book trip request via HTTP")
                .multicast(new MergeAggregationStrategy()).parallelProcessing()
                        .to("{{car.booking.url}}?bridgeEndpoint=true")
                        .to("{{flight.booking.url}}?bridgeEndpoint=true")
                        .to("{{hotel.booking.url}}?bridgeEndpoint=true")
                .end()
                .log(LoggingLevel.INFO,"Response: ${body}")
                .unmarshal().json(JsonLibrary.Jackson);

        // kafka based 
        from("direct:asyncBookTrip")
                .routeId("bookTrip-kafka-request")
                .routeDescription("This is demo service for demonstration telemetry aspects via Kafka")
                .log(LoggingLevel.INFO, "New book trip request via Kafka")
                .setBody(simple("New async request"))
                .multicast().parallelProcessing()
                        .to("kafka:car_input?brokers={{kafka.brokers}}")
                        .to("kafka:flight_input?brokers={{kafka.brokers}}")
                        .to("kafka:hotel_input?brokers={{kafka.brokers}}")
                .end();
        
        from("kafka:car_output?brokers={{kafka.brokers}}").to("seda:tripAggregator");
        from("kafka:flight_output?brokers={{kafka.brokers}}").to("seda:tripAggregator");
        from("kafka:hotel_output?brokers={{kafka.brokers}}").to("seda:tripAggregator");
        
        from("seda:tripAggregator").routeId("bookTrip-kafka-response")
                .aggregate(constant(true), new MergeAggregationStrategy())
                .completionSize(3)
                .log(LoggingLevel.INFO, "New book trip response: ${body}");
    }
}