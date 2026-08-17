package com.seneca_brito.lab_manager.infrastructure.config;

import com.seneca_brito.lab_manager.shared.exceptions.errosDTOs.ErroResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class OpenApiConfig {

    static final String BEARER_AUTH = "bearerAuth";
    private static final String ERROR_SCHEMA = "#/components/schemas/ErroResponse";
    private static final Map<Integer, String> ERROR_COMPONENTS = Map.of(
            400, "BadRequest",
            401, "Unauthorized",
            403, "Forbidden",
            404, "NotFound",
            409, "Conflict",
            422, "UnprocessableEntity",
            500, "InternalServerError"
    );

    private static final Map<String, EndpointContract> CONTRACTS = contracts();

    @Bean
    public OpenAPI labManagerOpenApi() {
        Components components = new Components()
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .name(BEARER_AUTH)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT retornado por POST /api/v1/autenticacao/login"));

        ModelConverters.getInstance().read(ErroResponse.class)
                .forEach(components::addSchemas);
        addErrorResponses(components);

        return new OpenAPI()
                .info(new Info()
                        .title("LabManager API")
                        .version("v1")
                        .description("Contrato HTTP oficial do LabManager. Datas usam ISO 8601 e os horarios "
                                + "seguem America/Sao_Paulo. O calendario opera de 07:30 a 18:00 em intervalos "
                                + "de 30 minutos (21 slots); reservas PENDENTE e APROVADA bloqueiam horarios, "
                                + "e apenas APROVADA entra nos indicadores de utilizacao.")
                        .contact(new Contact().name("LabManager")))
                .components(components)
                .security(List.of(new SecurityRequirement().addList(BEARER_AUTH)))
                .tags(List.of(
                        new Tag().name("Autenticacao"),
                        new Tag().name("Usuarios"),
                        new Tag().name("Laboratorios"),
                        new Tag().name("Calendario"),
                        new Tag().name("Reservas"),
                        new Tag().name("Reclamacoes"),
                        new Tag().name("Dashboard e relatorios")
                ));
    }

    @Bean
    public OpenApiCustomizer officialContractCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) ->
                pathItem.readOperationsMap().forEach((method, operation) ->
                        customizeOperation(path, method, operation)));
    }

    private static void customizeOperation(String path, PathItem.HttpMethod method, Operation operation) {
        EndpointContract contract = CONTRACTS.get(method.name() + " " + path);
        if (contract == null) {
            return;
        }

        operation.setSummary(contract.summary());
        operation.setDescription("Acesso: " + contract.access() + ".");
        operation.setTags(List.of(contract.tag()));
        operation.setSecurity(contract.publicEndpoint()
                ? List.of()
                : List.of(new SecurityRequirement().addList(BEARER_AUTH)));

        ApiResponse generatedSuccess = firstSuccess(operation.getResponses());
        ApiResponse officialSuccess = successResponse(contract, generatedSuccess);
        ApiResponses responses = new ApiResponses();
        responses.addApiResponse(Integer.toString(contract.successStatus()), officialSuccess);
        contract.errors().forEach(status -> responses.addApiResponse(
                Integer.toString(status),
                new ApiResponse().$ref("#/components/responses/" + ERROR_COMPONENTS.get(status))));
        operation.setResponses(responses);
    }

    private static ApiResponse firstSuccess(ApiResponses responses) {
        if (responses == null) {
            return null;
        }
        return responses.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("2"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private static ApiResponse successResponse(EndpointContract contract, ApiResponse generated) {
        ApiResponse response = new ApiResponse().description(contract.successDescription());
        if (contract.successStatus() == 201) {
            response.addHeaderObject("Location", new Header()
                    .description("URI do recurso criado")
                    .schema(new StringSchema().format("uri")));
            return response;
        }
        if (contract.successStatus() == 204) {
            return response;
        }
        if (generated != null) {
            Content generatedContent = generated.getContent();
            if (generatedContent != null && !generatedContent.isEmpty()) {
                response.setContent(new Content().addMediaType(
                        org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        generatedContent.values().iterator().next()));
            }
        }
        return response;
    }

    private static void addErrorResponses(Components components) {
        Map<Integer, String> descriptions = Map.of(
                400, "Requisicao malformada ou parametro invalido",
                401, "Token JWT ausente, invalido ou expirado",
                403, "Usuario autenticado sem permissao para a operacao",
                404, "Recurso nao encontrado",
                409, "Conflito de estado, horario ou integridade",
                422, "Falha de validacao ou regra de negocio",
                500, "Falha interna sem exposicao de detalhes tecnicos"
        );
        descriptions.forEach((status, description) -> components.addResponses(
                ERROR_COMPONENTS.get(status),
                new ApiResponse()
                        .description(description)
                        .content(new Content().addMediaType(
                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                new MediaType().schema(new Schema<>().$ref(ERROR_SCHEMA))))));
    }

    private static Map<String, EndpointContract> contracts() {
        Map<String, EndpointContract> contracts = new LinkedHashMap<>();

        add(contracts, "POST", "/api/v1/autenticacao/login", "Autenticar usuario", "Autenticacao",
                "publico", true, 200, "JWT emitido com prazo de expiracao", 400, 422, 500);
        add(contracts, "POST", "/api/v1/autenticacao/cadastro", "Cadastrar usuario", "Autenticacao",
                "publico", true, 201, "Usuario criado; resposta sem corpo", 400, 409, 422, 500);

        add(contracts, "GET", "/api/v1/usuarios", "Listar usuarios", "Usuarios",
                "somente ADMINISTRACAO", false, 200, "Pagina de usuarios em formato PagedModel", 400, 401, 403, 500);
        add(contracts, "POST", "/api/v1/usuarios", "Criar usuario", "Usuarios",
                "somente ADMINISTRACAO", false, 201, "Usuario criado; resposta sem corpo", 400, 401, 403, 409, 422, 500);
        add(contracts, "GET", "/api/v1/usuarios/me", "Consultar proprio perfil", "Usuarios",
                "qualquer usuario autenticado", false, 200, "Perfil autenticado", 401, 404, 500);
        add(contracts, "PATCH", "/api/v1/usuarios/me", "Atualizar proprio perfil", "Usuarios",
                "qualquer usuario autenticado", false, 200, "Perfil atualizado", 400, 401, 404, 409, 422, 500);
        add(contracts, "GET", "/api/v1/usuarios/{id}", "Detalhar usuario", "Usuarios",
                "somente ADMINISTRACAO", false, 200, "Usuario encontrado", 400, 401, 403, 404, 500);
        add(contracts, "PATCH", "/api/v1/usuarios/{id}", "Atualizar usuario", "Usuarios",
                "somente ADMINISTRACAO", false, 200, "Usuario atualizado", 400, 401, 403, 404, 409, 422, 500);
        add(contracts, "DELETE", "/api/v1/usuarios/{id}", "Excluir usuario", "Usuarios",
                "somente ADMINISTRACAO", false, 204, "Usuario excluido; resposta sem corpo", 400, 401, 403, 404, 409, 500);

        add(contracts, "GET", "/api/v1/laboratorios", "Listar laboratorios", "Laboratorios",
                "qualquer usuario autenticado", false, 200, "Pagina de laboratorios em formato PagedModel", 400, 401, 500);
        add(contracts, "POST", "/api/v1/laboratorios", "Criar laboratorio", "Laboratorios",
                "somente ADMINISTRACAO", false, 201, "Laboratorio criado; resposta sem corpo", 400, 401, 403, 409, 422, 500);
        add(contracts, "GET", "/api/v1/laboratorios/{id}", "Detalhar laboratorio", "Laboratorios",
                "qualquer usuario autenticado", false, 200, "Laboratorio encontrado", 400, 401, 404, 500);
        add(contracts, "PATCH", "/api/v1/laboratorios/{id}", "Atualizar laboratorio", "Laboratorios",
                "somente ADMINISTRACAO", false, 200, "Laboratorio atualizado", 400, 401, 403, 404, 409, 422, 500);
        add(contracts, "DELETE", "/api/v1/laboratorios/{id}", "Excluir laboratorio", "Laboratorios",
                "somente ADMINISTRACAO", false, 204, "Laboratorio excluido; resposta sem corpo", 400, 401, 403, 404, 409, 500);
        add(contracts, "GET", "/api/v1/laboratorios/{id}/calendario", "Consultar calendario do laboratorio", "Calendario",
                "qualquer usuario autenticado", false, 200, "Calendario diario com exatamente 21 slots", 400, 401, 404, 500);

        add(contracts, "GET", "/api/v1/reservas", "Listar todas as reservas", "Reservas",
                "somente ADMINISTRACAO", false, 200, "Pagina de reservas em formato PagedModel", 400, 401, 403, 500);
        add(contracts, "GET", "/api/v1/reservas/me", "Listar proprias reservas", "Reservas",
                "qualquer usuario autenticado", false, 200, "Pagina das reservas do usuario autenticado", 400, 401, 500);
        add(contracts, "POST", "/api/v1/reservas", "Criar reserva", "Reservas",
                "qualquer usuario autenticado", false, 201, "Reserva criada como PENDENTE; resposta sem corpo", 400, 401, 404, 409, 422, 500);
        add(contracts, "GET", "/api/v1/reservas/{id}", "Detalhar reserva", "Reservas",
                "proprietario ou ADMINISTRACAO", false, 200, "Reserva encontrada", 400, 401, 403, 404, 500);
        add(contracts, "PATCH", "/api/v1/reservas/{id}", "Atualizar reserva", "Reservas",
                "proprietario ou ADMINISTRACAO", false, 200, "Reserva atualizada", 400, 401, 403, 404, 409, 422, 500);
        add(contracts, "PATCH", "/api/v1/reservas/{id}/cancelamento", "Cancelar reserva", "Reservas",
                "proprietario ou ADMINISTRACAO", false, 200, "Reserva cancelada", 400, 401, 403, 404, 409, 422, 500);
        add(contracts, "PATCH", "/api/v1/reservas/{id}/aprovacao", "Aprovar reserva", "Reservas",
                "somente ADMINISTRACAO", false, 200, "Reserva aprovada", 400, 401, 403, 404, 409, 422, 500);
        add(contracts, "PATCH", "/api/v1/reservas/{id}/rejeicao", "Rejeitar reserva", "Reservas",
                "somente ADMINISTRACAO", false, 200, "Reserva rejeitada", 400, 401, 403, 404, 409, 422, 500);

        add(contracts, "GET", "/api/v1/reclamacoes", "Listar todas as reclamacoes", "Reclamacoes",
                "somente ADMINISTRACAO", false, 200, "Pagina de reclamacoes em formato PagedModel", 400, 401, 403, 500);
        add(contracts, "GET", "/api/v1/reclamacoes/me", "Listar proprias reclamacoes", "Reclamacoes",
                "qualquer usuario autenticado", false, 200, "Pagina das reclamacoes do usuario autenticado", 400, 401, 500);
        add(contracts, "POST", "/api/v1/reclamacoes", "Criar reclamacao", "Reclamacoes",
                "qualquer usuario autenticado", false, 201, "Reclamacao criada como PENDENTE; resposta sem corpo", 400, 401, 404, 409, 422, 500);
        add(contracts, "GET", "/api/v1/reclamacoes/{id}", "Detalhar reclamacao", "Reclamacoes",
                "proprietario ou ADMINISTRACAO", false, 200, "Reclamacao encontrada", 400, 401, 403, 404, 500);
        add(contracts, "PATCH", "/api/v1/reclamacoes/{id}", "Atualizar reclamacao", "Reclamacoes",
                "proprietario ou ADMINISTRACAO", false, 200, "Reclamacao atualizada", 400, 401, 403, 404, 409, 422, 500);
        add(contracts, "PATCH", "/api/v1/reclamacoes/{id}/cancelamento", "Cancelar reclamacao", "Reclamacoes",
                "proprietario ou ADMINISTRACAO", false, 200, "Reclamacao cancelada", 400, 401, 403, 404, 409, 422, 500);
        add(contracts, "PATCH", "/api/v1/reclamacoes/{id}/status", "Alterar status da reclamacao", "Reclamacoes",
                "somente ADMINISTRACAO", false, 200, "Status da reclamacao atualizado", 400, 401, 403, 404, 409, 422, 500);

        add(contracts, "GET", "/api/v1/dashboard", "Consultar resumo administrativo", "Dashboard e relatorios",
                "somente ADMINISTRACAO", false, 200, "Resumo consolidado", 401, 403, 500);
        add(contracts, "GET", "/api/v1/relatorios/historico", "Consultar historico por periodo", "Dashboard e relatorios",
                "somente ADMINISTRACAO", false, 200, "Historico agregado por status", 400, 401, 403, 422, 500);
        add(contracts, "GET", "/api/v1/relatorios/utilizacao", "Consultar utilizacao por laboratorio", "Dashboard e relatorios",
                "somente ADMINISTRACAO", false, 200, "Utilizacao baseada apenas em reservas APROVADA", 400, 401, 403, 422, 500);
        add(contracts, "GET", "/api/v1/relatorios/ranking-laboratorios", "Consultar ranking de laboratorios", "Dashboard e relatorios",
                "somente ADMINISTRACAO", false, 200, "Ranking por utilizacao aprovada", 400, 401, 403, 422, 500);

        return Map.copyOf(contracts);
    }

    private static void add(Map<String, EndpointContract> contracts, String method, String path,
                            String summary, String tag, String access, boolean publicEndpoint,
                            int successStatus, String successDescription, Integer... errors) {
        contracts.put(method + " " + path, new EndpointContract(summary, tag, access,
                publicEndpoint, successStatus, successDescription, Set.of(errors)));
    }

    private record EndpointContract(
            String summary,
            String tag,
            String access,
            boolean publicEndpoint,
            int successStatus,
            String successDescription,
            Set<Integer> errors
    ) {
    }
}
