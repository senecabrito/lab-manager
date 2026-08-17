package com.seneca_brito.lab_manager.infrastructure.config;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generatesOfficialContractWithAllRoutesAndBearerJwt() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("LabManager API"))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.paths", aMapWithSize(31)))
                .andExpect(jsonPath("$.paths['/api/v1/autenticacao/login'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/usuarios/me'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/laboratorios/{id}/calendario'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/reservas/{id}/aprovacao'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/v1/reservas/recomendacoes'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/inventario'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/inventario/{id}'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/v1/reservas/{reservaId}/check-in'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/reservas/{reservaId}/check-out'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/acessos/me'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/reclamacoes/{id}/status'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/v1/relatorios/ranking-laboratorios'].get").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andReturn();

        List<?> operations = JsonPath.read(result.getResponse().getContentAsString(), "$.paths.*.*");
        assertEquals(45, operations.size());
    }

    @Test
    void distinguishesPublicProtectedAndAdministrativeOperations() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/autenticacao/login'].post.security", empty()))
                .andExpect(jsonPath("$.paths['/api/v1/autenticacao/cadastro'].post.security", empty()))
                .andExpect(jsonPath("$.paths['/api/v1/usuarios/me'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/usuarios'].get.description")
                        .value("Acesso: somente ADMINISTRACAO."))
                .andExpect(jsonPath("$.paths['/api/v1/usuarios'].get.responses.403.$ref")
                        .value("#/components/responses/Forbidden"));
    }

    @Test
    void documentsDtosErrorsPaginationAndBodylessResponses() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.ErroResponse.properties.status.type").value("integer"))
                .andExpect(jsonPath("$.components.schemas.ErroResponse.properties.message.type").value("string"))
                .andExpect(jsonPath("$.components.schemas.ErroResponse.required", hasItem("status")))
                .andExpect(jsonPath("$.components.schemas.ErroResponse.required", hasItem("message")))
                .andExpect(jsonPath("$.components.schemas.ErroResponse.required", hasItem("erro")))
                .andExpect(jsonPath("$.components.schemas", aMapWithSize(39)))
                .andExpect(jsonPath("$.components.schemas.RecomendacaoReservaRequestDTO").exists())
                .andExpect(jsonPath("$.components.schemas.RecomendacaoReservaResponseDTO").exists())
                .andExpect(jsonPath("$.components.schemas.InventarioRequestDTO").exists())
                .andExpect(jsonPath("$.components.schemas.InventarioResponseDTO").exists())
                .andExpect(jsonPath("$.components.schemas.AcessoResponseDTO").exists())
                .andExpect(jsonPath("$.components.schemas.RegistroAcesso").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.InventarioItem").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.ReservaResponseDTO.properties.status.enum", hasItem("APROVADA")))
                .andExpect(jsonPath("$.components.schemas.CalendarioResponseDTO.properties.data.format").value("date"))
                .andExpect(jsonPath("$.components.schemas.CalendarioSlotDTO.properties.inicio.format").value("time"))
                .andExpect(jsonPath("$.paths['/api/v1/usuarios'].get.parameters[*].name", hasItem("page")))
                .andExpect(jsonPath("$.paths['/api/v1/usuarios'].get.parameters[*].name", hasItem("size")))
                .andExpect(jsonPath("$.paths['/api/v1/usuarios'].get.responses.200.content.application/json.schema.$ref").exists())
                .andExpect(jsonPath("$.paths['/api/v1/usuarios/{id}'].delete.responses.204.description")
                        .value("Usuario excluido; resposta sem corpo"))
                .andExpect(jsonPath("$.paths['/api/v1/usuarios/{id}'].delete.responses.204.content").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/laboratorios'].post.responses.201.headers.Location").exists())
                .andExpect(jsonPath("$.paths['/api/v1/reservas/{reservaId}/check-in'].post.responses.201.headers.Location").exists())
                .andExpect(jsonPath("$.paths['/api/v1/reservas/{reservaId}/check-in'].post.responses.201.content.application/json.schema.$ref").exists())
                .andExpect(jsonPath("$.paths['/api/v1/reservas/recomendacoes'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/inventario'].post.responses.403.$ref")
                        .value("#/components/responses/Forbidden"))
                .andExpect(jsonPath("$.paths['/api/v1/reservas/{reservaId}/check-in'].post.responses.409.$ref")
                        .value("#/components/responses/Conflict"))
                .andExpect(jsonPath("$.paths['/api/v1/reservas/{reservaId}/check-in'].post.responses.422.$ref")
                        .value("#/components/responses/UnprocessableEntity"))
                .andExpect(jsonPath("$.components.responses.InternalServerError.content.application/json.schema.$ref")
                        .value("#/components/schemas/ErroResponse"));
    }

    @Test
    void exposesDocumentationButKeepsApplicationProtected() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/laboratorios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message").value("Autenticacao obrigatoria"));
    }
}
