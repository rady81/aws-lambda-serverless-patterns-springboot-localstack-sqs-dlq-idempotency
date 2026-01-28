package com.northstar.studentcard.infra.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

/**
 * DLQ monitor.
 * In production you would alert via CloudWatch, PagerDuty, email, etc.
 * Locally, we log poison messages so you can see DLQ flow working.
 */
@EnableScheduling
@Component
public class SqsDlqPoller {

    private static final Logger log = LoggerFactory.getLogger(SqsDlqPoller.class);

    private final SqsClient sqsClient;
    private final SqsQueueProperties props;

    public SqsDlqPoller(SqsClient sqsClient, SqsQueueProperties props) {
        this.sqsClient = sqsClient;
        this.props = props;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollDlq() {
        ReceiveMessageRequest req = ReceiveMessageRequest.builder()
                .queueUrl(props.getApplicationDlqUrl())
                .maxNumberOfMessages(3)
                .waitTimeSeconds(2)
                .build();

        List<Message> messages = sqsClient.receiveMessage(req).messages();
        for (Message msg : messages) {
            log.error("DLQ message received. messageId={} body={}", msg.messageId(), msg.body());
            // We do not auto-delete DLQ messages here.
            // In real systems you typically route them to a quarantine store and delete after processing.
        }
    }
}
