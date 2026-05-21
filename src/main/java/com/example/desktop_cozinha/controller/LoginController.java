package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.LoginDAO;
import com.example.desktop_cozinha.services.SessaoService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;

public class LoginController {
    @FXML
    TextField txtemail;
    @FXML
    PasswordField pswsenha;
    @FXML
    Button btnlogin;
    @FXML
    Label lbesquecisenha;


    @FXML
    public void onButtonLoginClick() throws IOException {
        LoginDAO loginDAO = new LoginDAO();
        String usuarioDigitado = txtemail.getText();
        String senhaDigitada = pswsenha.getText();

        // 1. Validação prévia
        if (usuarioDigitado.isBlank() || senhaDigitada.isBlank()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Aviso");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, preencha o e-mail e a senha.");
            alerta.showAndWait();
            return; // Para a execução aqui
        }

        String senhaHashDoBanco = loginDAO.obterSenhaHash(usuarioDigitado);
        // 2. Feedback correto de usuário não encontrado
        if (senhaHashDoBanco == null) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText(null);
            alerta.setContentText("Usuário não encontrado.");
            alerta.showAndWait();
            return;
        }

        // 3. Validação da senha
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



