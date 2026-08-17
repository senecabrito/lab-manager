package com.seneca_brito.lab_manager.application.Commands.autenticacao.login;

import com.seneca_brito.lab_manager.shared.DTOs.loginDTOs.LoginDTO;
import com.seneca_brito.lab_manager.shared.DTOs.loginDTOs.TokenResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/autenticacao")
@RequiredArgsConstructor
public class LoginCommand {

    private final LoginHandler loginHandler;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginDTO dto) throws Exception {
        TokenResponseDTO token = loginHandler.login(dto);

        return ResponseEntity.ok().body(token);
    }
}
