package com.techdeals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TechDealsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TechDealsApplication.class, args);
    }
}
