package com.seneca_brito.lab_manager.shared.mappers;

import com.seneca_brito.lab_manager.domain.RegistroAcesso;
import com.seneca_brito.lab_manager.shared.DTOs.acessoDTOs.AcessoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AcessoMapper {

    public AcessoResponseDTO toDto(RegistroAcesso acesso) {
        var reserva = acesso.getReserva();
        return new AcessoResponseDTO(acesso.getId(), reserva.getId(), reserva.getUsuario().getId(),
                reserva.getLaboratorio().getId(), reserva.getLaboratorio().getNome(),
                acesso.getCheckIn(), acesso.getCheckOut(), acesso.getStatus());
    }
}
