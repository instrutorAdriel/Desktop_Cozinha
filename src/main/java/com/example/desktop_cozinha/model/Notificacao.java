package com.example.desktop_cozinha.model;

public class Notificacao {

    int id;
    String nomeProduto;
    String tipo;
    int quantidade_atual;
    String unidade_medida;
    String data_validade;
    int estoque_minimo;



    public Notificacao(int id, String nomeProduto, String tipo, int quantidade_atual, String unidade_medida, String data_validade, int estoque_minimo) {

        this.id = id;
        this.nomeProduto = nomeProduto;
        this.tipo = tipo;
        this.quantidade_atual = quantidade_atual;
        this.unidade_medida = unidade_medida;
        this.data_validade = data_validade;
        this.estoque_minimo = estoque_minimo;
    }

    public int getId() {return id;}
    public String getNomeProduto() {
        return nomeProduto;
    }
    public String getTipo() { return tipo; }
    public int getQuantidade_atual() {
        return quantidade_atual;
    }
    public String getUnidade_medida() {
        return unidade_medida;
    }
    public String getData_validade() {
        return data_validade;
    }
    public int getEstoque_minimo() {return estoque_minimo;}
}
