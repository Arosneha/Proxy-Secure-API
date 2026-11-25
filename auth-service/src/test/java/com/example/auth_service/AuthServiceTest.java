
package com.example.auth_service;

import com.example.auth_service.Dto.LoginRequest;
import com.example.auth_service.Dto.LoginResponse;
import com.example.auth_service.Dto.RegisterRequest;
import com.example.auth_service.Dto.UpdateRoleRequest;
import com.example.auth_service.Entity.User;
import com.example.auth_service.Enums.Role;
import com.example.auth_service.Exception.DuplicateResourceException;
import com.example.auth_service.Repository.UserRepository;
import com.example.auth_service.Service.AuthenticationService;
import com.example.auth_service.Service.JwtService;
import com.example.auth_service.Service.RegistrationService;
import com.example.auth_service.Service.RoleManagementService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtEncoder jwtEncoder;

    private JwtService jwtService;
    private RegistrationService registrationService;
    private AuthenticationService authenticationService;
    private RoleManagementService roleManagementService;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtEncoder = mock(JwtEncoder.class);

        jwtService = new JwtService(jwtEncoder);

        registrationService =
                new RegistrationService(userRepository, passwordEncoder);

        authenticationService =
                new AuthenticationService(
                        userRepository,
                        passwordEncoder,
                        jwtService
                );

        roleManagementService =
                new RoleManagementService(userRepository);
    }

    // TEST 1: Successful registration
    @Test
    void registerShouldSaveUserSuccessfully() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("  alice  ");
        request.setEmail("  ALICE@EXAMPLE.COM ");
        request.setPassword("Password123!");

        when(userRepository.existsByUsername("alice"))
                .thenReturn(false);

        when(userRepository.existsByEmail("alice@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password123!"))
                .thenReturn("bcrypt-hash");

        registrationService.register(request);

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("alice")
                        && user.getEmail().equals("alice@example.com")
                        && user.getPassword().equals("bcrypt-hash")
                        && user.getRole() == Role.USER
        ));
    }

    // TEST 2: Duplicate username
    @Test
    void registerShouldRejectDuplicateUsername() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("Password123!");

        when(userRepository.existsByUsername("alice"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> registrationService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    // TEST 3: Duplicate email
    @Test
    void registerShouldRejectDuplicateEmail() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("ALICE@EXAMPLE.COM");
        request.setPassword("Password123!");

        when(userRepository.existsByUsername("alice"))
                .thenReturn(false);

        when(userRepository.existsByEmail("alice@example.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> registrationService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    // TEST 4: Successful login
    @Test
    void loginShouldReturnJwtForValidCredentials() {

        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("bcrypt-hash");
        user.setRole(Role.USER);

        LoginRequest request = new LoginRequest();
        request.setEmail("ALICE@EXAMPLE.COM");
        request.setPassword("Password123!");

        Jwt encodedJwt = mock(Jwt.class);

        when(userRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password123!",
                "bcrypt-hash"
        )).thenReturn(true);

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(encodedJwt);

        when(encodedJwt.getTokenValue())
                .thenReturn("signed.jwt.token");

        LoginResponse response = authenticationService.login(request);

        assertEquals("signed.jwt.token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900, response.getExpiresIn());
    }

    // TEST 5: Incorrect password
    @Test
    void loginShouldRejectIncorrectPassword() {

        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("bcrypt-hash");

        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("wrong-password");

        when(userRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "bcrypt-hash"
        )).thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.login(request)
        );

        verify(jwtEncoder, never())
                .encode(any(JwtEncoderParameters.class));
    }

    // TEST 6: Unknown email
    @Test
    void loginShouldRejectUnknownEmail() {

        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("Password123!");

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.login(request)
        );

        verify(jwtEncoder, never())
                .encode(any(JwtEncoderParameters.class));
    }

    // TEST 7: JWT claims verification
    @Test
    void jwtShouldContainCorrectClaims() {

        User user = new User();
        user.setEmail("alice@example.com");
        user.setRole(Role.PREMIUM);

        Jwt encodedJwt = mock(Jwt.class);

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(encodedJwt);

        when(encodedJwt.getTokenValue())
                .thenReturn("signed.jwt.token");

        jwtService.generateToken(user);

        var captor =
                org.mockito.ArgumentCaptor.forClass(
                        JwtEncoderParameters.class
                );

        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();

        assertEquals(
                "secure-api-auth-service",
                claims.getClaimAsString("iss")
        );

        assertEquals("alice@example.com", claims.getSubject());

        assertEquals("PREMIUM", claims.getClaimAsString("role"));

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiresAt());

        assertEquals(
                900,
                claims.getExpiresAt().getEpochSecond()
                        - claims.getIssuedAt().getEpochSecond()
        );
    }

    // TEST 8: Update USER to PREMIUM
    @Test
    void shouldUpdateUserRoleToPremium() {

        User user = new User();
        user.setEmail("alice@example.com");
        user.setRole(Role.USER);

        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole(Role.PREMIUM);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        User result = roleManagementService.updateUserRole(
                10L,
                request,
                "admin@example.com"
        );

        assertEquals(Role.PREMIUM, result.getRole());

        verify(userRepository).save(user);
    }

    // TEST 9: Reject ADMIN role assignment
    @Test
    void shouldRejectAdminRoleAssignment() {

        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole(Role.ADMIN);

        assertThrows(
                IllegalArgumentException.class,
                () -> roleManagementService.updateUserRole(
                        10L,
                        request,
                        "admin@example.com"
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }

    // TEST 10: Admin cannot change their own role
    @Test
    void adminShouldNotChangeOwnRole() {

        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);

        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole(Role.PREMIUM);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        assertThrows(
                IllegalArgumentException.class,
                () -> roleManagementService.updateUserRole(
                        1L,
                        request,
                        "ADMIN@example.com"
                )
        );

        verify(userRepository, never()).save(any(User.class));
    }
}
