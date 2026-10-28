package com.sigec.system.sigec.Constructors;

import java.time.LocalDate;

public class AlertaEstoqueDTO {
    private String nomeProduto;
    private LocalDate dataValidade;
    private String nomeCategoria;
    private String lote;
    private int qtdAtual;
    private int qtdMinima;

    // Construtor vazio
    public AlertaEstoqueDTO() {}

    // Getters e Setters
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    public String getNomeCategoria() { return nomeCategoria; }
    public void setNomeCategoria(String nomeCategoria) { this.nomeCategoria = nomeCategoria; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public int getQtdAtual() { return qtdAtual; }
    public void setQtdAtual(int qtdAtual) { this.qtdAtual = qtdAtual; }

    public int getQtdMinima() { return qtdMinima; }
    public void setQtdMinima(int qtdMinima) { this.qtdMinima = qtdMinima; }
}