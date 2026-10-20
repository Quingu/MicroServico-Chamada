package com.pfc.thindesk.repositorio;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.pfc.thindesk.entity.Chamado;

public interface RepositorioChamado extends MongoRepository<Chamado, String> {

    List<Chamado> findByIdSolicitanteOrderByDataAberturaDesc(String idSolicitante);
}
