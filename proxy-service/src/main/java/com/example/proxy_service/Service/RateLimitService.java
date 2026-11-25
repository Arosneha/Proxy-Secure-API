
package com.example.proxy_service.Service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    private static final long WINDOW_SIZE_MS = 60_000;

    private final ConcurrentHashMap<String, UserRequestCounter> requestCounters =
            new ConcurrentHashMap<>();

    public boolean isAllowed(String userEmail, String role) {

        int limit = switch (role) {
            case "USER" -> 10;
            case "PREMIUM" -> 30;
            case "ADMIN" -> 60;
            default -> 0;
        };

        if (limit == 0) {
            return false;
        }

        long currentTime = System.currentTimeMillis();

        UserRequestCounter counter = requestCounters.compute(
                userEmail,
                (email, existingCounter) -> {
                    if (existingCounter == null ||
                            currentTime - existingCounter.windowStart >= WINDOW_SIZE_MS) {
                        return new UserRequestCounter(currentTime);
                    }

                    return existingCounter;
                }
        );

        return counter.requestCount.incrementAndGet() <= limit;
    }

    private static class UserRequestCounter {

        private final long windowStart;
        private final AtomicInteger requestCount = new AtomicInteger(0);

        private UserRequestCounter(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
