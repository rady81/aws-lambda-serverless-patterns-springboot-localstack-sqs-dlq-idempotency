package com.northstar.studentcard.infra.sqs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Typed configuration for SQS settings. SQS - LocalStack
 */
@Component
@ConfigurationProperties(prefix = "studentcard.sqs")
public class SqsQueueProperties {

    private String endpoint;
    private String region;
    private String accessKey;
    private String secretKey;

    private String applicationQueueUrl;
    private String applicationDlqUrl;

    private int pollWaitSeconds;
    private int maxMessagesPerPoll;
    private int visibilityTimeoutSeconds;

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getApplicationQueueUrl() { return applicationQueueUrl; }
    public void setApplicationQueueUrl(String applicationQueueUrl) { this.applicationQueueUrl = applicationQueueUrl; }

    public String getApplicationDlqUrl() { return applicationDlqUrl; }
    public void setApplicationDlqUrl(String applicationDlqUrl) { this.applicationDlqUrl = applicationDlqUrl; }

    public int getPollWaitSeconds() { return pollWaitSeconds; }
    public void setPollWaitSeconds(int pollWaitSeconds) { this.pollWaitSeconds = pollWaitSeconds; }

    public int getMaxMessagesPerPoll() { return maxMessagesPerPoll; }
    public void setMaxMessagesPerPoll(int maxMessagesPerPoll) { this.maxMessagesPerPoll = maxMessagesPerPoll; }

    public int getVisibilityTimeoutSeconds() { return visibilityTimeoutSeconds; }
    public void setVisibilityTimeoutSeconds(int visibilityTimeoutSeconds) { this.visibilityTimeoutSeconds = visibilityTimeoutSeconds; }
}
