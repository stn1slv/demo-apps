package com.example.demo;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@CamelSpringBootTest
@UseAdviceWith
public class MySimpleCamelRouterTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @EndpointInject("mock:kafka:car_output")
    private MockEndpoint mockKafkaOutput;

    @BeforeEach
    public void setup() throws Exception {
        // Replace the Kafka consumer and producer endpoints with mocks
        AdviceWith.adviceWith(camelContext, "bookCar-kafka", routeBuilder -> {
            routeBuilder.replaceFromWith("direct:kafka-test-input");
            routeBuilder.interceptSendToEndpoint("kafka:car_output")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka:car_output");
        });
        camelContext.start();
    }

    @Test
    public void testKafkaRoute() throws Exception {
        String testMessage = "{\"carId\":\"1234\",\"model\":\"Tesla\",\"bookingDate\":\"2025-07-01\"}";
        mockKafkaOutput.expectedMessageCount(1);
        producerTemplate.sendBody("direct:kafka-test-input", testMessage);
        mockKafkaOutput.assertIsSatisfied();
        String receivedBody = mockKafkaOutput.getReceivedExchanges().get(0).getIn().getBody(String.class);
        assertNotNull(receivedBody);
    }
}
