package com.seneca_brito.lab_manager.infrastructure.security.config;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.shared.ENUM.TipoDeUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenProviderTest {

    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "expirationtime", 900000L);
        ReflectionTestUtils.setField(tokenProvider, "key",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
    }

    @Test
    void generatesTokenFromUsuarioPrincipal() {
        Usuario usuario = Usuario.builder()
                .nome("Usuario Teste")
                .email("usuario@example.com")
                .senha("encoded")
                .curso("Computacao")
                .tipoDeUsuarios(TipoDeUsuarios.PROF)
                .build();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        String token = tokenProvider.gerarToken(authentication);

        assertTrue(tokenProvider.isTokenValid(token));
        assertEquals(usuario.getEmail(), tokenProvider.getUsernameFromToken(token));
    }
}
