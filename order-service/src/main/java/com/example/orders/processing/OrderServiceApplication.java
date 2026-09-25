package com.example.orders.processing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.example.orders")
public class OrderServiceApplication { public static void main(String[] args) { SpringApplication.run(OrderServiceApplication.class, args); } }
