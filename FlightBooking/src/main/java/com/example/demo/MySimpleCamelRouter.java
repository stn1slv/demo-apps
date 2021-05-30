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
                .port(8080).host("localhost")
                .bindingMode(RestBindingMode.json);

        rest().get("/bookFlight")
                .to("direct:bookFlight");

        from("direct:bookFlight")
                .log(LoggingLevel.INFO, "Book flight request")
                .setBody(constant("{ \"bookingId\": 127, \"flightNumber\": \"SU 0102\", \"startDate\": \"11-11-2018\", \"endDate\": \"15-11-2018\", \"price\": 355 }"))
                .unmarshal()
                .json(JsonLibrary.Jackson);
    }
}