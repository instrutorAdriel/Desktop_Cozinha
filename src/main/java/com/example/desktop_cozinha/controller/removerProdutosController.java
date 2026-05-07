package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.model.EdicaoProdutosDAO;
import com.example.desktop_cozinha.model.ProdutoListaEstoque;
import com.example.desktop_cozinha.model.removerDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import static com.example.desktop_cozinha.MainApplication.trocadorDeTelas;

public class removerProdutosController  {

    @FXML
    private Button btnRemoverProduto;

    @FXML
    private Button btnCancelarProduto;
    private Integer idProdutoEmRemover;

    public void preencherDadosParaEdicao(ProdutoListaEstoque p) {
        this.idProdutoEmRemover = p.getId(); // Salva o ID para usar no momento do UPDATE
    }


    @FXML
    protected void voltarTela () throws Exception {
        // ao cliclar no botão volta para tela de login
        trocadorDeTelas("lista-estoque.fxml");
    }


    protected void onRemoverProdutoClick() throws Exception {
        removerDAO dao = new removerDAO();

       dao.deletarProduto(idProdutoEmRemover);
    }




}
