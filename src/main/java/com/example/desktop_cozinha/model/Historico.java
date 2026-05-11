package com.example.desktop_cozinha.model;

public class Historico {
    protected int id;
    protected String nome_produto;
    protected String nome_usuario;
    protected String tipo_movimentacao;
    protected int quantidade_movimentada;
    protected String observacao;
    protected String data_hora;



    public Historico(String nome_produto,String nome_usuario, String tipo_movimentacao, int quantidade_movimentada, String observacao,String data_hora) {
        this.nome_produto = nome_produto;
        this.nome_usuario = nome_usuario;
        this.tipo_movimentacao = tipo_movimentacao;
        this.quantidade_movimentada = quantidade_movimentada;
        this.observacao = observacao;
        this.data_hora = data_hora;
    }

    public String getNome_produto() { return nome_produto; }
    public String getNome_usuario() { return nome_usuario; }
    public String getTipo_movimentacao() { return tipo_movimentacao; }
    public int getQuantidade_movimentada() { return quantidade_movimentada; }
    public String getObservacao() { return observacao; }
    public String getData_hora() { return data_hora; }
}

