package com.pfc.thindesk.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pfc.thindesk.dto.RequisicaoAberturaChamado;
import com.pfc.thindesk.dto.RequisicaoResolucaoChamado;
import com.pfc.thindesk.dto.RespostaChamado;
import com.pfc.thindesk.entidade.SituacaoChamado;
import com.pfc.thindesk.excecao.RecursoNaoEncontradoException;
import com.pfc.thindesk.excecao.RegraDeNegocioException;
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

    @Test
    void deveResolverChamadoComTecnico() throws Exception {
        when(servicoChamado.resolverChamado(eq("chamado-1"), any(RequisicaoResolucaoChamado.class), isNull()))
                .thenReturn(respostaResolvida());

        mockMvc.perform(put("/api/chamados/chamado-1/resolver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "solucao": "Substituído o toner da impressora."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("RESOLVIDO"))
                .andExpect(jsonPath("$.solucao").value("Substituído o toner da impressora."))
                .andExpect(jsonPath("$.nomeTecnicoResponsavel").value("Carlos Técnico"));
    }

    @Test
    void deveRetornar400QuandoSolucaoNaoForInformada() throws Exception {
        mockMvc.perform(put("/api/chamados/chamado-1/resolver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "solucao": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.solucao").exists());

        verifyNoInteractions(servicoChamado);
    }

    @Test
    void deveRetornar403AoResolverChamadoSemPerfilTecnico() throws Exception {
        when(servicoChamado.resolverChamado(eq("chamado-1"), any(RequisicaoResolucaoChamado.class), isNull()))
                .thenThrow(new AccessDeniedException("Apenas técnicos podem resolver chamados."));

        mockMvc.perform(put("/api/chamados/chamado-1/resolver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "solucao": "Tentativa indevida de resolução."
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Apenas técnicos podem resolver chamados."));
    }

    @Test
    void deveRetornar404AoResolverChamadoInexistente() throws Exception {
        when(servicoChamado.resolverChamado(eq("chamado-inexistente"), any(RequisicaoResolucaoChamado.class), isNull()))
                .thenThrow(new RecursoNaoEncontradoException("Chamado não encontrado com id: chamado-inexistente"));

        mockMvc.perform(put("/api/chamados/chamado-inexistente/resolver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "solucao": "Solução aplicada."
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Chamado não encontrado com id: chamado-inexistente"));
    }

    @Test
    void deveRetornar409AoResolverChamadoJaResolvido() throws Exception {
        when(servicoChamado.resolverChamado(eq("chamado-1"), any(RequisicaoResolucaoChamado.class), isNull()))
                .thenThrow(new RegraDeNegocioException("Chamado já está resolvido e não pode ser alterado."));

        mockMvc.perform(put("/api/chamados/chamado-1/resolver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "solucao": "Nova solução."
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Chamado já está resolvido e não pode ser alterado."));
    }

    private RespostaChamado respostaResolvida() {
        return new RespostaChamado(
                "chamado-1",
                "Impressora sem papel",
                "A impressora do setor financeiro não está imprimindo.",
                "usuario-1",
                "Maria Souza",
                LocalDateTime.of(2026, 10, 20, 10, 30),
                SituacaoChamado.RESOLVIDO,
                "Substituído o toner da impressora.",
                "tecnico-1",
                "Carlos Técnico",
                LocalDateTime.of(2026, 10, 20, 11, 45));
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
