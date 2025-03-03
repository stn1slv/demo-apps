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

        rest().get("/bookCar")
                .to("direct:bookCar");

        from("direct:bookCar").routeId("bookCar-http")
                .log(LoggingLevel.INFO, "New book car request with traceId=${header.x-b3-traceid}")
                .bean(new AvailableCars(),"getAvailableCar")
                .unmarshal().json(JsonLibrary.Jackson);

        // kafka based 
        from("kafka:car_input").routeId("bookCar-kafka")
                .log(LoggingLevel.INFO, "New book car request via Kafka topic")
                // .to("log:debug?showAll=true&multiline=true")
                .bean(new AvailableCars(),"getAvailableCar")
                .to("kafka:car_output");

    }
}