package com.seneca_brito.lab_manager.application.Commands.autenticacao.login;

import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import com.seneca_brito.lab_manager.infrastructure.security.config.TokenProvider;
import com.seneca_brito.lab_manager.shared.DTOs.loginDTOs.LoginDTO;
import com.seneca_brito.lab_manager.shared.DTOs.loginDTOs.TokenResponseDTO;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
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

    public TokenResponseDTO login(LoginDTO dto) throws Exception {
        try{
            Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.email(), dto.senha()));
            String token = tokenProvider.gerarToken(authenticate);

            return new TokenResponseDTO(token, expirationtime);
        }catch(BadCredentialsException e){
            throw new BadRequestException("Credenciais inválidas");
        }catch (Exception e){
            throw e;
        }
    }
}
