package com.pfc.thindesk.entidade;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chamados")
public class Chamado {

    @Id
    private String id;
    private String titulo;
    private String descricao;
    private String idSolicitante;
    private String nomeSolicitante;
    private LocalDateTime dataAbertura;
    private SituacaoChamado situacao;
    private String solucao;
    private String idTecnicoResponsavel;
    private String nomeTecnicoResponsavel;
    private LocalDateTime dataResolucao;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getIdSolicitante() {
        return idSolicitante;
    }

    public void setIdSolicitante(String idSolicitante) {
        this.idSolicitante = idSolicitante;
    }

    public String getNomeSolicitante() {
        return nomeSolicitante;
    }

    public void setNomeSolicitante(String nomeSolicitante) {
        this.nomeSolicitante = nomeSolicitante;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public SituacaoChamado getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoChamado situacao) {
        this.situacao = situacao;
    }

    public String getSolucao() {
        return solucao;
    }

    public void setSolucao(String solucao) {
        this.solucao = solucao;
    }

    public String getIdTecnicoResponsavel() {
        return idTecnicoResponsavel;
    }

    public void setIdTecnicoResponsavel(String idTecnicoResponsavel) {
        this.idTecnicoResponsavel = idTecnicoResponsavel;
    }

    public String getNomeTecnicoResponsavel() {
        return nomeTecnicoResponsavel;
    }

    public void setNomeTecnicoResponsavel(String nomeTecnicoResponsavel) {
        this.nomeTecnicoResponsavel = nomeTecnicoResponsavel;
    }

    public LocalDateTime getDataResolucao() {
        return dataResolucao;
    }

    public void setDataResolucao(LocalDateTime dataResolucao) {
        this.dataResolucao = dataResolucao;
    }
}
