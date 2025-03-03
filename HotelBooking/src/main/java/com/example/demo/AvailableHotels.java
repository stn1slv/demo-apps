package com.example.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.camel.Handler;

public class AvailableHotels {
    private static final Random RANDOM = new Random();
    private static final List<String> HOTELS = Arrays.asList(
            "Four Seasons",
            "Sheraton",
            "The Ritz",
            "Marriott",
            "Hilton",
            "Accor",
            "Hyatt",
            "Radisson"
    );

    @Handler
    public String getAvailableHotel() {
        return String.format("""
                {
                    "bookingId": %d,
                    "hotel": "%s",
                    "startDate": "12-11-2018",
                    "endDate": "15-11-2018",
                    "price": %d
                }""",
                RANDOM.nextInt(1000),
                HOTELS.get(RANDOM.nextInt(HOTELS.size())),
                RANDOM.nextInt(150) + 150
        );
    }
}
