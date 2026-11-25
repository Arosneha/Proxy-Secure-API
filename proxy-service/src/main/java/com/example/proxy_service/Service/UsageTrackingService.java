
package com.example.proxy_service.Service;

import com.example.proxy_service.Dto.UsageResponse;
import com.example.proxy_service.Entity.ApiUsage;
import com.example.proxy_service.Repository.ApiUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsageTrackingService {

    private final ApiUsageRepository apiUsageRepository;

    public UsageTrackingService(ApiUsageRepository apiUsageRepository) {
        this.apiUsageRepository = apiUsageRepository;
    }

    @Transactional
    public void recordRequest(String userEmail) {
        apiUsageRepository.incrementRequestCount(userEmail);
    }

    @Transactional(readOnly = true)
    public UsageResponse getUserUsage(String userEmail) {

        ApiUsage usage = apiUsageRepository
                .findByUserEmail(userEmail)
                .orElse(null);

        if (usage == null) {
            return new UsageResponse(userEmail, 0L);
        }

        return new UsageResponse(
                usage.getUserEmail(),
                usage.getRequestCount()
        );
    }
}
