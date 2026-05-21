package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.services.SessaoService;
import com.example.desktop_cozinha.services.EncryptService;
import com.example.desktop_cozinha.services.SenhaService;
import com.example.desktop_cozinha.model.Usuario;
import com.example.desktop_cozinha.model.UsuarioDAO;
import com.example.desktop_cozinha.services.EncryptService;
import com.example.desktop_cozinha.services.SenhaService;
import jakarta.mail.MessagingException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class ConfirmaEmailController {

    @FXML
    private TextField emailDigitar;
    @FXML
    private TextField codigoDigitar;

    @FXML
    private void onConfirmaEmailClick() throws SQLException, MessagingException {

        String emailDigitado = emailDigitar.getText();


        if(emailDigitado.isBlank()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Preencha o campo de email!");
            alert.showAndWait();
            return;
        }
        Usuario usuario = UsuarioDAO.buscaEmail(emailDigitado);

        if(usuario == null || !usuario.getEmail().equals(emailDigitado)){
            //usuario nao encontrado no sistema
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Email não encontrado!");
            alert.showAndWait();
            return ;
        }
        else {
            SessaoService.setEmailAtual(emailDigitado);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText("Código confirmado com sucesso!");
            SenhaService.enviarEmail(emailDigitado);
            return;
        }
    }

    @FXML
    private void onVoltarClick() throws IOException {
        MainApplication.trocadorDeTelas("Login.fxml");
    }

    @FXML
    private void onConfirmaToken() throws SQLException, IOException {

        String codigoDigitado = codigoDigitar.getText();

        if(codigoDigitado.isBlank()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Preencha o campo de código!");
            alert.showAndWait();
        }
        if(SessaoService.getEmailAtual() == null || codigoDigitado.isBlank()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Código não encontrado!");
            alert.showAndWait();


        }

        boolean verifica = EncryptService.checkPassword(codigoDigitado, UsuarioDAO.buscaEmail(SessaoService.getEmailAtual()).getToken());

        if(verifica){
            MainApplication.trocadorDeTelas("alteraSenha.fxml");
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Código inválido!");
            alert.showAndWait();
            
        }
        
        

    }
}
