package com.example.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.camel.Handler;

public class AvailableCars {
    private static final Random RANDOM = new Random();
    private static final List<String> CARS = Arrays.asList(
            "Toyota Corolla",
            "Honda Civic",
            "Mazda 3",
            "Hyundai Elantra",
            "Subaru Impreza",
            "Volkswagen Jetta",
            "Volkswagen Golf",
            "Ford Fiesta",
            "Ford Focus",
            "Chevrolet Cruze",
            "Kia Ceed",
            "Skoda Octavia",
            "Citroen C4",
            "Peugeot 308"
    );

    @Handler
    public String getAvailableCar() {
        return String.format("""
                {
                    "bookingId": %d,
                    "car": "%s",
                    "startDate": "12-11-2018",
                    "endDate": "15-11-2018",
                    "price": %d
                }""",
                RANDOM.nextInt(1000),
                CARS.get(RANDOM.nextInt(CARS.size())),
                RANDOM.nextInt(25) + 140
        );
    }
}
