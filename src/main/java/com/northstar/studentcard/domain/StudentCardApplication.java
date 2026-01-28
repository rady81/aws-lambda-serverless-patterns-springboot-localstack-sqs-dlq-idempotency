package com.northstar.studentcard.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity stored in local H2 database.
 * In a real system this would likely be a persistent database like PostgreSQL.
 */
@Entity
@Table(name="student_card_application")
public class StudentCardApplication {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID applicationId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nationalId;

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected StudentCardApplication() { }

    public StudentCardApplication(UUID applicationId, String fullName, String email, String nationalId, int age) {
        this.applicationId = applicationId;
        this.fullName = fullName;
        this.email = email;
        this.nationalId = nationalId;
        this.age = age;
        this.status = ApplicationStatus.SUBMITTED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getApplicationId() { return applicationId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getNationalId() { return nationalId; }
    public int getAge() { return age; }
    public ApplicationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
