package com.example.desktop_cozinha.model;

import java.sql.Timestamp;

public class Historico {
    private int id;
    private long produtoId;
    private long usuarioId;
    private TipoMovimentacao tipo;
    private int quantidade;
    private Timestamp dataHora;
    private String observacao;

    // Construtores, Getters e Setters, toString()

    // Enum compatível com o do banco de dados
    public enum TipoMovimentacao {
        ENTRADA, SAIDA, EDICAO, DESCARTE
    }
}
