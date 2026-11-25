
package com.example.proxy_service.Service;

import com.example.proxy_service.Dto.UsageResponse;
import com.example.proxy_service.Entity.ApiUsage;
import com.example.proxy_service.Repository.ApiUsageRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsageTrackingServiceTest {

    @Mock
    private ApiUsageRepository apiUsageRepository;

    @InjectMocks
    private UsageTrackingService usageTrackingService;

    @Test
    void shouldRecordUserRequest() {

        String email = "user@example.com";

        usageTrackingService.recordRequest(email);

        verify(apiUsageRepository, times(1))
                .incrementRequestCount(email);
    }

    @Test
    void shouldReturnExistingUserUsage() {

        String email = "user@example.com";

        ApiUsage usage = new ApiUsage();
        usage.setUserEmail(email);
        usage.setRequestCount(15L);

        when(apiUsageRepository.findByUserEmail(email))
                .thenReturn(Optional.of(usage));

        UsageResponse response =
                usageTrackingService.getUserUsage(email);

        assertEquals(email, response.getUserEmail());
        assertEquals(15L, response.getRequestCount());

        verify(apiUsageRepository).findByUserEmail(email);
    }

    @Test
    void shouldReturnZeroWhenUserHasNoUsage() {

        String email = "newuser@example.com";

        when(apiUsageRepository.findByUserEmail(email))
                .thenReturn(Optional.empty());

        UsageResponse response =
                usageTrackingService.getUserUsage(email);

        assertEquals(email, response.getUserEmail());
        assertEquals(0L, response.getRequestCount());
    }
}
