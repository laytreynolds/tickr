package com.tickr.tickr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TickrApplication {

    public static void main(String[] args) {
        SpringApplication.run(TickrApplication.class, args);
    }
}
