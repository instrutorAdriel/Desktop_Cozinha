package com.example.desktop_cozinha.controller;
import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.Historico;
import com.example.desktop_cozinha.model.HistoricoDAO;
import com.example.desktop_cozinha.model.LoginDAO;
import com.mysql.cj.BindValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private TableView<Historico> Historico;

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
    private TableColumn<Historico, String> data_hora;

    @FXML
    public void initialize() {
        configurarColunas();


    }

    private void configurarColunas() {

        nome_produto.setCellValueFactory(new PropertyValueFactory<>("nome_produto"));
        nome_usuario.setCellValueFactory(new PropertyValueFactory<>("nome_usuario"));
        tipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        quantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        observacao.setCellValueFactory(new PropertyValueFactory<>("observacao"));
        data_hora.setCellValueFactory(new PropertyValueFactory<>("data_hora"));
    }

    public void imprimirHistoricoCompleto(ActionEvent actionEvent) {

        HistoricoDAO historico = new HistoricoDAO();
        historico.imprimirHistoricoCompleto();

        carregarDados();
    }
    private void carregarDados() {

        ObservableList<Historico> lista =
                FXCollections.observableArrayList(HistoricoDAO.imprimirHistoricoCompleto());

        Historico.setItems(lista);
    }
}
