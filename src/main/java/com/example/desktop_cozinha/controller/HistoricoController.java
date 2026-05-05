package com.example.desktop_cozinha.controller;
import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.Historico;
import com.example.desktop_cozinha.model.HistoricoDAO;
import com.example.desktop_cozinha.model.LoginDAO;
import com.mysql.cj.BindValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.control.cell.PropertyValueFactory;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;

import static com.mysql.cj.Messages.getString;


public class HistoricoController {
    @FXML
    private TableView<Historico> historico;

    @FXML
    private TableColumn<Historico, String> nome_produto;

    @FXML
    private TableColumn<Historico, String> nome_usuario;


    @FXML
    private TableColumn<Historico, String> tipo;

    @FXML
    private TableColumn<Historico, String> quantidade;

    @FXML
    private TableColumn<Historico, String> observacao;

    @FXML
    public void initialize() {
        configurarColunas();

    }

    private void configurarColunas() {

        nome_produto.setCellValueFactory(new PropertyValueFactory<>("produto"));
        nome_usuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        tipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        quantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        observacao.setCellValueFactory(new PropertyValueFactory<>("observacao"));
    }

    public void imprimirHistoricoCompleto(ActionEvent actionEvent) {

        HistoricoDAO historico = new HistoricoDAO();
        historico.imprimirHistoricoCompleto();
    }
}
