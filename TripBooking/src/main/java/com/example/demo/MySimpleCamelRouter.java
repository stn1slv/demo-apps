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

        rest().get("/bookTrip")
                .to("direct:bookTrip");

        from("direct:bookTrip")
                .log(LoggingLevel.INFO, "Book trip request")
                .log("Sending ${body} with correlation key ${header.myId}")
                .multicast().to("seda:car", "seda:hotel", "seda:flight")
                .setBody(simple("Ok"));
//                .log("${body}");
        from("seda:car").to("http://carbooking:8080/camel/bookCar?bridgeEndpoint=true").to("seda:tripAggregator");
        from("seda:flight").to("http://flightbooking:8080/camel/bookFlight?bridgeEndpoint=true").to("seda:tripAggregator");
        from("seda:hotel").to("http://hotelbooking:8080/camel/bookHotel?bridgeEndpoint=true").to("seda:tripAggregator");

        from("seda:tripAggregator")
                .aggregate(constant(true), new MergeAggregationStrategy())
                .completionSize(3)
                .log("${body}");
    }
}