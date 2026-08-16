package com.seneca_brito.lab_manager.infrastructure.security.config;

import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioResponseDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class TokenProvider {

    @Value("${jwt.expiration}")
    private Long expirationtime;

    @Value("${jwt.key}")
    private String key;

    //gerar token
    public String gerarToken(Authentication authentication) {
        UsuarioResponseDTO usuario = (UsuarioResponseDTO) authentication.getPrincipal();
       return buildToken(usuario.email());
    }

    public String buildToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationtime);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey()).compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(key.getBytes());
    }
    //validar token
    public boolean isTokenValid(String token) {
        try{
            getClaimsFromToken(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        //validar assinatura
        //validar expiracao
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //extrair informacoes
    private String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
}
