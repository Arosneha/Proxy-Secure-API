package com.example.proxy_service.Controller;

import com.example.proxy_service.Entity.ApiUsage;
import com.example.proxy_service.Repository.ApiUsageRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminUsageController {

    private final ApiUsageRepository apiUsageRepository;

    public AdminUsageController(ApiUsageRepository apiUsageRepository) {
        this.apiUsageRepository = apiUsageRepository;
    }

    @GetMapping("/usage")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ApiUsage> getAllUsage() {
        return apiUsageRepository.findAll();
    }
}
