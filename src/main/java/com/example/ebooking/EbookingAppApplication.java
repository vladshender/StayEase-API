package com.example.ebooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
public class EbookingAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(EbookingAppApplication.class, args);
    }
}
