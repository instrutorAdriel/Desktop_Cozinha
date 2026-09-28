package com.sigec.system.sigec.Constructors;

import java.util.Objects;

/**
 * Modelo de domínio representando um Laboratório/Cozinha no sistema SIGEC.
 */
public class Laboratorio {

    private int idLaboratorio;
    private String nomeLaboratorio;
    private int capacidade;
    private String situacao;
    private int idUnidade;

    public Laboratorio() {
        this.situacao = "A";
    }

    public Laboratorio(int idLaboratorio, String nomeLaboratorio, int capacidade, String situacao, int idUnidade) {
        this.idLaboratorio = idLaboratorio;
        this.nomeLaboratorio = nomeLaboratorio;
        this.capacidade = capacidade;
        this.situacao = situacao;
        this.idUnidade = idUnidade;
    }

    public int getIdLaboratorio() {
        return idLaboratorio;
    }

    public void setIdLaboratorio(int idLaboratorio) {
        this.idLaboratorio = idLaboratorio;
    }

    public String getNomeLaboratorio() {
        return nomeLaboratorio;
    }

    public void setNomeLaboratorio(String nomeLaboratorio) {
        this.nomeLaboratorio = nomeLaboratorio;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public int getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(int idUnidade) {
        this.idUnidade = idUnidade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Laboratorio that = (Laboratorio) o;
        return idLaboratorio == that.idLaboratorio && Objects.equals(nomeLaboratorio, that.nomeLaboratorio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLaboratorio, nomeLaboratorio);
    }

    @Override
    public String toString() {
        return nomeLaboratorio != null ? nomeLaboratorio : "Laboratório #" + idLaboratorio;
    }
}
