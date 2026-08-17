package com.seneca_brito.lab_manager.shared.exceptions.errosDTOs;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErroCampo(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "nome") String campo,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Campo obrigatorio") String erro
) {
}
