package com.pfc.thindesk.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pfc.thindesk.dto.RequisicaoAberturaChamado;
import com.pfc.thindesk.dto.RespostaChamado;
import com.pfc.thindesk.entidade.SituacaoChamado;
import com.pfc.thindesk.servico.ServicoChamado;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChamadoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChamadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicoChamado servicoChamado;

    @Test
    void deveRetornar201AoAbrirChamadoValido() throws Exception {
        RespostaChamado resposta = new RespostaChamado(
                "chamado-1",
                "Impressora sem papel",
                "A impressora do setor financeiro não está imprimindo.",
                "usuario-1",
                "Maria Souza",
                LocalDateTime.of(2026, 10, 20, 10, 30),
                SituacaoChamado.ABERTO,
                null,
                null,
                null,
                null);
        when(servicoChamado.abrirChamado(any(RequisicaoAberturaChamado.class), isNull())).thenReturn(resposta);

        mockMvc.perform(post("/api/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Impressora sem papel",
                                  "descricao": "A impressora do setor financeiro não está imprimindo."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("chamado-1"))
                .andExpect(jsonPath("$.situacao").value("ABERTO"))
                .andExpect(jsonPath("$.idSolicitante").value("usuario-1"));
    }

    @Test
    void deveRetornar400QuandoCorpoForInvalido() throws Exception {
        mockMvc.perform(post("/api/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "",
                                  "descricao": "curta"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos."))
                .andExpect(jsonPath("$.campos.titulo").exists())
                .andExpect(jsonPath("$.campos.descricao").exists());

        verifyNoInteractions(servicoChamado);
    }
}
