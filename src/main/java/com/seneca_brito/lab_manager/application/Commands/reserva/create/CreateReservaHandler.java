package com.seneca_brito.lab_manager.application.Commands.reserva.create;

import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.application.services.ReservaPolicy;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReservaRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaRequestDTO;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import com.seneca_brito.lab_manager.shared.exceptions.ConflitoEstadoException;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateReservaHandler {

    private final ReservaRepository reservaRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaPolicy reservaPolicy;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reserva create(ReservaRequestDTO dto, String authenticatedEmail) {
        Usuario usuario = usuarioRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario autenticado nao encontrado"));
        Laboratorio laboratorio = laboratorioRepository.findAllByIdForUpdate(List.of(dto.laboratorioId()))
                .stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Laboratorio nao encontrado"));
        reservaPolicy.validate(laboratorio, dto.dataReserva(), dto.horarioInicio(),
                dto.horarioFim(), dto.quantidadeAlunos());
        if (reservaRepository.existsConflito(laboratorio.getId(), dto.dataReserva(),
                dto.horarioInicio(), dto.horarioFim(), reservaPolicy.estadosQueBloqueiam(), null)) {
            throw new ConflitoEstadoException("Horario indisponivel para o laboratorio");
        }
        Reserva reserva = new Reserva();
        reserva.setDataReserva(dto.dataReserva());
        reserva.setHorarioInicio(dto.horarioInicio());
        reserva.setHorarioFim(dto.horarioFim());
        reserva.setQuantidadeAlunos(dto.quantidadeAlunos());
        reserva.setObservacao(dto.observacao());
        reserva.setStatus(StatusReserva.PENDENTE);
        reserva.setUsuario(usuario);
        reserva.setLaboratorio(laboratorio);
        return reservaRepository.saveAndFlush(reserva);
    }
}
