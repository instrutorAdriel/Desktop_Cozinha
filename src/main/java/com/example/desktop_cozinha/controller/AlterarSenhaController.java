package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.model.AlterarSenhaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AlterarSenhaController {
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtSenha;
    @FXML
    private PasswordField  txtConfirmarSenha;
    @FXML
    private Button btnAlterar;

     public void alterarSenha() {
        String email = txtEmail.getText();
        String senha = txtSenha.getText();
        String confirmarSenha = txtConfirmarSenha.getText();
        if (!(senha.equals(confirmarSenha))) {
            Alert alerta = new  Alert(Alert.AlertType.ERROR);
            alerta.setTitle("senha diferente");
            alerta.setHeaderText(null);
            alerta.setContentText("confirmar senha diferente");
            alerta.showAndWait();

            return;
        }
        if (senha.isEmpty() || confirmarSenha.isEmpty() || email.isEmpty() ) {
            Alert alerta = new  Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Campos obrigatorios");
            alerta.setHeaderText(null);
            alerta.setContentText("Preencha os campos obrigatorios");
            alerta.showAndWait();

            return;
        }


        AlterarSenhaDAO DAO = new AlterarSenhaDAO();
        DAO.alterar(senha, email);


        Alert alerta = new  Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("sucesso!!");
        alerta.setHeaderText(null);
        alerta.setContentText("senha alterada com sucesso");
        alerta.showAndWait();


    }
}
