package com.pfc.thindesk.repositorio;

import com.pfc.thindesk.entidade.Chamado;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RepositorioChamado extends MongoRepository<Chamado, String> {
}
