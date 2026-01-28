package com.northstar.studentcard.api;

import com.northstar.studentcard.StudentCardPlatformApplication;
import com.northstar.studentcard.api.dto.CreateApplicationRequest;
import com.northstar.studentcard.api.dto.CreateApplicationResponse;
import com.northstar.studentcard.domain.StudentCardApplication;
import com.northstar.studentcard.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST endpoints for students to submit applications.
 */

@RestController
@RequestMapping("/api/applications")
public class StudentCardApplicationController {
    private final ApplicationService applicationService;

    public StudentCardApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<CreateApplicationResponse> submit(
            @Valid @RequestBody CreateApplicationRequest request) {
        StudentCardApplication saved = applicationService.submitApplication(
                request.getFullName(),
                request.getEmail(),
                request.getNationalId(),
                request.getAge()
        );
        return ResponseEntity.ok(new CreateApplicationResponse(saved.getApplicationId(), saved.getStatus()));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<StudentCardApplication> get(@PathVariable UUID applicationId) {
        return ResponseEntity.ok(applicationService.getApplication(applicationId));

    }

}
