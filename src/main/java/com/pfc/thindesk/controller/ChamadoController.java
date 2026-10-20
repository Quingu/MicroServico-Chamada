package com.pfc.thindesk.controller;

import com.pfc.thindesk.dto.RequisicaoAberturaChamado;
import com.pfc.thindesk.dto.RespostaChamado;
import com.pfc.thindesk.servico.ServicoChamado;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/usuario/{id}")
    public List<RespostaChamado> listarChamadosDoUsuario(@PathVariable("id") String id,
                                                         Authentication autenticacao) {
        return servicoChamado.listarChamadosDoUsuario(id, autenticacao);
    }

    @GetMapping("/{id}")
    public RespostaChamado consultarChamado(@PathVariable("id") String id, Authentication autenticacao) {
        return servicoChamado.consultarChamadoPorId(id, autenticacao);
    }
}
