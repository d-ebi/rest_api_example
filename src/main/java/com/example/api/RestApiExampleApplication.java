package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.api"})
public class RestApiExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestApiExampleApplication.class, args);
    }
}
