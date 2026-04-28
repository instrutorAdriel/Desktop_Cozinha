package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.Services.SessaoService;
import com.example.desktop_cozinha.Services.encryptService;
import com.example.desktop_cozinha.Services.senhaService;
import com.example.desktop_cozinha.model.Usuario;
import com.example.desktop_cozinha.model.UsuarioDAO;
import jakarta.mail.MessagingException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class confirmaEmailController {

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
            alert.setHeaderText("Email nao encontrado!");
            alert.showAndWait();
            return ;
        }
        else {
            SessaoService.setEmailAtual(emailDigitado);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText("Codigo confirmado com sucesso!");
            senhaService.enviarEmail(emailDigitado);
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
            alert.setHeaderText("Preencha o campo de codigo!");
            alert.showAndWait();
        }
        if(SessaoService.getEmailAtual() == null || codigoDigitado.isBlank()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Codigo nao encontrado!");
            alert.showAndWait();


        }

        boolean verifica = encryptService.checkPassword(codigoDigitado, SessaoService.getEmailAtual());
        if(verifica){
            MainApplication.trocadorDeTelas("alteraSenha.fxml");
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Codigo invalido!");
            alert.showAndWait();
            
        }
        
        
        
        
        
        
    }





















}
