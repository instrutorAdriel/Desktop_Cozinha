package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.CadastroDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Objects;

public class CadastroController {
    @FXML
    private TextField txtNome ;

    @FXML
    private   TextField txtEmail ;

    @FXML
    private   TextField txtCargo ;


    @FXML
    private PasswordField txtSenha;

    @FXML
    private PasswordField txtConfirmarSenha;


    @FXML
    private Button btnVoltar;

    @FXML
    private Button btnCadastrar;

    @FXML

        public void voltarTela() {
            // Pega a janela (Stage) onde este botão está e fecha ela
            Stage stage = (Stage) btnVoltar.getScene().getWindow();
            stage.close();
        }

    @FXML
    protected void cadastrar() throws Exception {

    }


    @FXML
    protected void onCadastrar () throws Exception {
        //1. Le os valores digitados nos campos de tela
        // getText () retorna o conteudo atual do campo como String

        String nome = txtNome.getText();
        String email = txtEmail.getText();
        String senha = txtSenha.getText();
        String cargo = txtCargo.getText();
        String confirmarSenha = txtConfirmarSenha.getText();

        //2. Validar se tem um campo vazio
        if (nome.isBlank () || senha.isBlank () || email.isBlank () || confirmarSenha.isBlank () ) {
            //  exibe uma janela de aviso para o usuario
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Campos obrigatorios!");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, preencha os campos obrigatorios antes de cadastrar !");
            alerta.showAndWait();

            // interromper o metodo

            return;
        }


        // Verificar se senha e confirmar senha são iguais
        if (!Objects.equals(senha, confirmarSenha)) {

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Senhas nã o conferem!");
            alerta.setHeaderText(null);
            alerta.setContentText("AS senhas não conferem! Digite a senha novamente !");
            alerta.showAndWait();

            return;

        }

        // 3. Cria uma instancia do CadastroDAO
        CadastroDAO dao = new CadastroDAO();

        //4. Chama o metodo de cadastro do cliente
        dao.cadastrarUsuario(nome, email, senha, cargo);

        //5.informa ao usuario que o cadastro foi realizado

        Alert sucesso = new Alert(Alert.AlertType.CONFIRMATION);
        sucesso.setTitle("Cadastro realizado!");
        sucesso.setHeaderText(null);
        sucesso.setContentText("Cadastro realizado com sucesso!");
        sucesso.showAndWait();

        //6. limpa os campos de texto
        txtNome.clear();
        txtEmail.clear();
        txtCargo.clear();
        txtSenha.clear();
        txtConfirmarSenha.clear();

        //MainApplication.trocadorDeTelas("hello-view.fxml");


    }


    public Button getBtnCadastrar() {
        return btnCadastrar;
    }

    public void setBtnCadastrar(Button btnCadastrar) {
        this.btnCadastrar = btnCadastrar;
    }

    public Button getBtnVoltar() {
        return btnVoltar;
    }

    public void setBtnVoltar(Button btnVoltar) {
        this.btnVoltar = btnVoltar;
    }
}
