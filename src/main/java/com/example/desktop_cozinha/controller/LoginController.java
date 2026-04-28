package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

import java.io.IOException;

public class LoginController {
    @FXML
    private Label welcomeText;
    @FXML
    private Hyperlink esqueciSenha;
    @FXML
    private Button login;





    @FXML
    protected void onEsqueciSenhaClick() throws IOException {

        esqueciSenha.getOnAction();
        MainApplication.trocadorDeTelas("confirmaEmail.fxml");
    }
}