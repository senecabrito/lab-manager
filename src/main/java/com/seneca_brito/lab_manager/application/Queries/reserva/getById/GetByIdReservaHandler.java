package com.seneca_brito.lab_manager.application.Queries.reserva.getById;

import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReservaRepository;
import com.seneca_brito.lab_manager.shared.ENUM.RoleTypeEnum;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.UserNotAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetByIdReservaHandler {

    private final ReservaRepository reservaRepository;

    @Transactional(readOnly = true)
    public Reserva find(UUID id, Authentication authentication) {
        Reserva reserva = reservaRepository.findDetailedById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva nao encontrada"));
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleTypeEnum.ADMINISTRACAO.name()));
        if (!admin && !reserva.getUsuario().getEmail().equals(authentication.getName())) {
            throw new UserNotAuthorizedException("Reserva pertence a outro usuario");
        }
        return reserva;
    }
}
