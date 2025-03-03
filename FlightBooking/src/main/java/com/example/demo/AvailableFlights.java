package com.example.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.camel.Handler;

public class AvailableFlights {
    private static final Random RANDOM = new Random();
    private static final List<String> FLIGHTS = Arrays.asList(
            "American Airlines",
            "Delta Air Lines",
            "Lufthansa",
            "United Airlines",
            "Air France–KLM",
            "IAG",
            "Southwest Airlines",
            "China Southern Airlines",
            "All Nippon Airways",
            "China Eastern Airlines",
            "Ryanair",
            "Air China",
            "British Airways",
            "Emirates",
            "Turkish Airlines",
            "Qatar Airways"
    );

    @Handler
    public String getAvailableFlight() {
        return String.format("""
                {
                    "bookingId": %d,
                    "flight": "%s %d",
                    "startDate": "12-11-2018",
                    "endDate": "15-11-2018",
                    "price": %d
                }""",
                RANDOM.nextInt(1000),
                FLIGHTS.get(RANDOM.nextInt(FLIGHTS.size())),
                RANDOM.nextInt(10000),
                RANDOM.nextInt(100) + 100
        );
    }
}
