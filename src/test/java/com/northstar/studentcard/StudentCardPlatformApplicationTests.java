package com.northstar.studentcard;

import com.northstar.studentcard.api.dto.CreateApplicationRequest;
import com.northstar.studentcard.domain.ApplicationStatus;
import com.northstar.studentcard.store.StudentCardApplicationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentCardPlatformIntegrationTest {

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.5"))
            .withServices(SQS);

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2"))
            .withExposedPorts(6379);

    @LocalServerPort
    int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    private final StudentCardApplicationRepository repository;

    StudentCardPlatformIntegrationTest(StudentCardApplicationRepository repository) {
        this.repository = repository;
    }

    @Test
    void submitApplication_and_workerProcesses_it() throws Exception {
        // Override properties dynamically by system props used by Spring Boot.
        System.setProperty("studentcard.sqs.endpoint", localstack.getEndpointOverride(SQS).toString());
        System.setProperty("studentcard.sqs.region", localstack.getRegion());
        System.setProperty("studentcard.sqs.accessKey", localstack.getAccessKey());
        System.setProperty("studentcard.sqs.secretKey", localstack.getSecretKey());

        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", String.valueOf(redis.getMappedPort(6379)));

        // Create queues inside LocalStack for the test.
        // (We do it here rather than relying on init scripts because Testcontainers runs its own container lifecycle.)
        TestQueueBootstrapper.ensureQueuesExist(
                localstack.getEndpointOverride(SQS).toString(),
                localstack.getRegion(),
                localstack.getAccessKey(),
                localstack.getSecretKey()
        );

        CreateApplicationRequest req = new CreateApplicationRequest();
        req.setFullName("Aarav Mehta");
        req.setEmail("aarav.mehta@northstar.edu");
        req.setNationalId("ID-45012"); // not ending with 99 so it should process successfully
        req.setAge(20);

        String url = "http://localhost:" + port + "/api/applications";

        ResponseEntity<String> response = restTemplate.postForEntity(url, req, String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        // Wait for async worker to process (poller runs on schedule).
        // We'll just wait and assert that at least one application moves to APPROVED/DECLINED.
        long deadline = System.currentTimeMillis() + Duration.ofSeconds(20).toMillis();

        boolean processed = false;
        while (System.currentTimeMillis() < deadline) {
            processed = repository.findAll().stream()
                    .anyMatch(a -> a.getStatus() == ApplicationStatus.APPROVED || a.getStatus() == ApplicationStatus.DECLINED);
            if (processed) break;
            Thread.sleep(500);
        }

        assertThat(processed).isTrue();
    }
}
