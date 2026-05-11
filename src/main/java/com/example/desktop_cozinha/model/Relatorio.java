package com.example.desktop_cozinha.model;

public class Relatorio {

    String nomeProduto;
    int quantidade_atual;
    String unidade_medida;
    String data_validade;
    int estoque_minimo;



    public Relatorio(String nomeProduto, int quantidade_atual, String unidade_medida, String data_validade, int estoque_minimo) {

        this.nomeProduto = nomeProduto;
        this.quantidade_atual = quantidade_atual;
        this.unidade_medida = unidade_medida;
        this.data_validade = data_validade;
        this.estoque_minimo = estoque_minimo;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }
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
