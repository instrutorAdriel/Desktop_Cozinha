package com.sigec.system.sigec.Constructors;

public class UnidadeDTO {
    private int id;
    private String nome;

    public UnidadeDTO(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
}