package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.Services.SessaoService;
import com.example.desktop_cozinha.Services.emailService;
import com.example.desktop_cozinha.Services.senhaService;
import com.example.desktop_cozinha.model.Usuario;
import com.example.desktop_cozinha.model.UsuarioDAO;
import jakarta.mail.MessagingException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.SQLException;

public class confirmaEmailController {

    @FXML
    private Button confirmaEmail;
    @FXML
    private Button voltar;
    @FXML
    private Button confirmaToken;
    @FXML
    private Label confirmaEmailText;
    @FXML
    private Label confirmaTokenText;

    @FXML
    private void onConfirmaEmailClick() throws SQLException, IOException, MessagingException {

        String confirmarEmail = confirmaEmail.getText();
        confirmaEmailText.setText(confirmarEmail);

        if(confirmarEmail.isBlank()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Preencha o campo de email!");
            alert.showAndWait();

        }
        Usuario usuario = UsuarioDAO.buscaEmail(confirmarEmail);

        if(usuario == null || !usuario.getEmail().equals(confirmarEmail)){
            //usuario nao encontrado no sistema
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Email nao encontrado!");
            alert.showAndWait();

        }
        SessaoService.setEmailAtual(confirmarEmail);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText("Codigo confirmado com sucesso!");
        senhaService.enviarEmail(confirmarEmail);
        MainApplication.trocadorDeTelas("alteraSenha.fxml");

    }

    @FXML
    private void onVoltarClick() throws IOException {
        MainApplication.trocadorDeTelas("Login.fxml");
    }

    @FXML
    private void setConfirmaToken()





















}
