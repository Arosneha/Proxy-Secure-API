package com.example.auth_service.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
public class SecurityTestController {

    @GetMapping("/api/test/security")
    public String testSecurity(@AuthenticationPrincipal Jwt jwt) {
        return "Authenticated successfully: " + jwt.getSubject();
    }
}

