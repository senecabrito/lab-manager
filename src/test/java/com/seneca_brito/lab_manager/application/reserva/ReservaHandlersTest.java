package com.seneca_brito.lab_manager.application.reserva;

import com.seneca_brito.lab_manager.application.Commands.reserva.create.CreateReservaHandler;
import com.seneca_brito.lab_manager.application.Commands.reserva.delete.DeleteReservaHandler;
import com.seneca_brito.lab_manager.application.Commands.reserva.status.ReservaStatusHandler;
import com.seneca_brito.lab_manager.application.Commands.reserva.update.UpdateReservaHandler;
import com.seneca_brito.lab_manager.application.services.ReservaPolicy;
import com.seneca_brito.lab_manager.domain.*;
import com.seneca_brito.lab_manager.infrastructure.repositories.*;
import com.seneca_brito.lab_manager.infrastructure.security.RolesEntity;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaRequestDTO;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaUpdateDTO;
import com.seneca_brito.lab_manager.shared.ENUM.*;
import com.seneca_brito.lab_manager.shared.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaHandlersTest {

    @Mock ReservaRepository reservaRepository;
    @Mock LaboratorioRepository laboratorioRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ReservaPolicy policy;
    private Usuario owner;
    private Laboratorio lab;
    private Reserva reserva;
    private ReservaRequestDTO request;

    @BeforeEach
    void setUp() {
        owner = Usuario.builder().id(UUID.randomUUID()).email("owner@example.com")
                .nome("Owner").senha("hash").curso("Curso").matricula("M1")
                .tipoDeUsuarios(TipoDeUsuarios.PROF).build();
        lab = new Laboratorio();
        lab.setId(UUID.randomUUID());
        lab.setCapacidade(40);
        reserva = new Reserva();
        reserva.setId(UUID.randomUUID());
        reserva.setUsuario(owner);
        reserva.setLaboratorio(lab);
        reserva.setDataReserva(LocalDate.of(2026, 8, 25));
        reserva.setHorarioInicio(LocalTime.of(10, 0));
        reserva.setHorarioFim(LocalTime.of(11, 0));
        reserva.setQuantidadeAlunos(20);
        reserva.setStatus(StatusReserva.PENDENTE);
        request = new ReservaRequestDTO(reserva.getDataReserva(), reserva.getHorarioInicio(),
                reserva.getHorarioFim(), lab.getId(), 20, "Aula pratica");
        lenient().when(policy.estadosQueBloqueiam())
                .thenReturn(Set.of(StatusReserva.PENDENTE, StatusReserva.APROVADA));
    }

    @Test
    void creationUsesAuthenticatedOwnerPersistsObservationAndPendingStatus() {
        when(usuarioRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(laboratorioRepository.findAllByIdForUpdate(List.of(lab.getId()))).thenReturn(List.of(lab));
        when(reservaRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        Reserva result = createHandler().create(request, owner.getEmail());
        assertAll(
                () -> assertSame(owner, result.getUsuario()),
                () -> assertSame(lab, result.getLaboratorio()),
                () -> assertEquals(StatusReserva.PENDENTE, result.getStatus()),
                () -> assertEquals(20, result.getQuantidadeAlunos()),
                () -> assertEquals("Aula pratica", result.getObservacao()));
        verify(policy).validate(lab, request.dataReserva(), request.horarioInicio(),
                request.horarioFim(), request.quantidadeAlunos());
    }

    @Test
    void sequentialConflictIsRejectedBeforeInsert() {
        when(usuarioRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(laboratorioRepository.findAllByIdForUpdate(any())).thenReturn(List.of(lab));
        when(reservaRepository.existsConflito(any(), any(), any(), any(), anyCollection(), isNull()))
                .thenReturn(true);
        assertThrows(ConflitoEstadoException.class,
                () -> createHandler().create(request, owner.getEmail()));
        verify(reservaRepository, never()).saveAndFlush(any());
    }

    @Test
    void updatePreservesMissingFieldsAndExcludesItselfFromConflict() {
        ReservaUpdateDTO update = new ReservaUpdateDTO(null, null, null, null, 30, null);
        when(reservaRepository.findDetailedById(reserva.getId())).thenReturn(Optional.of(reserva));
        when(laboratorioRepository.findAllByIdForUpdate(any())).thenReturn(List.of(lab));
        when(reservaRepository.findByIdForUpdate(reserva.getId())).thenReturn(Optional.of(reserva));
        when(reservaRepository.saveAndFlush(reserva)).thenReturn(reserva);
        Reserva result = updateHandler().update(reserva.getId(), update, userAuth(owner.getEmail()));
        assertEquals(30, result.getQuantidadeAlunos());
        assertEquals(LocalTime.of(10, 0), result.getHorarioInicio());
        verify(reservaRepository).existsConflito(eq(lab.getId()), eq(reserva.getDataReserva()),
                eq(reserva.getHorarioInicio()), eq(reserva.getHorarioFim()), anyCollection(),
                eq(reserva.getId()));
    }

    @Test
    void userCannotUpdateAnotherUsersReservation() {
        when(reservaRepository.findDetailedById(reserva.getId())).thenReturn(Optional.of(reserva));
        assertThrows(UserNotAuthorizedException.class, () -> updateHandler().update(reserva.getId(),
                new ReservaUpdateDTO(null, null, null, null, 10, null), userAuth("other@example.com")));
    }

    @Test
    void terminalReservationCannotBeEdited() {
        reserva.setStatus(StatusReserva.CANCELADA);
        when(reservaRepository.findDetailedById(reserva.getId())).thenReturn(Optional.of(reserva));
        when(laboratorioRepository.findAllByIdForUpdate(any())).thenReturn(List.of(lab));
        when(reservaRepository.findByIdForUpdate(reserva.getId())).thenReturn(Optional.of(reserva));
        assertThrows(ConflitoEstadoException.class, () -> updateHandler().update(reserva.getId(),
                new ReservaUpdateDTO(null, null, null, null, 10, null), userAuth(owner.getEmail())));
    }

    @Test
    void ownerCanCancelAndCalendarStateIsReleased() {
        when(reservaRepository.findByIdForUpdate(reserva.getId())).thenReturn(Optional.of(reserva));
        when(reservaRepository.saveAndFlush(reserva)).thenReturn(reserva);
        Reserva result = new DeleteReservaHandler(reservaRepository)
                .cancel(reserva.getId(), userAuth(owner.getEmail()));
        assertEquals(StatusReserva.CANCELADA, result.getStatus());
        assertFalse(result.getStatus().bloqueiaHorario());
    }

    @Test
    void cancellationOfRejectedReservationIsRejected() {
        reserva.setStatus(StatusReserva.REJEITADA);
        when(reservaRepository.findByIdForUpdate(reserva.getId())).thenReturn(Optional.of(reserva));
        assertThrows(ConflitoEstadoException.class, () -> new DeleteReservaHandler(reservaRepository)
                .cancel(reserva.getId(), userAuth(owner.getEmail())));
    }

    @Test
    void adminApprovesPendingReservationAfterRecheckingApprovedConflicts() {
        when(reservaRepository.findDetailedById(reserva.getId())).thenReturn(Optional.of(reserva));
        when(laboratorioRepository.findAllByIdForUpdate(any())).thenReturn(List.of(lab));
        when(reservaRepository.findByIdForUpdate(reserva.getId())).thenReturn(Optional.of(reserva));
        when(reservaRepository.saveAndFlush(reserva)).thenReturn(reserva);
        Reserva result = statusHandler().approve(reserva.getId());
        assertEquals(StatusReserva.APROVADA, result.getStatus());
        verify(reservaRepository).existsConflito(any(), any(), any(), any(),
                eq(Set.of(StatusReserva.APROVADA)), eq(reserva.getId()));
    }

    @Test
    void approvalConflictReturnsConflictAndKeepsPending() {
        when(reservaRepository.findDetailedById(reserva.getId())).thenReturn(Optional.of(reserva));
        when(laboratorioRepository.findAllByIdForUpdate(any())).thenReturn(List.of(lab));
        when(reservaRepository.findByIdForUpdate(reserva.getId())).thenReturn(Optional.of(reserva));
        when(reservaRepository.existsConflito(any(), any(), any(), any(), anyCollection(), any()))
                .thenReturn(true);
        assertThrows(ConflitoEstadoException.class, () -> statusHandler().approve(reserva.getId()));
        assertEquals(StatusReserva.PENDENTE, reserva.getStatus());
    }

    @Test
    void adminRejectsPendingButCannotApproveRejectedReservation() {
        when(reservaRepository.findByIdForUpdate(reserva.getId())).thenReturn(Optional.of(reserva));
        when(reservaRepository.saveAndFlush(reserva)).thenReturn(reserva);
        ReservaStatusHandler handler = statusHandler();
        assertEquals(StatusReserva.REJEITADA, handler.reject(reserva.getId()).getStatus());
        when(reservaRepository.findDetailedById(reserva.getId())).thenReturn(Optional.of(reserva));
        when(laboratorioRepository.findAllByIdForUpdate(any())).thenReturn(List.of(lab));
        assertThrows(ConflitoEstadoException.class, () -> handler.approve(reserva.getId()));
    }

    private CreateReservaHandler createHandler() {
        return new CreateReservaHandler(reservaRepository, laboratorioRepository, usuarioRepository, policy);
    }

    private UpdateReservaHandler updateHandler() {
        return new UpdateReservaHandler(reservaRepository, laboratorioRepository, policy);
    }

    private ReservaStatusHandler statusHandler() {
        return new ReservaStatusHandler(reservaRepository, laboratorioRepository);
    }

    private Authentication userAuth(String email) {
        return new UsernamePasswordAuthenticationToken(email, "",
                List.of(new SimpleGrantedAuthority(RoleTypeEnum.USUARIO.name())));
    }
}
