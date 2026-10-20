package com.pfc.thindesk.servico;

import com.pfc.thindesk.dto.RequisicaoAberturaChamado;
import com.pfc.thindesk.dto.RespostaChamado;
import com.pfc.thindesk.entidade.Chamado;
import com.pfc.thindesk.entidade.SituacaoChamado;
import com.pfc.thindesk.repositorio.RepositorioChamado;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ServicoChamado {

    private final RepositorioChamado repositorioChamado;
    private final ServicoAutenticacao servicoAutenticacao;

    public ServicoChamado(RepositorioChamado repositorioChamado, ServicoAutenticacao servicoAutenticacao) {
        this.repositorioChamado = repositorioChamado;
        this.servicoAutenticacao = servicoAutenticacao;
    }

    public RespostaChamado abrirChamado(RequisicaoAberturaChamado requisicao, Authentication autenticacao) {
        Chamado chamado = new Chamado();
        chamado.setTitulo(requisicao.titulo().trim());
        chamado.setDescricao(requisicao.descricao().trim());
        chamado.setIdSolicitante(servicoAutenticacao.obterIdUsuario(autenticacao));
        chamado.setNomeSolicitante(servicoAutenticacao.obterNomeUsuario(autenticacao));
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setSituacao(SituacaoChamado.ABERTO);

        return RespostaChamado.de(repositorioChamado.save(chamado));
    }

    public List<Chamado> listarChamados() {
        return repositorioChamado.findAll();
    }
}
