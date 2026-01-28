package com.northstar.studentcard.infra.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.northstar.studentcard.domain.ApplicationStatus;
import com.northstar.studentcard.domain.StudentCardApplication;
import com.northstar.studentcard.infra.idempotency.RedisIdempotencyService;
import com.northstar.studentcard.service.ApplicationService;
import com.northstar.studentcard.service.CreditDecisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.UUID;

/**
 * Polls SQS like a Lambda consumer (locally testable).
 *
 * Key behaviors:
 * - Reads in batches
 * - Idempotency using Redis (safe against duplicates)
 * - On success, deletes message
 * - On failure, does NOT delete message -> SQS retry happens automatically
 * - After maxReceiveCount, message goes to DLQ (configured in LocalStack init)
 */
@EnableScheduling
@Component
public class SqsWorkerPoller {

    private static final Logger log = LoggerFactory.getLogger(SqsWorkerPoller.class);

    private final SqsClient sqsClient;
    private final SqsQueueProperties props;
    private final ObjectMapper objectMapper;

    private final ApplicationService applicationService;
    private final CreditDecisionService creditDecisionService;
    private final RedisIdempotencyService idempotencyService;

    public SqsWorkerPoller(
            SqsClient sqsClient,
            SqsQueueProperties props,
            ObjectMapper objectMapper,
            ApplicationService applicationService,
            CreditDecisionService creditDecisionService,
            RedisIdempotencyService idempotencyService
    ) {
        this.sqsClient = sqsClient;
        this.props = props;
        this.objectMapper = objectMapper;
        this.applicationService = applicationService;
        this.creditDecisionService = creditDecisionService;
        this.idempotencyService = idempotencyService;
    }

    /**
     * Polls every 2 seconds.
     * Long polling wait time is configured separately (pollWaitSeconds) to reduce empty responses.
     */
    @Scheduled(fixedDelay = 2000)
    public void poll() {
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(props.getApplicationQueueUrl())
                .maxNumberOfMessages(props.getMaxMessagesPerPoll())
                .waitTimeSeconds(props.getPollWaitSeconds())
                .visibilityTimeout(props.getVisibilityTimeoutSeconds())
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();
        if (messages.isEmpty()) {
            return;
        }

        for (Message msg : messages) {
            processSingleMessage(msg);
        }
    }

    private void processSingleMessage(Message msg) {
        // Use the SQS messageId as idempotency key. In real systems you often carry a domain eventId instead.
        String idempotencyKey = "sqs:processed:" + msg.messageId();

        boolean firstTime = idempotencyService.tryMarkProcessed(idempotencyKey);
        if (!firstTime) {
            log.info("Duplicate message detected. Skipping. messageId={}", msg.messageId());
            delete(msg);
            return;
        }

        try {
            SqsMessageModels.ApplicationSubmittedMessage payload =
                    objectMapper.readValue(msg.body(), SqsMessageModels.ApplicationSubmittedMessage.class);

            UUID applicationId = payload.getApplicationId();
            StudentCardApplication app = applicationService.getApplication(applicationId);

            applicationService.updateStatus(applicationId, ApplicationStatus.UNDER_REVIEW);

            ApplicationStatus decision = creditDecisionService.decide(app);
            applicationService.updateStatus(applicationId, decision);

            log.info("Processed applicationId={} decision={}", applicationId, decision);
            delete(msg);
        } catch (Exception e) {
            // IMPORTANT: We intentionally do not delete message. SQS will retry.
            // After maxReceiveCount, it will land in DLQ.
            log.warn("Processing failed. Message will be retried. messageId={} error={}", msg.messageId(), e.getMessage());
        }
    }

    private void delete(Message msg) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(props.getApplicationQueueUrl())
                .receiptHandle(msg.receiptHandle())
                .build();

        sqsClient.deleteMessage(deleteRequest);
    }
}
