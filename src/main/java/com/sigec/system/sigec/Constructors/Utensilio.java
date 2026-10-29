package com.sigec.system.sigec.Constructors;

public class Utensilio {
    private int idUtensilio;
    private String nome;
    private String laboratorio;
    private String status;
    private int qtdTotal;
    private int qtdDisponivel;
    private int numeroPatrimonio;
    private int idCategoria;

    private int idLaboratorio;

    public Utensilio(int idUtensilio, String nome, String laboratorio, String status, int qtdTotal, int qtdDisponivel, int numeroPatrimonio, int idCategoria, int idLaboratorio) {
        this.idUtensilio = idUtensilio;
        this.nome = nome;
        this.laboratorio = laboratorio;
        this.status = status;
        this.qtdTotal = qtdTotal;
        this.qtdDisponivel = qtdDisponivel;
        this.numeroPatrimonio = numeroPatrimonio;
        this.idCategoria = idCategoria;
        this.idLaboratorio = idLaboratorio;
    }

    public int getIdUtensilio() { return idUtensilio; }
    public String getNome() { return nome; }
    public String getLaboratorio() { return laboratorio; }
    public String getStatus() { return status; }
    public int getQtdTotal() { return qtdTotal; }
    public int getQtdDisponivel() { return qtdDisponivel; }
    public int getNumeroPatrimonio() { return numeroPatrimonio; }
    public int getIdCategoria() { return idCategoria; }

    public int getIdLaboratorio() { return idLaboratorio; }
    public void setIdLaboratorio(int idLaboratorio) { this.idLaboratorio = idLaboratorio; }

    public void setNumeroPatrimonio(int numeroPatrimonio) { this.numeroPatrimonio = numeroPatrimonio; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }
}