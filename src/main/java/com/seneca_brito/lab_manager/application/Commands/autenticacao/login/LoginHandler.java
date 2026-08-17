package com.seneca_brito.lab_manager.application.Commands.autenticacao.login;

import com.seneca_brito.lab_manager.infrastructure.security.config.TokenProvider;
import com.seneca_brito.lab_manager.shared.DTOs.loginDTOs.LoginDTO;
import com.seneca_brito.lab_manager.shared.DTOs.loginDTOs.TokenResponseDTO;
import com.seneca_brito.lab_manager.shared.exceptions.RequisicaoInvalidaException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginHandler {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @Value("${jwt.expiration}")
    private Long expirationtime;

    public TokenResponseDTO login(LoginDTO dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.senha()));
            return new TokenResponseDTO(tokenProvider.gerarToken(authentication), expirationtime);
        } catch (BadCredentialsException e) {
            throw new RequisicaoInvalidaException("Credenciais invalidas");
        }
    }
}
