package com.northstar.studentcard.infra.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.northstar.studentcard.domain.StudentCardApplication;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

/**
 * Publishes events to SQS. This is the async boundary between API and worker.
 */
@Service
public class SqsPublisher {

    private final SqsClient sqsClient;
    private final SqsQueueProperties props;
    private final ObjectMapper objectMapper;

    public SqsPublisher(SqsClient sqsClient, SqsQueueProperties props, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    public void publishApplicationSubmitted(StudentCardApplication application) {
        try {
            UUID applicationId = application.getApplicationId();
            SqsMessageModels.ApplicationSubmittedMessage payload =
                    new SqsMessageModels.ApplicationSubmittedMessage(applicationId);

            String body = objectMapper.writeValueAsString(payload);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(props.getApplicationQueueUrl())
                    // This is a convenient idempotency key if you later enable FIFO queue.
                   // .messageGroupId("student-card-applications") // safe even if queue is standard in LocalStack
                    //.messageDeduplicationId(applicationId.toString())
                    .messageBody(body)
                    .build();

            sqsClient.sendMessage(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish ApplicationSubmitted message", e);
        }
    }
}
