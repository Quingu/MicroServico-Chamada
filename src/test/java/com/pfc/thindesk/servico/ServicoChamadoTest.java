package com.pfc.thindesk.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pfc.thindesk.dto.RequisicaoAberturaChamado;
import com.pfc.thindesk.dto.RequisicaoResolucaoChamado;
import com.pfc.thindesk.dto.RespostaChamado;
import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.entity.SituacaoChamado;
import com.pfc.thindesk.excecao.RecursoNaoEncontradoException;
import com.pfc.thindesk.excecao.RegraDeNegocioException;
import com.pfc.thindesk.repositorio.RepositorioChamado;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class ServicoChamadoTest {

    @Mock
    private RepositorioChamado repositorioChamado;

    @Mock
    private ServicoAutenticacao servicoAutenticacao;

    @InjectMocks
    private ServicoChamado servicoChamado;

    @Test
    void deveAbrirChamadoComDadosDoUsuarioAutenticado() {
        Authentication autenticacao = mock(Authentication.class);
        when(servicoAutenticacao.obterIdUsuario(autenticacao)).thenReturn("usuario-1");
        when(servicoAutenticacao.obterNomeUsuario(autenticacao)).thenReturn("Maria Souza");
        when(repositorioChamado.save(any(Chamado.class))).thenAnswer(invocacao -> {
            Chamado chamado = invocacao.getArgument(0);
            chamado.setId("chamado-1");
            return chamado;
        });

        RequisicaoAberturaChamado requisicao = new RequisicaoAberturaChamado(
                "Impressora sem papel",
                "A impressora do setor financeiro não está imprimindo.");

        RespostaChamado resposta = servicoChamado.abrirChamado(requisicao, autenticacao);

        ArgumentCaptor<Chamado> captor = ArgumentCaptor.forClass(Chamado.class);
        verify(repositorioChamado).save(captor.capture());
        Chamado chamadoSalvo = captor.getValue();

        assertThat(chamadoSalvo.getTitulo()).isEqualTo("Impressora sem papel");
        assertThat(chamadoSalvo.getDescricao()).isEqualTo("A impressora do setor financeiro não está imprimindo.");
        assertThat(chamadoSalvo.getIdSolicitante()).isEqualTo("usuario-1");
        assertThat(chamadoSalvo.getNomeSolicitante()).isEqualTo("Maria Souza");
        assertThat(chamadoSalvo.getDataAbertura()).isNotNull();
        assertThat(chamadoSalvo.getSituacao()).isEqualTo(SituacaoChamado.ABERTO);
        assertThat(chamadoSalvo.getSolucao()).isNull();
        assertThat(chamadoSalvo.getDataResolucao()).isNull();
        assertThat(resposta.id()).isEqualTo("chamado-1");
        assertThat(resposta.situacao()).isEqualTo(SituacaoChamado.ABERTO);
    }

    @Test
    void deveListarChamadosDoProprioUsuario() {
        Authentication autenticacao = mock(Authentication.class);
        when(servicoAutenticacao.obterIdUsuario(autenticacao)).thenReturn("usuario-1");
        Chamado chamado = criarChamado("chamado-1", "usuario-1");
        when(repositorioChamado.findByIdSolicitanteOrderByDataAberturaDesc("usuario-1"))
                .thenReturn(List.of(chamado));

        List<RespostaChamado> chamados = servicoChamado.listarChamadosDoUsuario("usuario-1", autenticacao);

        assertThat(chamados).hasSize(1);
        assertThat(chamados.get(0).id()).isEqualTo("chamado-1");
        assertThat(chamados.get(0).idSolicitante()).isEqualTo("usuario-1");
    }

    @Test
    void deveRetornarListaVaziaQuandoUsuarioNaoPossuirChamados() {
        Authentication autenticacao = mock(Authentication.class);
        when(servicoAutenticacao.obterIdUsuario(autenticacao)).thenReturn("usuario-1");
        when(repositorioChamado.findByIdSolicitanteOrderByDataAberturaDesc("usuario-1"))
                .thenReturn(List.of());

        List<RespostaChamado> chamados = servicoChamado.listarChamadosDoUsuario("usuario-1", autenticacao);

        assertThat(chamados).isEmpty();
    }

    @Test
    void deveImpedirUsuarioComumDeConsultarChamadosDeOutroUsuario() {
        Authentication autenticacao = mock(Authentication.class);
        when(servicoAutenticacao.obterIdUsuario(autenticacao)).thenReturn("usuario-2");
        when(servicoAutenticacao.possuiPerfilTecnicoOuAdministrador(autenticacao)).thenReturn(false);

        assertThatThrownBy(() -> servicoChamado.listarChamadosDoUsuario("usuario-1", autenticacao))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("permissão");

        verify(repositorioChamado, never()).findByIdSolicitanteOrderByDataAberturaDesc(any());
    }

    @Test
    void devePermitirAdministradorConsultarChamadosDeOutroUsuario() {
        Authentication autenticacao = mock(Authentication.class);
        when(servicoAutenticacao.obterIdUsuario(autenticacao)).thenReturn("admin");
        when(servicoAutenticacao.possuiPerfilTecnicoOuAdministrador(autenticacao)).thenReturn(true);
        when(repositorioChamado.findByIdSolicitanteOrderByDataAberturaDesc("usuario-1"))
                .thenReturn(List.of(criarChamado("chamado-1", "usuario-1")));

        List<RespostaChamado> chamados = servicoChamado.listarChamadosDoUsuario("usuario-1", autenticacao);

        assertThat(chamados).hasSize(1);
    }

    @Test
    void deveConsultarChamadoDoProprioSolicitante() {
        Authentication autenticacao = mock(Authentication.class);
        when(servicoAutenticacao.obterIdUsuario(autenticacao)).thenReturn("usuario-1");
        when(repositorioChamado.findById("chamado-1"))
                .thenReturn(Optional.of(criarChamado("chamado-1", "usuario-1")));

        RespostaChamado resposta = servicoChamado.consultarChamadoPorId("chamado-1", autenticacao);

        assertThat(resposta.id()).isEqualTo("chamado-1");
        assertThat(resposta.situacao()).isEqualTo(SituacaoChamado.ABERTO);
    }

    @Test
    void deveLancarRecursoNaoEncontradoQuandoChamadoNaoExistir() {
        Authentication autenticacao = mock(Authentication.class);
        when(repositorioChamado.findById("chamado-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoChamado.consultarChamadoPorId("chamado-inexistente", autenticacao))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("chamado-inexistente");
    }

    @Test
    void deveImpedirConsultaDeChamadoPorUsuarioSemPermissao() {
        Authentication autenticacao = mock(Authentication.class);
        when(servicoAutenticacao.obterIdUsuario(autenticacao)).thenReturn("usuario-2");
        when(repositorioChamado.findById("chamado-1"))
                .thenReturn(Optional.of(criarChamado("chamado-1", "usuario-1")));
        when(servicoAutenticacao.possuiPerfilAdministrador(autenticacao)).thenReturn(false);

        assertThatThrownBy(() -> servicoChamado.consultarChamadoPorId("chamado-1", autenticacao))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("permissão");
    }

    @Test
    void devePermitirTecnicoResponsavelConsultarChamado() {
        Authentication autenticacao = mock(Authentication.class);
        when(servicoAutenticacao.obterIdUsuario(autenticacao)).thenReturn("tecnico-1");
        Chamado chamado = criarChamado("chamado-1", "usuario-1");
        chamado.setIdTecnicoResponsavel("tecnico-1");
        when(repositorioChamado.findById("chamado-1")).thenReturn(Optional.of(chamado));

        RespostaChamado resposta = servicoChamado.consultarChamadoPorId("chamado-1", autenticacao);

        assertThat(resposta.id()).isEqualTo("chamado-1");
    }

    @Test
    void deveResolverChamadoComTecnicoAutenticado() {
        Authentication autenticacao = mock(Authentication.class);
        when(servicoAutenticacao.possuiPerfilTecnico(autenticacao)).thenReturn(true);
        when(servicoAutenticacao.obterIdUsuario(autenticacao)).thenReturn("tecnico-1");
        when(servicoAutenticacao.obterNomeUsuario(autenticacao)).thenReturn("Carlos Técnico");
        Chamado chamado = criarChamado("chamado-1", "usuario-1");
        when(repositorioChamado.findById("chamado-1")).thenReturn(Optional.of(chamado));
        when(repositorioChamado.save(any(Chamado.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        RespostaChamado resposta = servicoChamado.resolverChamado(
                "chamado-1",
                new RequisicaoResolucaoChamado("Substituído o toner da impressora."),
                autenticacao);

        assertThat(chamado.getSolucao()).isEqualTo("Substituído o toner da impressora.");
        assertThat(chamado.getIdTecnicoResponsavel()).isEqualTo("tecnico-1");
        assertThat(chamado.getNomeTecnicoResponsavel()).isEqualTo("Carlos Técnico");
        assertThat(chamado.getDataResolucao()).isNotNull();
        assertThat(chamado.getSituacao()).isEqualTo(SituacaoChamado.RESOLVIDO);
        assertThat(resposta.situacao()).isEqualTo(SituacaoChamado.RESOLVIDO);
        assertThat(resposta.solucao()).isEqualTo("Substituído o toner da impressora.");
    }

    @Test
    void deveImpedirResolucaoPorUsuarioSemPerfilTecnico() {
        Authentication autenticacao = mock(Authentication.class);
        when(repositorioChamado.findById("chamado-1"))
                .thenReturn(Optional.of(criarChamado("chamado-1", "usuario-1")));
        when(servicoAutenticacao.possuiPerfilTecnico(autenticacao)).thenReturn(false);
        when(servicoAutenticacao.possuiPerfilAdministrador(autenticacao)).thenReturn(false);

        assertThatThrownBy(() -> servicoChamado.resolverChamado(
                "chamado-1",
                new RequisicaoResolucaoChamado("Tentativa indevida de resolução."),
                autenticacao))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("técnicos");

        verify(repositorioChamado, never()).save(any(Chamado.class));
    }

    @Test
    void deveLancarRecursoNaoEncontradoAoResolverChamadoInexistente() {
        Authentication autenticacao = mock(Authentication.class);
        when(repositorioChamado.findById("chamado-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoChamado.resolverChamado(
                "chamado-inexistente",
                new RequisicaoResolucaoChamado("Solução aplicada."),
                autenticacao))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("chamado-inexistente");
    }

    @Test
    void deveImpedirSegundaResolucaoDeChamado() {
        Authentication autenticacao = mock(Authentication.class);
        Chamado chamado = criarChamado("chamado-1", "usuario-1");
        chamado.setSituacao(SituacaoChamado.RESOLVIDO);
        when(repositorioChamado.findById("chamado-1")).thenReturn(Optional.of(chamado));
        when(servicoAutenticacao.possuiPerfilTecnico(autenticacao)).thenReturn(true);

        assertThatThrownBy(() -> servicoChamado.resolverChamado(
                "chamado-1",
                new RequisicaoResolucaoChamado("Nova solução."),
                autenticacao))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("já está resolvido");

        verify(repositorioChamado, never()).save(any(Chamado.class));
    }

    private Chamado criarChamado(String id, String idSolicitante) {
        Chamado chamado = new Chamado();
        chamado.setId(id);
        chamado.setTitulo("Título do chamado");
        chamado.setDescricao("Descrição detalhada do chamado.");
        chamado.setIdSolicitante(idSolicitante);
        chamado.setNomeSolicitante("Maria Souza");
        chamado.setDataAbertura(LocalDateTime.of(2026, 10, 20, 10, 30));
        chamado.setSituacao(SituacaoChamado.ABERTO);
        return chamado;
    }
}
