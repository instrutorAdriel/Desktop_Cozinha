package com.sigec.system.sigec.Constructors;

public class CursoDTO { // Pode manter o nome para não quebrar referências ou ajustar para TurmaDTO
    private int id;
    private String nome;

    public CursoDTO(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}