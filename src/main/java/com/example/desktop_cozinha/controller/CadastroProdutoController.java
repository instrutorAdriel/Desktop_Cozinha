package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.CadastroProdutoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;
import java.time.LocalDate;
import java.sql.Date;



public class CadastroProdutoController  implements Initializable {

    @FXML
    private TextField txtNomeProduto;

    @FXML
    private TextField txtQtdAtual ;

    @FXML
    private TextField txtEstoqueMinimo;
    @FXML
    private ChoiceBox<String> txtUnidadeDeMedida;

    @FXML
    private ChoiceBox <String>txtTipoProduto;


    @FXML
    private DatePicker txtDataValidade;


    @FXML
    private Button btnCadastro;

    @FXML
    private Button btnVoltar;

    public void initialize(URL url, ResourceBundle rb) {
        txtUnidadeDeMedida.getItems().addAll("KG", "LITRO", "UNIDADE");
        txtTipoProduto.getItems().addAll("PERECIVEL", "NAO_PERECIVEL", "UTENSILIO");

    }


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
        String qtdAtual = txtQtdAtual.getText();
        String tipoProduto = txtTipoProduto.getValue();
        String unidadeDeMedida = txtUnidadeDeMedida.getValue();
        String estoqueMinimo = txtEstoqueMinimo.getText();
        // Pega o valor do calendário (pode ser a data ou pode ser null se estiver vazio)
        LocalDate dataSelecionada = txtDataValidade.getValue();

        // Cria a variável do banco vazia por padrão
        Date dataValidade = null;

        // Só converte se o usuário realmente tiver escolhido uma data
        if (dataSelecionada != null) {
            dataValidade = Date.valueOf(dataSelecionada);
        }


        //2. Validar se tem um campo vazio

        if ( nomeProduto.isBlank () || tipoProduto.isBlank () || qtdAtual.isBlank () || unidadeDeMedida.isBlank () || estoqueMinimo.isBlank() ){
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


        dao.cadastrarProduto(nomeProduto, tipoProduto, qtdAtual, unidadeDeMedida, estoqueMinimo, dataValidade);

        //5.informa ao usuario que o cadastro foi realizado

        Alert sucesso = new Alert(Alert.AlertType.CONFIRMATION);
        sucesso.setTitle("Cadastro realizado!");
        sucesso.setHeaderText(null);
        sucesso.setContentText("Cadastro realizado com sucesso!");
        sucesso.showAndWait();

        //6. limpa os campos de texto
        txtNomeProduto.clear();
        txtQtdAtual.clear();
        txtEstoqueMinimo.clear();





    }

}
