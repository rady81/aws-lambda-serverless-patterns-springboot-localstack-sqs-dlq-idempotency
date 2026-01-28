package com.northstar.studentcard.service;

import com.northstar.studentcard.StudentCardPlatformApplication;

import com.northstar.studentcard.domain.StudentCardApplication;
import com.northstar.studentcard.infra.sqs.SqsPublisher;
import com.northstar.studentcard.store.StudentCardApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Orchestrates application submission.
 * - Persists the application in DB
 * - Publishes an async message to SQS for downstream processing (credit decision)
 */
@Service
public class ApplicationService {

    private final StudentCardApplicationRepository repository;
    private final SqsPublisher sqsPublisher;

    public ApplicationService(StudentCardApplicationRepository repository, SqsPublisher sqsPublisher) {
        this.repository = repository;
        this.sqsPublisher = sqsPublisher;
    }

    public StudentCardApplication submitApplication(String fullName, String email, String nationalId, int age) {
        UUID applicationId = UUID.randomUUID();
        StudentCardApplication app = new StudentCardApplication(applicationId, fullName, email, nationalId, age);

        StudentCardApplication saved = repository.save(app);

        // Publish event for async processing (simulates "Lambda trigger" pattern).
        sqsPublisher.publishApplicationSubmitted(saved);

        return saved;
    }

    public StudentCardApplication getApplication(UUID applicationId) {
        return repository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
    }

    public void updateStatus(UUID applicationId, com.northstar.studentcard.domain.ApplicationStatus status) {
        StudentCardApplication app = getApplication(applicationId);
        app.setStatus(status);
        repository.save(app);
    }
}

