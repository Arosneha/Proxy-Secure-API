
package com.example.proxy_service.Controller;

import com.example.proxy_service.Dto.UsageResponse;
import com.example.proxy_service.Service.UsageTrackingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage")
public class UsageController {

    private final UsageTrackingService usageTrackingService;

    public UsageController(UsageTrackingService usageTrackingService) {
        this.usageTrackingService = usageTrackingService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM', 'ADMIN')")
    public UsageResponse getMyUsage(
            @AuthenticationPrincipal Jwt jwt) {

        String userEmail = jwt.getSubject();

        return usageTrackingService.getUserUsage(userEmail);
    }
}
