package com.northstar.studentcard;

/**
 * Add this helper file (test-only) to keep test clean:
 */
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;

import java.net.URI;
import java.util.Map;

/**
 * Test helper: creates SQS main queue + DLQ and sets redrive policy inside a LocalStack container.
 */
public final class TestQueueBootstrapper {

    private TestQueueBootstrapper() { }

    public static void ensureQueuesExist(String endpoint, String region, String accessKey, String secretKey) {
        try (SqsClient sqs = SqsClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build()) {

            sqs.createQueue(CreateQueueRequest.builder().queueName("student-card-application-dlq").build());
            sqs.createQueue(CreateQueueRequest.builder().queueName("student-card-application-queue").build());

            String dlqUrl = sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName("student-card-application-dlq").build()).queueUrl();
            String mainUrl = sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName("student-card-application-queue").build()).queueUrl();

            String dlqArn = sqs.getQueueAttributes(GetQueueAttributesRequest.builder()
                            .queueUrl(dlqUrl)
                            .attributeNames(QueueAttributeName.QUEUE_ARN)
                            .build())
                    .attributes()
                    .get(QueueAttributeName.QUEUE_ARN);

            String redrivePolicy = "{\"deadLetterTargetArn\":\"" + dlqArn + "\",\"maxReceiveCount\":\"3\"}";

            sqs.setQueueAttributes(SetQueueAttributesRequest.builder()
                    .queueUrl(mainUrl)
                    .attributes(Map.of(
                            QueueAttributeName.REDRIVE_POLICY, redrivePolicy,
                            QueueAttributeName.VISIBILITY_TIMEOUT, "15"
                    ))
                    .build());
        }
    }
}
