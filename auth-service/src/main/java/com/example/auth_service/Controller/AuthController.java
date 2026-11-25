package com.example.auth_service.Controller;

import com.example.auth_service.Dto.RegisterRequest;
import com.example.auth_service.Dto.RegisterResponse;
import com.example.auth_service.Service.RegistrationService;
import com.example.auth_service.Dto.LoginRequest;
import com.example.auth_service.Dto.LoginResponse;
import com.example.auth_service.Service.AuthenticationService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;

    public AuthController(RegistrationService registrationService, AuthenticationService authenticationService) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        registrationService.register(request);

        RegisterResponse response = new RegisterResponse(
                "User registered successfully"
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authenticationService.login(request);

        return ResponseEntity.ok(response);
    }
}
