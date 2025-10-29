package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.api"})
public class RestApiExampleApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RestApiExampleApplication.class);
        String runProfiles = System.getProperty("spring-boot.run.profiles");
        if (runProfiles != null && !runProfiles.isBlank()) {
            String[] profiles = java.util.Arrays.stream(runProfiles.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
            if (profiles.length > 0) {
                app.setAdditionalProfiles(profiles);
            }
        }
        app.run(args);
    }
}
