package com.northstar.studentcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point.
 * This single Spring Boot application acts as:
 * - REST API (submit applications)
 * - Async worker (poll SQS like a Lambda consumer)
 * - DLQ monitor (poll DLQ for poison messages)
 */
@SpringBootApplication
public class StudentCardPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentCardPlatformApplication.class, args);
    }

}
