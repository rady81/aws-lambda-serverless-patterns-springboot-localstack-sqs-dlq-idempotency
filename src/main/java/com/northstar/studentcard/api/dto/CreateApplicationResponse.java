package com.northstar.studentcard.api.dto;

import com.northstar.studentcard.domain.ApplicationStatus;

import java.util.UUID;

/**
 * API response after submission.
 */
public class CreateApplicationResponse {
    private UUID applicationId;
    private ApplicationStatus status;

    public CreateApplicationResponse(UUID applicationId, ApplicationStatus status) {
        this.applicationId = applicationId;
        this.status = status;
    }

    public UUID getApplicationId() { return applicationId; }
    public ApplicationStatus getStatus() { return status; }
}
