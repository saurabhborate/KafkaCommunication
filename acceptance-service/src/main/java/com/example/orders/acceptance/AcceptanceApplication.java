package com.example.orders.acceptance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AcceptanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AcceptanceApplication.class, args);
    }
}
