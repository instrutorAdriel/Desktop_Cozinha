package com.example.desktop_cozinha.model;

public class Historico {
    protected int id;
    protected String nome_produto;
    protected String nome_usuario;
    protected String tipo_movimentacao;
    protected int quantidade;
    protected String tipo_estoque;
    protected String data_hora;



    public Historico(String nome_produto,String nome_usuario, String tipo_movimentacao, int quantidade, String tipo_estoque,String data_hora) {
        this.nome_produto = nome_produto;
        this.nome_usuario = nome_usuario;
        this.tipo_movimentacao = tipo_movimentacao;
        this.quantidade = quantidade;
        this.tipo_estoque = tipo_estoque;
        this.data_hora = data_hora;
    }

    public String getNome_produto() { return nome_produto; }
    public String getNome_usuario() { return nome_usuario; }
    public String getTipo_movimentacao() { return tipo_movimentacao; }
    public int getQuantidade() { return quantidade; }
    public String getTipo_estoque() { return tipo_estoque; }
    public String getData_hora() { return data_hora; }
}

