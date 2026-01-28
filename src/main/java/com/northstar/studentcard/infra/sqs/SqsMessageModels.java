package com.northstar.studentcard.infra.sqs;

import java.util.UUID;

/**
 * Message payloads sent through SQS.
 * These are intentionally small: we pass identifiers, not full customer data.
 */
public class SqsMessageModels {

    public static class ApplicationSubmittedMessage {
        private UUID applicationId;

        public ApplicationSubmittedMessage() { }

        public ApplicationSubmittedMessage(UUID applicationId) {
            this.applicationId = applicationId;
        }

        public UUID getApplicationId() { return applicationId; }
        public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    }
}
