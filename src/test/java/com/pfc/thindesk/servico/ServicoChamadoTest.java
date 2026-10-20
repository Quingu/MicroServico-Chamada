package com.pfc.thindesk.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pfc.thindesk.dto.RequisicaoAberturaChamado;
import com.pfc.thindesk.dto.RespostaChamado;
import com.pfc.thindesk.entidade.Chamado;
import com.pfc.thindesk.entidade.SituacaoChamado;
import com.pfc.thindesk.repositorio.RepositorioChamado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        org.mockito.Mockito.verify(repositorioChamado).save(captor.capture());
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
}
