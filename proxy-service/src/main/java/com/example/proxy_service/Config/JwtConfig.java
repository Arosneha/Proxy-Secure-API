package com.example.proxy_service.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {

        String pem = Files.readString(Path.of(publicKeyPath));

        String content = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(content);

        X509EncodedKeySpec keySpec =
                new X509EncodedKeySpec(decoded);

        return (RSAPublicKey) KeyFactory
                .getInstance("RSA")
                .generatePublic(keySpec);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(publicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(
                        new JwtTimestampValidator(),
                        new JwtIssuerValidator("secure-api-auth-service")
                );

        decoder.setJwtValidator(validator);

        return decoder;
    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt ->
                Collections.singletonList(
                        new SimpleGrantedAuthority(
                                "ROLE_" + jwt.getClaimAsString("role")
                        )
                )
        );

        return converter;
    }
}
