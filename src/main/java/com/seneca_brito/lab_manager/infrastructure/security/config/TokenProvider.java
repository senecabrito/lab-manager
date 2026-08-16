package com.seneca_brito.lab_manager.infrastructure.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {

    @Value("${jwt.expiration}")
    private Long expirationtime;

    @Value("${jwt.key}")
    private Long key;
}
