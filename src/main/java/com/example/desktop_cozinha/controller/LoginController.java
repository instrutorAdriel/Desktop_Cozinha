package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.LoginDAO;
import com.mysql.cj.BindValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;

import static com.mysql.cj.Messages.getString;

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



        String senhaHashDoBanco = loginDAO.obterSenhaHash(usuarioDigitado);

        if (senhaHashDoBanco != null) {
                if (BCrypt.checkpw(senhaDigitada, senhaHashDoBanco)){
                    //MainApplication.trocadorDeTelas("home.fxml");
                    IO.println("login on");
                }
                else {
                    Alert alerta = new Alert(Alert.AlertType.WARNING);
                    alerta.setTitle("As senhas não conferem.");
                    alerta.setHeaderText(null);
                    alerta.setContentText("A senha informada é inválida.");
                    alerta.showAndWait();
                }
        } else {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Preencha os campos obrigatórios.");
            alerta.setHeaderText(null);
            alerta.setContentText("Existem campos não preenchidos.");
            alerta.showAndWait();
            return;
        }
        }
    }


