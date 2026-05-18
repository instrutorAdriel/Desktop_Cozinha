package com.example.desktop_cozinha.model;

// Classe que representa um produto exibido na lista de estoque.

    public class ProdutoListaEstoque {

        // Identificador do produto no banco de dados
        private String Id;

        // Nome do produto
        private String nome;

        // Tipo do produto (perecível, utensílio, etc.)
        private String tipo;

        // Quantidade disponível no estoque
        private String quantidade;

        // Unidade de medida (kg, litro, unidade...)
        private String unidade;

        public ProdutoListaEstoque(String nome, String tipo, String quantidade, String unidade, String id) {
           this.Id =id;
            this.nome = nome;
            this.tipo = tipo;
            this.quantidade = quantidade;
            this.unidade = unidade;
        }

        public String getId() { return Id; }
        public String getNome() { return nome; }
        public String getTipo() { return tipo; }
        public String getQuantidade() { return quantidade; }
        public String getUnidade() { return unidade; }
    }

