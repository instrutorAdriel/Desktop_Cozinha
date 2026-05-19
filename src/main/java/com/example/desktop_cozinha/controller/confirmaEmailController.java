package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.Services.regexService;
import com.example.desktop_cozinha.Services.sessaoService;
import com.example.desktop_cozinha.Services.encryptService;
import com.example.desktop_cozinha.Services.senhaService;
import com.example.desktop_cozinha.model.Usuario;
import com.example.desktop_cozinha.model.UsuarioDAO;
import jakarta.mail.MessagingException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
        if(!regexService.emailValidation(emailDigitado)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Insira um email valido!");
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
            sessaoService.setEmailAtual(emailDigitado);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText("Código confirmado com sucesso!");
            senhaService.enviarEmail(emailDigitado);
            return;
        }
    }

    @FXML
    private void onVoltarClick() throws IOException {
        MainApplication.trocadorDeTelas("noti.fxml");
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
        if(sessaoService.getEmailAtual() == null || codigoDigitado.isBlank()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Código não encontrado!");
            alert.showAndWait();


        }

        boolean verifica = encryptService.checkPassword(codigoDigitado, UsuarioDAO.buscaEmail(sessaoService.getEmailAtual()).getToken());

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
    public void OnNotificacaoClick() throws Exception {
        MainApplication.trocadorDeTelas("noti.fxml");
    }

}
