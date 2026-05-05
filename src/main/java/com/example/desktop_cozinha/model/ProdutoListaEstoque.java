package com.example.desktop_cozinha.model;

import java.time.LocalDate;


public class ProdutoListaEstoque {
    private int id;
    private String nome;
    private String tipo;
    private String quantidade;
    private String unidade;
    private String estoqueMinimo;

    // 1. Alterado para LocalDate para bater com o seu Getter e com o JavaFX
    private LocalDate dataValidade;

    // 2. Adicionei todos os campos como parâmetros do construtor
    public ProdutoListaEstoque(int id, String nome, String tipo, String quantidade, String unidade, String estoqueMinimo, LocalDate dataValidade) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.estoqueMinimo = estoqueMinimo;
        this.dataValidade = dataValidade;
    }

    public LocalDate getDataValidade() { return dataValidade; }
    public String getEstoqueMinimo() { return estoqueMinimo; }
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public String getQuantidade() { return quantidade; }
    public String getUnidade() { return unidade; }
}