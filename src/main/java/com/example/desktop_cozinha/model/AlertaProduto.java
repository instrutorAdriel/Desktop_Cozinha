package com.example.desktop_cozinha.model;

import javafx.beans.property.SimpleStringProperty;

public class AlertaProduto {
    private final SimpleStringProperty nome;
    private final SimpleStringProperty dataValidade;
    private final SimpleStringProperty status;
    private final SimpleStringProperty quantidade;

    public AlertaProduto(String nome, String dataValidade, String status, String quantidade) {
        this.nome = new SimpleStringProperty(nome);
        this.dataValidade = new SimpleStringProperty(dataValidade);
        this.status = new SimpleStringProperty(status);
        this.quantidade = new SimpleStringProperty(quantidade);
    }

    public String getNome() {
        return nome.get();
    }

    public String getDataValidade() {
        return dataValidade.get();
    }

    public String getStatus() {
        return status.get();
    }

    public String getQuantidade() {
        return quantidade.get();
    }
}
