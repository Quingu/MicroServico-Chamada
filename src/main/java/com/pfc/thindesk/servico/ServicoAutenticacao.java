package com.pfc.thindesk.servico;

import com.pfc.thindesk.excecao.RegraDeNegocioException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ServicoAutenticacao {

    public String obterIdUsuario(Authentication autenticacao) {
        return obterNomeUsuario(autenticacao);
    }

    public String obterNomeUsuario(Authentication autenticacao) {
        validarAutenticacao(autenticacao);
        return autenticacao.getName();
    }

    private void validarAutenticacao(Authentication autenticacao) {
        if (autenticacao == null
                || !autenticacao.isAuthenticated()
                || autenticacao instanceof AnonymousAuthenticationToken) {
            throw new RegraDeNegocioException("Usuário não autenticado.");
        }
    }
}
