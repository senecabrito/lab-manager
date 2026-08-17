package com.seneca_brito.lab_manager.application.integration;

import com.seneca_brito.lab_manager.application.Commands.reserva.create.CreateReservaHandler;
import com.seneca_brito.lab_manager.application.Commands.reserva.status.ReservaStatusHandler;
import com.seneca_brito.lab_manager.domain.*;
import com.seneca_brito.lab_manager.infrastructure.repositories.*;
import com.seneca_brito.lab_manager.infrastructure.security.RolesEntity;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaRequestDTO;
import com.seneca_brito.lab_manager.shared.ENUM.*;
import com.seneca_brito.lab_manager.shared.exceptions.ConflitoEstadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IntegratedModulesTest {

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        if (Boolean.getBoolean("mysql.integration")) {
            registry.add("spring.datasource.url", () -> System.getProperty("mysql.url"));
            registry.add("spring.datasource.username", () -> System.getProperty("mysql.username", "admin"));
            registry.add("spring.datasource.password", () -> System.getProperty("mysql.password", "admin123"));
        } else {
            registry.add("spring.datasource.url",
                    () -> "jdbc:h2:mem:integrated_modules;MODE=MySQL;"
                            + "DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1");
            registry.add("spring.datasource.username", () -> "sa");
            registry.add("spring.datasource.password", () -> "");
            registry.add("spring.flyway.locations", () -> "classpath:db/h2migration");
        }
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolesRepository rolesRepository;
    @Autowired LaboratorioRepository laboratorioRepository;
    @Autowired ReservaRepository reservaRepository;
    @Autowired ReclamacaoRepository reclamacaoRepository;
    @Autowired CreateReservaHandler createReservaHandler;
    @Autowired ReservaStatusHandler reservaStatusHandler;

    private Usuario admin;
    private Usuario usuario;
    private String adminToken;
    private String usuarioToken;
    private LocalDate reservationDate;

    @BeforeEach
    void setUp() throws Exception {
        reclamacaoRepository.deleteAll();
        reservaRepository.deleteAll();
        laboratorioRepository.deleteAll();
        usuarioRepository.deleteAll();
        rolesRepository.deleteAll();

        RolesEntity adminRole = rolesRepository.save(RolesEntity.builder()
                .nome(RoleTypeEnum.ADMINISTRACAO.name()).build());
        RolesEntity userRole = rolesRepository.save(RolesEntity.builder()
                .nome(RoleTypeEnum.USUARIO.name()).build());
        admin = usuarioRepository.saveAndFlush(user("admin@integration.test", "ADM-I", TipoDeUsuarios.ADMIN,
                Set.of(adminRole)));
        usuario = usuarioRepository.saveAndFlush(user("user@integration.test", "USR-I", TipoDeUsuarios.PROF,
                Set.of(userRole)));
        adminToken = login(admin.getEmail());
        usuarioToken = login(usuario.getEmail());
        reservationDate = LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusDays(5);
    }

    @Test
    void laboratoryCrudFiltersAndAdministrativeAuthorizationWork() throws Exception {
        mockMvc.perform(post("/api/v1/laboratorios")
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken))
                        .contentType(MediaType.APPLICATION_JSON).content(labJson("Lab negado")))
                .andExpect(status().isForbidden());

        UUID id = createLaboratory("Lab Integrado");
        mockMvc.perform(get("/api/v1/laboratorios/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localizacao").value("Bloco A"))
                .andExpect(jsonPath("$.recursos[0]").exists());

        mockMvc.perform(get("/api/v1/laboratorios")
                        .param("capacidadeMinima", "30")
                        .param("localizacao", "bloco")
                        .param("recursos", "PROJETOR", "AR CONDICIONADO")
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(patch("/api/v1/laboratorios/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"capacidade\":45}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.capacidade").value(45));

        mockMvc.perform(delete("/api/v1/laboratorios/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void principalFlowKeepsCalendarConsistentThroughApprovalAndCancellation() throws Exception {
        UUID labId = createLaboratory("Lab Agenda");
        mockMvc.perform(get("/api/v1/laboratorios/{id}/calendario", labId)
                        .param("data", reservationDate.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots.length()").value(21))
                .andExpect(jsonPath("$.slots[5].ocupado").value(false));

        UUID reservaId = createReservation(labId, "10:00", "11:00", usuarioToken);
        mockMvc.perform(get("/api/v1/laboratorios/{id}/calendario", labId)
                        .param("data", reservationDate.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots[5].ocupado").value(true));

        if (Boolean.getBoolean("mysql.integration")) {
            assertUtilization(labId, 0, 630);
            mockMvc.perform(get("/api/v1/relatorios/ranking-laboratorios")
                            .param("dataInicial", reservationDate.toString())
                            .param("dataFinal", reservationDate.toString())
                            .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].laboratorioId").value(labId.toString()))
                    .andExpect(jsonPath("$[0].minutosOcupados").value(0));
        }

        mockMvc.perform(get("/api/v1/reservas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/reservas/{id}/aprovacao", reservaId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/reservas/{id}/aprovacao", reservaId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APROVADA"));

        if (Boolean.getBoolean("mysql.integration")) {
            assertUtilization(labId, 60, 630);
            mockMvc.perform(get("/api/v1/relatorios/ranking-laboratorios")
                            .param("dataInicial", reservationDate.toString())
                            .param("dataFinal", reservationDate.toString())
                            .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].laboratorioId").value(labId.toString()));
        }
        mockMvc.perform(get("/api/v1/reservas/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].usuarioId")
                        .value(usuario.getId().toString()));
        mockMvc.perform(patch("/api/v1/reservas/{id}/cancelamento", reservaId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADA"));
        mockMvc.perform(get("/api/v1/laboratorios/{id}/calendario", labId)
                        .param("data", reservationDate.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.slots[5].ocupado").value(false));
        if (Boolean.getBoolean("mysql.integration")) {
            assertUtilization(labId, 0, 630);
            UUID rejected = createReservation(labId, "13:00", "13:30", usuarioToken);
            mockMvc.perform(patch("/api/v1/reservas/{id}/rejeicao", rejected)
                            .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJEITADA"));
            assertUtilization(labId, 0, 630);
        }
    }

    @Test
    void overlapIsConflictWhileAdjacentReservationIsAllowed() throws Exception {
        UUID labId = createLaboratory("Lab Conflito");
        createReservation(labId, "10:00", "11:00", usuarioToken);
        mockMvc.perform(post("/api/v1/reservas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationJson(labId, "10:00", "11:00")))
                .andExpect(status().isConflict());
        createReservation(labId, "11:00", "12:00", usuarioToken);
        assertEquals(2, reservaRepository.count());
    }

    @Test
    void complaintOwnershipAdministrativeStatusAndDashboardWorkTogether() throws Exception {
        UUID labId = createLaboratory("Lab Reclamacao");
        MvcResult created = mockMvc.perform(post("/api/v1/reclamacoes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Falha no projetor\","
                                + "\"categoriaProblema\":\"EQUIPAMENTO\","
                                + "\"laboratorioId\":\"" + labId + "\"}"))
                .andExpect(status().isCreated()).andReturn();
        UUID id = locationId(created);
        assertEquals(usuario.getId(), reclamacaoRepository.findById(id).orElseThrow().getUsuario().getId());

        mockMvc.perform(patch("/api/v1/reclamacoes/{id}/status", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EM_ANALISE\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/reclamacoes/{id}/status", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EM_ANALISE\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_ANALISE"));
        mockMvc.perform(get("/api/v1/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reclamacoes").value(1))
                .andExpect(jsonPath("$.problemasPendentes").value(1));
    }

    @Test
    void errorsRelationshipsAndAdministrativeBoundariesUseTheDocumentedContract() throws Exception {
        UUID missing = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/laboratorios/{id}", missing)
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/laboratorios")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(labJson("Lab Invalido").replace("\"capacidade\":40", "\"capacidade\":0")))
                .andExpect(status().isUnprocessableContent());

        mockMvc.perform(get("/api/v1/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/relatorios/historico")
                        .param("dataInicial", reservationDate.toString())
                        .param("dataFinal", reservationDate.minusDays(1).toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isUnprocessableContent());

        UUID labId = createLaboratory("Lab Relacionado");
        UUID reservaId = createReservation(labId, "12:00", "13:00", usuarioToken);
        mockMvc.perform(patch("/api/v1/reservas/{id}/rejeicao", reservaId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/reservas/{id}", missing)
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/v1/laboratorios/{id}", labId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/reclamacoes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"Falha\",\"categoriaProblema\":\"INVALIDA\","
                                + "\"laboratorioId\":\"" + labId + "\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/reclamacoes/{id}", missing)
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuarioToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void simultaneousConflictingOperationsPersistOnlyOneWinner() throws Exception {
        UUID labId = createLaboratory("Lab Concorrente");
        ReservaRequestDTO dto = new ReservaRequestDTO(reservationDate, LocalTime.of(14, 0),
                LocalTime.of(15, 0), labId, 20, "Concorrencia");
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<String> operation = () -> {
                start.await(10, TimeUnit.SECONDS);
                try {
                    createReservaHandler.create(dto, usuario.getEmail());
                    return "SUCCESS";
                } catch (ConflitoEstadoException e) {
                    return "CONFLICT";
                }
            };
            Future<String> first = executor.submit(operation);
            Future<String> second = executor.submit(operation);
            start.countDown();
            List<String> results = List.of(first.get(20, TimeUnit.SECONDS),
                    second.get(20, TimeUnit.SECONDS));
            assertEquals(1, results.stream().filter("SUCCESS"::equals).count());
            assertEquals(1, results.stream().filter("CONFLICT"::equals).count());
            assertEquals(1, reservaRepository.count());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void simultaneousApprovalsOfLegacyConflictsProduceOneApprovedWinner() throws Exception {
        UUID labId = createLaboratory("Lab Aprovacao Concorrente");
        Laboratorio laboratorio = laboratorioRepository.findById(labId).orElseThrow();
        Reserva first = pendingReservation(laboratorio, LocalTime.of(16, 0), LocalTime.of(17, 0));
        Reserva second = pendingReservation(laboratorio, LocalTime.of(16, 30), LocalTime.of(17, 30));
        reservaRepository.saveAllAndFlush(List.of(first, second));

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<String> approveFirst = () -> approveAfterStart(start, first.getId());
            Callable<String> approveSecond = () -> approveAfterStart(start, second.getId());
            Future<String> firstResult = executor.submit(approveFirst);
            Future<String> secondResult = executor.submit(approveSecond);
            start.countDown();
            List<String> results = List.of(firstResult.get(20, TimeUnit.SECONDS),
                    secondResult.get(20, TimeUnit.SECONDS));
            assertEquals(1, results.stream().filter("SUCCESS"::equals).count());
            assertEquals(1, results.stream().filter("CONFLICT"::equals).count());
            assertEquals(1, reservaRepository.findAll().stream()
                    .filter(item -> item.getStatus() == StatusReserva.APROVADA).count());
        } finally {
            executor.shutdownNow();
        }
    }

    private String approveAfterStart(CountDownLatch start, UUID reservaId) throws Exception {
        start.await(10, TimeUnit.SECONDS);
        try {
            reservaStatusHandler.approve(reservaId);
            return "SUCCESS";
        } catch (ConflitoEstadoException e) {
            return "CONFLICT";
        }
    }

    private Reserva pendingReservation(Laboratorio laboratorio, LocalTime inicio, LocalTime fim) {
        Reserva reserva = new Reserva();
        reserva.setDataReserva(reservationDate);
        reserva.setHorarioInicio(inicio);
        reserva.setHorarioFim(fim);
        reserva.setQuantidadeAlunos(20);
        reserva.setObservacao("Aprovacao concorrente");
        reserva.setStatus(StatusReserva.PENDENTE);
        reserva.setUsuario(usuario);
        reserva.setLaboratorio(laboratorio);
        return reserva;
    }

    private UUID createLaboratory(String nome) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/laboratorios")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(labJson(nome)))
                .andExpect(status().isCreated()).andReturn();
        return locationId(result);
    }

    private void assertUtilization(UUID labId, int occupiedMinutes, int availableMinutes)
            throws Exception {
        mockMvc.perform(get("/api/v1/relatorios/utilizacao")
                        .param("dataInicial", reservationDate.toString())
                        .param("dataFinal", reservationDate.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].laboratorioId").value(labId.toString()))
                .andExpect(jsonPath("$[0].minutosOcupados").value(occupiedMinutes))
                .andExpect(jsonPath("$[0].minutosDisponiveis").value(availableMinutes));
    }

    private UUID createReservation(UUID labId, String inicio, String fim, String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/reservas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationJson(labId, inicio, fim)))
                .andExpect(status().isCreated()).andReturn();
        return locationId(result);
    }

    private String login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"Senha#123\"}"))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private Usuario user(String email, String matricula, TipoDeUsuarios tipo, Set<RolesEntity> roles) {
        return Usuario.builder().nome("Usuario Integracao").email(email)
                .senha(passwordEncoder.encode("Senha#123")).tipoDeUsuarios(tipo)
                .curso("Computacao").matricula(matricula).roles(roles).build();
    }

    private String labJson(String nome) {
        return "{\"nome\":\"" + nome + "\",\"capacidade\":40,"
                + "\"quantidadeComputadores\":30,\"status\":\"DISPONIVEL\","
                + "\"tipoLaboratorio\":\"INFORMATICA\",\"localizacao\":\"Bloco A\","
                + "\"recursos\":[\"Projetor\",\"Ar condicionado\"]}";
    }

    private String reservationJson(UUID labId, String inicio, String fim) {
        return "{\"dataReserva\":\"" + reservationDate + "\","
                + "\"horarioInicio\":\"" + inicio + "\",\"horarioFim\":\"" + fim + "\","
                + "\"laboratorioId\":\"" + labId + "\",\"quantidadeAlunos\":20,"
                + "\"observacao\":\"Aula pratica\"}";
    }

    private UUID locationId(MvcResult result) {
        String location = Objects.requireNonNull(result.getResponse().getHeader(HttpHeaders.LOCATION));
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
