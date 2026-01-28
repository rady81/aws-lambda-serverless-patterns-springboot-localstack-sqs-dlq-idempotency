package com.northstar.studentcard.store;

import com.northstar.studentcard.domain.StudentCardApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Data access layer for applications.
 */
public interface StudentCardApplicationRepository extends JpaRepository<StudentCardApplication, UUID> {
}
