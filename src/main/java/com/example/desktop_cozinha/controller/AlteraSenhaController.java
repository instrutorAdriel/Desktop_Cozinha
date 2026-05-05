package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.services.SenhaService;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

import java.io.IOException;
import java.sql.SQLException;

public class AlteraSenhaController {

    @FXML
    private Button confirmaTroca;
    @FXML
    PasswordField novaSenha;
    @FXML
    PasswordField confirmaSenha;

    @FXML
    protected void onConfirmaTrocaClick() throws SQLException, IOException {

        String senhaDigitada = novaSenha.getText();
        String senhaConfirmada = confirmaSenha.getText();

        if (!senhaDigitada.equals(senhaConfirmada)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("As senhas devem ser iguais!");
            alert.showAndWait();

        }
        if (senhaDigitada.isBlank() || senhaConfirmada.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Preencha todos os campos!");

        }
        if (senhaDigitada.isBlank()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Preencha o campo de senha!");
            alert.showAndWait();

        }
        if(senhaConfirmada.isBlank()){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Confirme sua senha!");

        }
        else {
            SenhaService.resetaSenha(senhaDigitada, senhaConfirmada);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText("Senha alterada com sucesso!");
            alert.showAndWait();
            TranslateTransition tt = new TranslateTransition(javafx.util.Duration.seconds(1), confirmaTroca);
            tt.setToX(1000);
            tt.play();
            MainApplication.trocadorDeTelas("Login.fxml");
        }
        novaSenha.clear();
        confirmaSenha.clear();
    }
}
