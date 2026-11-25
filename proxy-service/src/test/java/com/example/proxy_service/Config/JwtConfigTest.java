
package com.example.proxy_service.Config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtConfigTest {

    private static KeyPair validKeyPair;
    private static KeyPair wrongKeyPair;

    private final JwtConfig jwtConfig = new JwtConfig();

    @BeforeAll
    static void generateTestKeys() throws Exception {

        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        validKeyPair = generator.generateKeyPair();
        wrongKeyPair = generator.generateKeyPair();
    }

    @Test
    void shouldAcceptValidJwt() throws Exception {

        JwtDecoder decoder = createDecoder(validKeyPair);

        String token = createToken(
                validKeyPair,
                "secure-api-auth-service",
                Instant.now().plusSeconds(900)
        );

        assertDoesNotThrow(() -> decoder.decode(token));
    }

    @Test
    void shouldRejectExpiredJwt() throws Exception {

        JwtDecoder decoder = createDecoder(validKeyPair);

        String token = createToken(
                validKeyPair,
                "secure-api-auth-service",
                Instant.now().minusSeconds(600)
        );

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }

    @Test
    void shouldRejectIncorrectIssuer() throws Exception {

        JwtDecoder decoder = createDecoder(validKeyPair);

        String token = createToken(
                validKeyPair,
                "wrong-issuer",
                Instant.now().plusSeconds(900)
        );

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }

    @Test
    void shouldRejectInvalidSignature() throws Exception {

        JwtDecoder decoder = createDecoder(validKeyPair);

        String token = createToken(
                wrongKeyPair,
                "secure-api-auth-service",
                Instant.now().plusSeconds(900)
        );

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }

    @Test
    void shouldConvertUserRoleCorrectly() {
        assertRoleConversion("USER", "ROLE_USER");
    }

    @Test
    void shouldConvertPremiumRoleCorrectly() {
        assertRoleConversion("PREMIUM", "ROLE_PREMIUM");
    }

    @Test
    void shouldConvertAdminRoleCorrectly() {
        assertRoleConversion("ADMIN", "ROLE_ADMIN");
    }

    private void assertRoleConversion(
            String role,
            String expectedAuthority) {

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("test@example.com")
                .claim("role", role)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        JwtAuthenticationConverter converter =
                jwtConfig.jwtAuthenticationConverter();

        Authentication authentication = converter.convert(jwt);

        assertNotNull(authentication);

        assertTrue(
                authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals(expectedAuthority))
        );
    }

    private JwtDecoder createDecoder(KeyPair keyPair) {

        return jwtConfig.jwtDecoder(
                (RSAPublicKey) keyPair.getPublic()
        );
    }

    private String createToken(
            KeyPair signingKey,
            String issuer,
            Instant expiration
    ) throws Exception {

        Instant now = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("test@example.com")
                .issuer(issuer)
                .issueTime(Date.from(now.minusSeconds(60)))
                .expirationTime(Date.from(expiration))
                .claim("role", "USER")
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.RS256),
                claims
        );

        signedJWT.sign(
                new RSASSASigner(
                        (RSAPrivateKey) signingKey.getPrivate()
                )
        );

        return signedJWT.serialize();
    }
}
