package com.pfc.thindesk.dto;

import com.pfc.thindesk.entidade.Chamado;
import com.pfc.thindesk.entidade.SituacaoChamado;
import java.time.LocalDateTime;

public record RespostaChamado(
        String id,
        String titulo,
        String descricao,
        String idSolicitante,
        String nomeSolicitante,
        LocalDateTime dataAbertura,
        SituacaoChamado situacao,
        String solucao,
        String idTecnicoResponsavel,
        String nomeTecnicoResponsavel,
        LocalDateTime dataResolucao
) {

    public static RespostaChamado de(Chamado chamado) {
        return new RespostaChamado(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getIdSolicitante(),
                chamado.getNomeSolicitante(),
                chamado.getDataAbertura(),
                chamado.getSituacao(),
                chamado.getSolucao(),
                chamado.getIdTecnicoResponsavel(),
                chamado.getNomeTecnicoResponsavel(),
                chamado.getDataResolucao()
        );
    }
}
