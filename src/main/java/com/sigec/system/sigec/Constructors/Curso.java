package com.sigec.system.sigec.Constructors;

import java.util.Objects;

/**
 * Modelo de domínio representando um Curso no sistema SIGEC.
 */
public class Curso {

    private int idCurso;
    private String nomeCurso;
    private String situacao;

    public Curso() {
        this.situacao = "A";
    }

    public Curso(int idCurso, String nomeCurso, String situacao) {
        this.idCurso = idCurso;
        this.nomeCurso = nomeCurso;
        this.situacao = situacao;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public String getNomeCurso() {
        return nomeCurso;
    }

    public void setNomeCurso(String nomeCurso) {
        this.nomeCurso = nomeCurso;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Curso curso = (Curso) o;
        return idCurso == curso.idCurso && Objects.equals(nomeCurso, curso.nomeCurso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCurso, nomeCurso);
    }

    @Override
    public String toString() {
        return nomeCurso != null ? nomeCurso : "Curso #" + idCurso;
    }
}

