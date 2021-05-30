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

        rest().get("/bookHotel")
                .to("direct:bookHotel");

        from("direct:bookHotel")
                .log(LoggingLevel.INFO, "Book hotel request")
                .setBody(constant("{ \"bookingId\": 125, \"hotel\": \"Hilton New York\", \"startDate\": \"13-11-2018\", \"endDate\": \"14-11-2018\", \"price\": 125 }"))
                .unmarshal()
                .json(JsonLibrary.Jackson);
    }
}