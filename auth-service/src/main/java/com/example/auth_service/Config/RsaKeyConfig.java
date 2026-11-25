package com.example.auth_service.Config;


import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaKeyConfig {

    @Value("${jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {

        String pem = Files.readString(Path.of(privateKeyPath));

        String content = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(content);

        PKCS8EncodedKeySpec keySpec =
                new PKCS8EncodedKeySpec(decoded);

        return (RSAPrivateKey) KeyFactory
                .getInstance("RSA")
                .generatePrivate(keySpec);
    }

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
    public JwtEncoder jwtEncoder(
            RSAPrivateKey privateKey,
            RSAPublicKey publicKey) throws Exception {

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("auth-key-1")
                .build();

        JWKSource<SecurityContext> jwkSource =
                (selector, context) ->
                        selector.select(new JWKSet(rsaKey));

        return new NimbusJwtEncoder(jwkSource);
    }
}