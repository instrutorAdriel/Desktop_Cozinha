package com.example.desktop_cozinha.model;


    public class ProdutoListaEstoque {
        private String nome;
        private String tipo;
        private String quantidade;
        private String unidade;

        public ProdutoListaEstoque(String nome, String tipo, String quantidade, String unidade) {
            this.nome = nome;
            this.tipo = tipo;
            this.quantidade = quantidade;
            this.unidade = unidade;
        }

        public String getNome() { return nome; }
        public String getTipo() { return tipo; }
        public String getQuantidade() { return quantidade; }
        public String getUnidade() { return unidade; }
    }

