package com.pfc.thindesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequisicaoAberturaChamado(
        @NotBlank(message = "O título é obrigatório.")
        @Size(min = 3, max = 120, message = "O título deve ter entre 3 e 120 caracteres.")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória.")
        @Size(min = 10, max = 2000, message = "A descrição deve ter entre 10 e 2000 caracteres.")
        String descricao
) {
}
