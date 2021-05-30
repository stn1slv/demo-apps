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
//                .port(8080).host("localhost")
                .bindingMode(RestBindingMode.json);

        rest().get("/bookCar")
                .to("direct:bookCar");

        from("direct:bookCar")
                .log(LoggingLevel.INFO, "Book car request")
//                .to("log:com.mycompany.order?showAll=true&multiline=true")
                .setBody(constant("{ \"bookingId\": 123, \"car\": \"Toyota Corolla\", \"startDate\": \"12-11-2018\", \"endDate\": \"15-11-2018\", \"price\": 150 }"))
                // .to("kafka:test?brokers=localhost:9092")
                .unmarshal()
                .json(JsonLibrary.Jackson);
    }
}