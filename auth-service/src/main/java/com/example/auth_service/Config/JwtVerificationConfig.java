package com.example.auth_service.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtVerificationConfig {

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(publicKey)
                .build();

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        new JwtTimestampValidator(),
                        new JwtIssuerValidator(
                                "secure-api-auth-service"
                        )
                )
        );

        return decoder;
    }
}