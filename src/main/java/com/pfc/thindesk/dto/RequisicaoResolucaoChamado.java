package com.pfc.thindesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequisicaoResolucaoChamado(
        @NotBlank(message = "A solução é obrigatória.")
        @Size(max = 2000, message = "A solução deve ter no máximo 2000 caracteres.")
        String solucao
) {
}
