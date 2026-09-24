package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.LoginDAO;
import com.example.desktop_cozinha.services.SessaoService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;

public class LoginController {
    @FXML private TextField txtemail;
    @FXML private PasswordField pswsenha;
    @FXML private TextField txtsenhaRevelada;
    @FXML private ToggleButton btnMostrarSenha;
    @FXML private Button btnlogin;
    @FXML private Label lbesquecisenha;

    @FXML
    public void initialize() {
        // Vincula bidirecionalmente o texto digitado no PasswordField com o TextField visível
        txtsenhaRevelada.textProperty().bindBidirectional(pswsenha.textProperty());
    }

    @FXML
    private void toggleMostrarSenha() {
        boolean mostrar = btnMostrarSenha.isSelected();
        txtsenhaRevelada.setVisible(mostrar);
        txtsenhaRevelada.setManaged(mostrar);
        pswsenha.setVisible(!mostrar);
        pswsenha.setManaged(!mostrar);
    }

    @FXML
    public void onButtonLoginClick() throws IOException {
        LoginDAO loginDAO = new LoginDAO();
        String usuarioDigitado = txtemail.getText();
        String senhaDigitada = pswsenha.getText(); // Pega a senha normalmente via pswsenha

        if (usuarioDigitado.isBlank() || senhaDigitada.isBlank()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Aviso");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, preencha o e-mail e a senha.");
            alerta.showAndWait();
            return;
        }

        String senhaHashDoBanco = loginDAO.obterSenhaHash(usuarioDigitado);
        if (senhaHashDoBanco == null) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText(null);
            alerta.setContentText("Usuário não encontrado.");
            alerta.showAndWait();
            return;
        }

        if (BCrypt.checkpw(senhaDigitada, senhaHashDoBanco)) {
            SessaoService.setEmailAtual(usuarioDigitado);
            MainApplication.trocadorDeTelas("home.fxml");
        } else {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("As senhas não conferem.");
            alerta.setHeaderText(null);
            alerta.setContentText("A senha informada é inválida.");
            alerta.showAndWait();
        }
    }

    @FXML
    public void EsqueciSenha() throws IOException {
        MainApplication.trocadorDeTelas("esqueceu-senha.fxml");
    }

    @FXML
    public void Cadastrarme() throws IOException {
        MainApplication.trocadorDeTelas("cadastro.fxml");
    }
}