package com.pfc.thindesk.controller;

import com.pfc.thindesk.dto.RequisicaoAberturaChamado;
import com.pfc.thindesk.dto.RespostaChamado;
import com.pfc.thindesk.servico.ServicoChamado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    private final ServicoChamado servicoChamado;

    public ChamadoController(ServicoChamado servicoChamado) {
        this.servicoChamado = servicoChamado;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaChamado abrirChamado(@Valid @RequestBody RequisicaoAberturaChamado requisicao,
                                        Authentication autenticacao) {
        return servicoChamado.abrirChamado(requisicao, autenticacao);
    }
}
