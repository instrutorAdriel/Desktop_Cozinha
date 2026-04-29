package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.CadastroProdutoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Objects;


public class CadastroProdutoController {

    @FXML
    private TextField txtNomeProduto;

    @FXML
    private MenuItem perecivel;
    @FXML
    private MenuItem nao_perecivel;

    @FXML
    private MenuItem utensilhos

    @FXML
    private TextField txtTipoProduto;

    @FXML
    private TextField txtQtdAtual ;
    @FXML
    private TextField txtUnidadeDeMedida;

    @FXML
    private TextField txtEstoqueMinimo;

    @FXML
    private TextField txtDataValidade;


    @FXML
    private Button btnCadastro;

    @FXML
    private Button btnVoltar;

    @FXML
    protected void voltarTela () throws Exception {
        // ao cliclar no botão volta para tela de login
        MainApplication.trocadorDeTelas("login.fxml");
    }

    @FXML
    protected void onCadastrar () throws Exception {
        //1. Le os valores digitados nos campos de tela
        // getText () retorna o conteudo atual do campo como String

        String nomeProduto = txtNomeProduto.getText();
        String tipoProduto = txtTipoProduto.getText();
        String qtdAtual = txtQtdAtual.getText();
        String unidadeDeMedida = txtUnidadeDeMedida.getText();
        String estoqueMinimo = txtEstoqueMinimo.getText();
        Date dataValidade = Date.valueOf(txtDataValidade.getText());

        //2. Validar se tem um campo vazio

        if (nomeProduto.isBlank () || tipoProduto.isBlank () || qtdAtual.isBlank () || unidadeDeMedida.isBlank () || estoqueMinimo.isBlank() ){
            //  exibe uma janela de aviso para o usuario
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Campos obrigatorios!");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, preencha os campos obrigatorios antes de cadastrar o produto!");
            alerta.showAndWait();

            // interromper o metodo

            return;
        }



        // 3. Cria uma instancia do CadastroDAO
        CadastroProdutoDAO dao = new CadastroProdutoDAO();

        //4. Chama o metodo de cadastro do cliente


        dao.cadastrarProduto(nomeProduto,tipoProduto,qtdAtual,unidadeDeMedida,estoqueMinimo, Date.valueOf(dataValidade.toLocalDate()));

        //5.informa ao usuario que o cadastro foi realizado

        Alert sucesso = new Alert(Alert.AlertType.CONFIRMATION);
        sucesso.setTitle("Cadastro realizado!");
        sucesso.setHeaderText(null);
        sucesso.setContentText("Cadastro realizado com sucesso!");
        sucesso.showAndWait();

        //6. limpa os campos de texto
        txtNomeProduto.clear();
        txtTipoProduto.clear();
        txtUnidadeDeMedida.clear();
        txtQtdAtual.clear();
        txtEstoqueMinimo.clear();
        txtDataValidade.clear();


        MainApplication.trocadorDeTelas("login.fxml");


    }

}
