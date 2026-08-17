package com.seneca_brito.lab_manager.application.reclamacao;

import com.seneca_brito.lab_manager.application.Commands.reclamacao.create.CreateReclamacaoHandler;
import com.seneca_brito.lab_manager.application.Commands.reclamacao.delete.DeleteReclamacaoHandler;
import com.seneca_brito.lab_manager.application.Commands.reclamacao.update.UpdateReclamacaoHandler;
import com.seneca_brito.lab_manager.domain.*;
import com.seneca_brito.lab_manager.infrastructure.repositories.*;
import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.*;
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

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReclamacaoHandlersTest {

    @Mock ReclamacaoRepository reclamacaoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock LaboratorioRepository laboratorioRepository;
    private Usuario owner;
    private Laboratorio lab;
    private Reclamacao reclamacao;
    private Clock clock;

    @BeforeEach
    void setUp() {
        owner = Usuario.builder().id(UUID.randomUUID()).email("owner@example.com")
                .nome("Owner").senha("hash").curso("Curso").matricula("M1")
                .tipoDeUsuarios(TipoDeUsuarios.PROF).build();
        lab = new Laboratorio();
        lab.setId(UUID.randomUUID());
        reclamacao = new Reclamacao();
        reclamacao.setId(UUID.randomUUID());
        reclamacao.setUsuario(owner);
        reclamacao.setLaboratorio(lab);
        reclamacao.setDescricao("Equipamento sem funcionar");
        reclamacao.setCategoriaProblema(CategoriaProblema.EQUIPAMENTO);
        reclamacao.setStatus(StatusReclamacao.PENDENTE);
        clock = Clock.fixed(Instant.parse("2026-08-17T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    }

    @Test
    void creationUsesJwtOwnerServerTimeAndInitialStatus() {
        when(usuarioRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(laboratorioRepository.findById(lab.getId())).thenReturn(Optional.of(lab));
        when(reclamacaoRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        ReclamacaoRequestDTO dto = new ReclamacaoRequestDTO("Falha na maquina",
                CategoriaProblema.EQUIPAMENTO, lab.getId());
        Reclamacao result = createHandler().create(dto, owner.getEmail());
        assertAll(
                () -> assertSame(owner, result.getUsuario()),
                () -> assertEquals(StatusReclamacao.PENDENTE, result.getStatus()),
                () -> assertEquals(LocalDateTime.of(2026, 8, 17, 10, 0), result.getDataReclamacao()));
    }

    @Test
    void ownerCanEditAllowedFieldsWhilePending() {
        when(reclamacaoRepository.findByIdForUpdate(reclamacao.getId())).thenReturn(Optional.of(reclamacao));
        when(reclamacaoRepository.saveAndFlush(reclamacao)).thenReturn(reclamacao);
        Reclamacao result = updateHandler().update(reclamacao.getId(),
                new ReclamacaoUpdateDTO("Nova descricao", CategoriaProblema.SOFTWARE),
                auth(owner.getEmail(), RoleTypeEnum.USUARIO));
        assertEquals("Nova descricao", result.getDescricao());
        assertEquals(CategoriaProblema.SOFTWARE, result.getCategoriaProblema());
        assertSame(owner, result.getUsuario());
    }

    @Test
    void userCannotEditThirdPartyComplaint() {
        when(reclamacaoRepository.findByIdForUpdate(reclamacao.getId())).thenReturn(Optional.of(reclamacao));
        assertThrows(UserNotAuthorizedException.class, () -> updateHandler().update(reclamacao.getId(),
                new ReclamacaoUpdateDTO("Tentativa", null),
                auth("other@example.com", RoleTypeEnum.USUARIO)));
    }

    @Test
    void commonUpdateCannotChangeAdministrativeStatus() {
        assertArrayEquals(new java.lang.reflect.RecordComponent[]{},
                Arrays.stream(ReclamacaoUpdateDTO.class.getRecordComponents())
                        .filter(component -> component.getName().equals("status"))
                        .toArray(java.lang.reflect.RecordComponent[]::new));
    }

    @Test
    void adminTransitionsPendingToAnalysisAndAnalysisToCompleted() {
        when(reclamacaoRepository.findByIdForUpdate(reclamacao.getId())).thenReturn(Optional.of(reclamacao));
        when(reclamacaoRepository.saveAndFlush(reclamacao)).thenReturn(reclamacao);
        UpdateReclamacaoHandler handler = updateHandler();
        handler.updateStatus(reclamacao.getId(),
                new ReclamacaoStatusUpdateDTO(StatusReclamacao.EM_ANALISE));
        assertEquals(StatusReclamacao.EM_ANALISE, reclamacao.getStatus());
        handler.updateStatus(reclamacao.getId(),
                new ReclamacaoStatusUpdateDTO(StatusReclamacao.CONCLUIDA));
        assertEquals(StatusReclamacao.CONCLUIDA, reclamacao.getStatus());
    }

    @Test
    void invalidAdministrativeTransitionIsRejected() {
        reclamacao.setStatus(StatusReclamacao.CONCLUIDA);
        when(reclamacaoRepository.findByIdForUpdate(reclamacao.getId())).thenReturn(Optional.of(reclamacao));
        assertThrows(ConflitoEstadoException.class, () -> updateHandler().updateStatus(
                reclamacao.getId(), new ReclamacaoStatusUpdateDTO(StatusReclamacao.EM_ANALISE)));
    }

    @Test
    void ownerCancelsPendingButCannotCancelAnalyzedComplaint() {
        when(reclamacaoRepository.findByIdForUpdate(reclamacao.getId())).thenReturn(Optional.of(reclamacao));
        when(reclamacaoRepository.saveAndFlush(reclamacao)).thenReturn(reclamacao);
        DeleteReclamacaoHandler handler = new DeleteReclamacaoHandler(reclamacaoRepository);
        assertEquals(StatusReclamacao.CANCELADA,
                handler.cancel(reclamacao.getId(), auth(owner.getEmail(), RoleTypeEnum.USUARIO)).getStatus());
        reclamacao.setStatus(StatusReclamacao.EM_ANALISE);
        assertThrows(ConflitoEstadoException.class,
                () -> handler.cancel(reclamacao.getId(), auth(owner.getEmail(), RoleTypeEnum.USUARIO)));
    }

    private CreateReclamacaoHandler createHandler() {
        return new CreateReclamacaoHandler(reclamacaoRepository, usuarioRepository,
                laboratorioRepository, clock);
    }

    private UpdateReclamacaoHandler updateHandler() {
        return new UpdateReclamacaoHandler(reclamacaoRepository);
    }

    private Authentication auth(String email, RoleTypeEnum role) {
        return new UsernamePasswordAuthenticationToken(email, "",
                List.of(new SimpleGrantedAuthority(role.name())));
    }
}
