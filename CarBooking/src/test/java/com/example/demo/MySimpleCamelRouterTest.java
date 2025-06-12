package com.example.demo;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class MySimpleCamelRouterTest extends CamelTestSupport {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Create HTTP-only routes for testing
                restConfiguration()
                        .component("servlet")
                        .bindingMode(RestBindingMode.json);

                rest().get("/bookCar")
                        .to("direct:bookCar");

                from("direct:bookCar").routeId("bookCar-http")
                        .log(LoggingLevel.INFO, "New book car request with traceId=${header.x-b3-traceid}")
                        .bean(new AvailableCars(),"getAvailableCar")
                        .unmarshal().json(JsonLibrary.Jackson);

                // Create a separate Kafka-like route for testing without actual Kafka
                from("direct:kafka-test-input").routeId("bookCar-kafka-test")
                        .log(LoggingLevel.INFO, "New book car request via test Kafka topic")
                        .bean(new AvailableCars(),"getAvailableCar")
                        .to("mock:kafka-output");
            }
        };
    }

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    /**
     * Test the direct bookCar route without external dependencies
     */
    @Test
    void testDirectBookCarRoute() throws Exception {
        // Given
        context().start();

        // When
        String response = template().requestBody("direct:bookCar", null, String.class);

        // Then
        assertNotNull(response);
        JsonNode jsonNode = objectMapper.readTree(response);
        assertTrue(jsonNode.has("bookingId"));
        assertTrue(jsonNode.has("car"));
        assertTrue(jsonNode.has("price"));
        assertTrue(jsonNode.has("startDate"));
        assertTrue(jsonNode.has("endDate"));
        
        // Verify field values
        assertTrue(jsonNode.get("bookingId").asInt() >= 0);
        assertTrue(jsonNode.get("bookingId").asInt() < 1000);
        assertFalse(jsonNode.get("car").asText().isEmpty());
        assertEquals("12-11-2018", jsonNode.get("startDate").asText());
        assertEquals("15-11-2018", jsonNode.get("endDate").asText());
        assertTrue(jsonNode.get("price").asInt() >= 140);
        assertTrue(jsonNode.get("price").asInt() <= 164);
    }

    /**
     * Test Kafka-like route using AdviceWith to mock Kafka endpoints
     */
    @Test
    void testKafkaRouteWithMockedEndpoints() throws Exception {
        // Given - Use AdviceWith to modify the test Kafka route
        AdviceWith.adviceWith(context(), "bookCar-kafka-test", a -> {
            a.replaceFromWith("direct:kafka-input");
            a.interceptSendToEndpoint("mock:kafka-output")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-verified-output");
        });

        MockEndpoint mockOutput = context().getEndpoint("mock:kafka-verified-output", MockEndpoint.class);
        mockOutput.expectedMessageCount(1);

        context().start();

        // When
        String response = template().requestBody("direct:kafka-input", null, String.class);

        // Then
        assertNotNull(response);
        JsonNode jsonNode = objectMapper.readTree(response);
        assertTrue(jsonNode.has("bookingId"));
        assertTrue(jsonNode.has("car"));
        assertTrue(jsonNode.has("price"));
        assertTrue(jsonNode.has("startDate"));
        assertTrue(jsonNode.has("endDate"));

        // Verify mock endpoint received the message
        mockOutput.assertIsSatisfied();
        
        Exchange receivedExchange = mockOutput.getReceivedExchanges().get(0);
        String receivedResponse = receivedExchange.getIn().getBody(String.class);
        JsonNode receivedJson = objectMapper.readTree(receivedResponse);
        assertTrue(receivedJson.has("bookingId"));
        assertTrue(receivedJson.has("car"));
    }

    /**
     * Test Kafka route with headers propagation using AdviceWith
     */
    @Test
    void testKafkaRouteWithHeaders() throws Exception {
        // Given
        AdviceWith.adviceWith(context(), "bookCar-kafka-test", a -> {
            a.replaceFromWith("direct:kafka-input-headers");
            a.interceptSendToEndpoint("mock:kafka-output")
                .skipSendToOriginalEndpoint()
                .to("mock:kafka-output-headers");
        });

        MockEndpoint mockOutput = context().getEndpoint("mock:kafka-output-headers", MockEndpoint.class);
        mockOutput.expectedMessageCount(1);
        mockOutput.expectedHeaderReceived("x-b3-traceid", "test-trace-123");

        context().start();

        // When
        String response = template().requestBodyAndHeader(
            "direct:kafka-input-headers", 
            null, 
            "x-b3-traceid", 
            "test-trace-123", 
            String.class
        );

        // Then
        assertNotNull(response);
        mockOutput.assertIsSatisfied();
        
        // Verify header propagation
        Exchange exchange = mockOutput.getReceivedExchanges().get(0);
        assertEquals("test-trace-123", exchange.getIn().getHeader("x-b3-traceid"));
    }

    /**
     * Test error handling in routes
     */
    @Test
    void testErrorHandling() throws Exception {
        // Given - Add a route with error handling capabilities
        context().addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                onException(RuntimeException.class)
                    .handled(true)
                    .setBody(constant("{\"error\":\"Processing failed\"}"))
                    .to("mock:error-output");

                from("direct:error-test")
                    .routeId("error-test-route")
                    .process(exchange -> {
                        String body = exchange.getIn().getBody(String.class);
                        if ("ERROR".equals(body)) {
                            throw new RuntimeException("Simulated error");
                        }
                    })
                    .bean(new AvailableCars(), "getAvailableCar")
                    .to("mock:success-output");
            }
        });

        MockEndpoint successMock = context().getEndpoint("mock:success-output", MockEndpoint.class);
        MockEndpoint errorMock = context().getEndpoint("mock:error-output", MockEndpoint.class);
        
        successMock.expectedMessageCount(1);
        errorMock.expectedMessageCount(1);

        context().start();

        // When - Send success and error messages
        template().sendBody("direct:error-test", "NORMAL");
        template().sendBody("direct:error-test", "ERROR");

        // Then
        successMock.assertIsSatisfied();
        errorMock.assertIsSatisfied();
        
        // Verify success response
        String successResponse = successMock.getReceivedExchanges().get(0).getIn().getBody(String.class);
        JsonNode successJson = objectMapper.readTree(successResponse);
        assertTrue(successJson.has("bookingId"));
        
        // Verify error response
        String errorResponse = errorMock.getReceivedExchanges().get(0).getIn().getBody(String.class);
        JsonNode errorJson = objectMapper.readTree(errorResponse);
        assertTrue(errorJson.has("error"));
        assertEquals("Processing failed", errorJson.get("error").asText());
    }

    /**
     * Test multiple requests to verify randomness and consistency
     */
    @Test
    void testMultipleRequests() throws Exception {
        // Given
        context().start();

        // When - make multiple requests
        String response1 = template().requestBody("direct:bookCar", null, String.class);
        String response2 = template().requestBody("direct:bookCar", null, String.class);
        String response3 = template().requestBody("direct:bookCar", null, String.class);

        // Then - verify all responses are valid
        JsonNode json1 = objectMapper.readTree(response1);
        JsonNode json2 = objectMapper.readTree(response2);
        JsonNode json3 = objectMapper.readTree(response3);

        // All should have required fields
        for (JsonNode json : new JsonNode[]{json1, json2, json3}) {
            assertTrue(json.has("bookingId"));
            assertTrue(json.has("car"));
            assertTrue(json.has("price"));
            assertEquals("12-11-2018", json.get("startDate").asText());
            assertEquals("15-11-2018", json.get("endDate").asText());
        }

        // At least one field should vary between requests (due to randomness)
        boolean hasDifference = 
            !json1.get("bookingId").equals(json2.get("bookingId")) ||
            !json1.get("car").equals(json2.get("car")) ||
            !json1.get("price").equals(json2.get("price"));
        
        assertTrue(hasDifference, "Multiple requests should produce some variation due to randomness");
    }

    /**
     * Test Kafka route performance with multiple messages using AdviceWith
     */
    @Test
    void testKafkaRoutePerformance() throws Exception {
        // Given
        AdviceWith.adviceWith(context(), "bookCar-kafka-test", a -> {
            a.replaceFromWith("direct:perf-kafka-input");
            a.interceptSendToEndpoint("mock:kafka-output")
                .skipSendToOriginalEndpoint()
                .to("mock:perf-kafka-output");
        });

        MockEndpoint mockOutput = context().getEndpoint("mock:perf-kafka-output", MockEndpoint.class);
        int messageCount = 10;
        mockOutput.expectedMessageCount(messageCount);

        context().start();

        // When - Send multiple messages to test throughput
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < messageCount; i++) {
            template().sendBody("direct:perf-kafka-input", null);
        }
        long endTime = System.currentTimeMillis();

        // Then
        mockOutput.assertIsSatisfied();
        assertEquals(messageCount, mockOutput.getReceivedExchanges().size());
        
        // Verify performance (should complete within reasonable time)
        long duration = endTime - startTime;
        assertTrue(duration < 5000, "Performance test should complete within 5 seconds");

        // Verify all responses are valid
        for (Exchange exchange : mockOutput.getReceivedExchanges()) {
            String response = exchange.getIn().getBody(String.class);
            JsonNode jsonNode = objectMapper.readTree(response);
            assertTrue(jsonNode.has("bookingId"));
            assertTrue(jsonNode.has("car"));
        }
    }

    /**
     * Test route configuration and structure
     */
    @Test
    void testRouteConfiguration() throws Exception {
        // Given
        context().start();

        // Then - verify routes are configured correctly
        assertNotNull(context().getRoute("bookCar-http"));
        assertNotNull(context().getRoute("bookCar-kafka-test"));
        
        // Verify route IDs
        assertEquals("bookCar-http", context().getRoute("bookCar-http").getId());
        assertEquals("bookCar-kafka-test", context().getRoute("bookCar-kafka-test").getId());
    }

    /**
     * Test real Kafka route simulation with actual route structure
     */
    @Test
    void testRealKafkaRouteSimulation() throws Exception {
        // Given - Create a route that mimics the real Kafka route structure
        context().addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:real-kafka-input").routeId("real-kafka-simulation")
                    .log(LoggingLevel.INFO, "New book car request via simulated Kafka topic")
                    .bean(new AvailableCars(),"getAvailableCar")
                    .to("mock:real-kafka-output");
            }
        });

        MockEndpoint mockOutput = context().getEndpoint("mock:real-kafka-output", MockEndpoint.class);
        mockOutput.expectedMessageCount(1);

        context().start();

        // When
        String response = template().requestBody("direct:real-kafka-input", null, String.class);

        // Then
        assertNotNull(response);
        JsonNode jsonNode = objectMapper.readTree(response);
        assertTrue(jsonNode.has("bookingId"));
        assertTrue(jsonNode.has("car"));
        assertTrue(jsonNode.has("price"));
        
        // Verify the message was sent to the output endpoint
        mockOutput.assertIsSatisfied();
        
        Exchange receivedExchange = mockOutput.getReceivedExchanges().get(0);
        String receivedResponse = receivedExchange.getIn().getBody(String.class);
        assertEquals(response, receivedResponse);
    }
}
