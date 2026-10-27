package com.sigec.system.sigec.Constructors;

public class Utensilio {
    private int idUtensilio;
    private String nome;
    private String laboratorio;
    private String status;
    private int qtdTotal;
    private int qtdDisponivel;

    public Utensilio(int idUtensilio, String nome, String laboratorio, String status, int qtdTotal, int qtdDisponivel) {
        this.idUtensilio = idUtensilio;
        this.nome = nome;
        this.laboratorio = laboratorio;
        this.status = status;
        this.qtdTotal = qtdTotal;
        this.qtdDisponivel = qtdDisponivel;
    }

    public int getIdUtensilio() { return idUtensilio; }
    public String getNome() { return nome; }
    public String getLaboratorio() { return laboratorio; }
    public String getStatus() { return status; }
    public int getQtdTotal() { return qtdTotal; }
    public int getQtdDisponivel() { return qtdDisponivel; }
}