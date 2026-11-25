package com.example.proxy_service.Service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {

    private final RateLimitService rateLimitService =
            new RateLimitService();

    @Test
    void userShouldBeAllowedTenRequests() {

        for (int i = 1; i <= 10; i++) {
            assertTrue(rateLimitService.isAllowed(
                    "user@example.com", "USER"));
        }

        assertFalse(rateLimitService.isAllowed(
                "user@example.com", "USER"));
    }

    @Test
    void premiumShouldBeAllowedThirtyRequests() {

        for (int i = 1; i <= 30; i++) {
            assertTrue(rateLimitService.isAllowed(
                    "premium@example.com", "PREMIUM"));
        }

        assertFalse(rateLimitService.isAllowed(
                "premium@example.com", "PREMIUM"));
    }

    @Test
    void adminShouldBeAllowedSixtyRequests() {

        for (int i = 1; i <= 60; i++) {
            assertTrue(rateLimitService.isAllowed(
                    "admin@example.com", "ADMIN"));
        }

        assertFalse(rateLimitService.isAllowed(
                "admin@example.com", "ADMIN"));
    }

    @Test
    void unknownRoleShouldBeRejected() {

        assertFalse(rateLimitService.isAllowed(
                "unknown@example.com", "UNKNOWN"));
    }

    @Test
    void differentUsersShouldHaveSeparateLimits() {

        for (int i = 1; i <= 10; i++) {
            assertTrue(rateLimitService.isAllowed(
                    "user1@example.com", "USER"));
        }

        assertTrue(rateLimitService.isAllowed(
                "user2@example.com", "USER"));
    }
}
