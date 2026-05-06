package com.example.desktop_cozinha.model;

import java.sql.Timestamp;

public class Historico {
    protected int id;
    protected String nome_produto;
    protected String nome_usuario;
    protected String tipo;
    protected int quantidade;
    protected String observacao;
    protected String data_hora;


    // Construtores, Getters e Setters, toString()

    // Enum compatível com o do banco de dados
    public enum TipoMovimentacao {
        ENTRADA, SAIDA, EDICAO, DESCARTE
    }
    public Historico(String nome_produto,String nome_usuario, String tipo, int quantidade, String observacao,String data_hora) {
        this.nome_produto = nome_produto;
        this.nome_usuario = nome_usuario;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.observacao = observacao;
        this.data_hora = data_hora;
    }

    public String getNome_produto() { return nome_produto; }
    public String getNome_usuario() { return nome_usuario; }
    public String getTipo() { return tipo; }
    public int getQuantidade() { return quantidade; }
    public String getObservacao() { return observacao; }
    public String getData_hora() { return data_hora; }
}

