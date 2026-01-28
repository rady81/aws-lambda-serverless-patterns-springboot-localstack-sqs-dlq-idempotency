package com.northstar.studentcard.domain;

/**
 * Represents the current processing state of a student credit card application.
 */
public enum ApplicationStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    DECLINED,
    FAILED
}
