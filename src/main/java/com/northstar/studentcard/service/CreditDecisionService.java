package com.northstar.studentcard.service;

import com.northstar.studentcard.domain.ApplicationStatus;
import com.northstar.studentcard.domain.StudentCardApplication;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Business logic that decides approval/decline.
 * This is deterministic (based on hashing) so it is predictable in tests.
 *
 * Also includes a way to simulate transient failures:
 * - If nationalId ends with "99" we throw an exception (to trigger retries and DLQ).
 */
@Service
public class CreditDecisionService {

    public ApplicationStatus decide(StudentCardApplication application) {
        if (application.getNationalId() != null && application.getNationalId().endsWith("99")) {
            // Simulate a transient or integration failure (e.g., credit bureau timeout).
            throw new IllegalStateException("Credit bureau temporary failure for nationalId ending with 99");
        }

        // Simple deterministic scoring: hash nationalId + age.
        int score = score(application.getNationalId() + ":" + application.getAge());
        return (score % 2 == 0) ? ApplicationStatus.APPROVED : ApplicationStatus.DECLINED;
    }

    private int score(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            int value = 0;
            for (int i = 0; i < 4; i++) {
                value = (value << 8) | (hashed[i] & 0xff);
            }
            return Math.abs(value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to calculate score", e);
        }
    }
}

