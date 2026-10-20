package com.pfc.thindesk.servico;

import com.pfc.thindesk.dto.RequisicaoAberturaChamado;
import com.pfc.thindesk.dto.RequisicaoResolucaoChamado;
import com.pfc.thindesk.dto.RespostaChamado;
import com.pfc.thindesk.entidade.Chamado;
import com.pfc.thindesk.entidade.SituacaoChamado;
import com.pfc.thindesk.excecao.RecursoNaoEncontradoException;
import com.pfc.thindesk.excecao.RegraDeNegocioException;
import com.pfc.thindesk.repositorio.RepositorioChamado;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
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

    public List<RespostaChamado> listarChamadosDoUsuario(String idUsuario, Authentication autenticacao) {
        String idAutenticado = servicoAutenticacao.obterIdUsuario(autenticacao);
        boolean perfilPrivilegiado = servicoAutenticacao.possuiPerfilTecnicoOuAdministrador(autenticacao);

        if (!idAutenticado.equals(idUsuario) && !perfilPrivilegiado) {
            throw new AccessDeniedException(
                    "Usuário não possui permissão para consultar os chamados de outro usuário.");
        }

        return repositorioChamado.findByIdSolicitanteOrderByDataAberturaDesc(idUsuario)
                .stream()
                .map(RespostaChamado::de)
                .toList();
    }

    public RespostaChamado consultarChamadoPorId(String id, Authentication autenticacao) {
        Chamado chamado = repositorioChamado.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado com id: " + id));

        String idAutenticado = servicoAutenticacao.obterIdUsuario(autenticacao);
        boolean ehSolicitante = idAutenticado.equals(chamado.getIdSolicitante());
        boolean ehTecnicoResponsavel = idAutenticado.equals(chamado.getIdTecnicoResponsavel());

        if (!ehSolicitante && !ehTecnicoResponsavel
                && !servicoAutenticacao.possuiPerfilAdministrador(autenticacao)) {
            throw new AccessDeniedException("Usuário não possui permissão para consultar este chamado.");
        }

        return RespostaChamado.de(chamado);
    }

    public RespostaChamado resolverChamado(String id, RequisicaoResolucaoChamado requisicao,
                                           Authentication autenticacao) {
        Chamado chamado = repositorioChamado.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado com id: " + id));

        boolean perfilTecnico = servicoAutenticacao.possuiPerfilTecnico(autenticacao);
        boolean perfilAdministrador = servicoAutenticacao.possuiPerfilAdministrador(autenticacao);
        if (!perfilTecnico && !perfilAdministrador) {
            throw new AccessDeniedException("Apenas técnicos podem resolver chamados.");
        }

        if (chamado.getSituacao() == SituacaoChamado.RESOLVIDO) {
            throw new RegraDeNegocioException("Chamado já está resolvido e não pode ser alterado.");
        }

        chamado.setSolucao(requisicao.solucao().trim());
        chamado.setIdTecnicoResponsavel(servicoAutenticacao.obterIdUsuario(autenticacao));
        chamado.setNomeTecnicoResponsavel(servicoAutenticacao.obterNomeUsuario(autenticacao));
        chamado.setDataResolucao(LocalDateTime.now());
        chamado.setSituacao(SituacaoChamado.RESOLVIDO);

        return RespostaChamado.de(repositorioChamado.save(chamado));
    }
}
