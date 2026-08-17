package com.seneca_brito.lab_manager.application.Commands.autenticacao.login;

import com.seneca_brito.lab_manager.shared.DTOs.loginDTOs.LoginDTO;
import com.seneca_brito.lab_manager.shared.DTOs.loginDTOs.TokenResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/autenticacao")
@RequiredArgsConstructor
public class LoginCommand {

    private final LoginHandler loginHandler;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginDTO dto) throws Exception {
        loginHandler.login(dto);

        return ResponseEntity.ok().build();
    }
}
