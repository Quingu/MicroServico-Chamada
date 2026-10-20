package com.pfc.thindesk.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pfc.thindesk.dto.RequisicaoAberturaChamado;
import com.pfc.thindesk.dto.RespostaChamado;
import com.pfc.thindesk.entidade.SituacaoChamado;
import com.pfc.thindesk.excecao.RecursoNaoEncontradoException;
import com.pfc.thindesk.servico.ServicoChamado;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
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
        when(servicoChamado.abrirChamado(any(RequisicaoAberturaChamado.class), isNull()))
                .thenReturn(respostaChamado("chamado-1", "usuario-1"));

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

    @Test
    void deveListarChamadosDoUsuario() throws Exception {
        when(servicoChamado.listarChamadosDoUsuario(eq("usuario-1"), isNull()))
                .thenReturn(List.of(respostaChamado("chamado-1", "usuario-1")));

        mockMvc.perform(get("/api/chamados/usuario/usuario-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("chamado-1"))
                .andExpect(jsonPath("$[0].idSolicitante").value("usuario-1"));
    }

    @Test
    void deveRetornarListaVaziaQuandoUsuarioNaoPossuirChamados() throws Exception {
        when(servicoChamado.listarChamadosDoUsuario(eq("usuario-1"), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/chamados/usuario/usuario-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void deveRetornar403AoConsultarChamadosDeOutroUsuario() throws Exception {
        when(servicoChamado.listarChamadosDoUsuario(eq("usuario-1"), isNull()))
                .thenThrow(new AccessDeniedException("Usuário não possui permissão para consultar os chamados de outro usuário."));

        mockMvc.perform(get("/api/chamados/usuario/usuario-1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value(
                        "Usuário não possui permissão para consultar os chamados de outro usuário."));
    }

    @Test
    void deveConsultarChamadoPorId() throws Exception {
        when(servicoChamado.consultarChamadoPorId(eq("chamado-1"), isNull()))
                .thenReturn(respostaChamado("chamado-1", "usuario-1"));

        mockMvc.perform(get("/api/chamados/chamado-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("chamado-1"))
                .andExpect(jsonPath("$.titulo").value("Impressora sem papel"))
                .andExpect(jsonPath("$.situacao").value("ABERTO"));
    }

    @Test
    void deveRetornar404QuandoChamadoNaoExistir() throws Exception {
        when(servicoChamado.consultarChamadoPorId(eq("chamado-inexistente"), isNull()))
                .thenThrow(new RecursoNaoEncontradoException("Chamado não encontrado com id: chamado-inexistente"));

        mockMvc.perform(get("/api/chamados/chamado-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Chamado não encontrado com id: chamado-inexistente"));
    }

    @Test
    void deveRetornar403AoConsultarChamadoSemPermissao() throws Exception {
        when(servicoChamado.consultarChamadoPorId(eq("chamado-1"), isNull()))
                .thenThrow(new AccessDeniedException("Usuário não possui permissão para consultar este chamado."));

        mockMvc.perform(get("/api/chamados/chamado-1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Usuário não possui permissão para consultar este chamado."));
    }

    private RespostaChamado respostaChamado(String id, String idSolicitante) {
        return new RespostaChamado(
                id,
                "Impressora sem papel",
                "A impressora do setor financeiro não está imprimindo.",
                idSolicitante,
                "Maria Souza",
                LocalDateTime.of(2026, 10, 20, 10, 30),
                SituacaoChamado.ABERTO,
                null,
                null,
                null,
                null);
    }
}
