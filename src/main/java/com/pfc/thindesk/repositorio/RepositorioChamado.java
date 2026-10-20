package com.pfc.thindesk.repositorio;

import com.pfc.thindesk.entidade.Chamado;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RepositorioChamado extends MongoRepository<Chamado, String> {

    List<Chamado> findByIdSolicitanteOrderByDataAberturaDesc(String idSolicitante);
}
