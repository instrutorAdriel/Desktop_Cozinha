package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.AlertaProduto;
import com.example.desktop_cozinha.model.AlertaProdutoDAO;
import com.example.desktop_cozinha.model.HomeDAO;
import com.example.desktop_cozinha.services.SessaoService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import static com.example.desktop_cozinha.MainApplication.sair;
import static com.example.desktop_cozinha.MainApplication.trocadorDeTelas;

public class HomeController implements Initializable {

    @FXML
    private Label dataLabel;

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label horaLabel;

    @FXML
    private Button botaoSair;


    @FXML
    private TableView<AlertaProduto> tabelaAlertas;

    @FXML private TableColumn<AlertaProduto, String> colProduto;
    @FXML private TableColumn<AlertaProduto, String> colValidade;
    @FXML private TableColumn<AlertaProduto, String> colStatus;
    @FXML private TableColumn<AlertaProduto, String> colQuantidade;

    private final AlertaProdutoDAO alertaDAO = new AlertaProdutoDAO();
    private final ObservableList<AlertaProduto> listaAlertas = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colProduto.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colValidade.setCellValueFactory(new PropertyValueFactory<>("dataValidade"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        configurarEstiloLinhas();
        atualizarTabela();
        configurarRelogio();
        usuarioAtual();
    }

    public void configurarRelogio() {
        DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatadorHora = DateTimeFormatter.ofPattern("HH:mm");

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime agora = LocalDateTime.now();
            dataLabel.setText(agora.format(formatadorData));
            horaLabel.setText(agora.format(formatadorHora));
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);

        LocalDateTime agoraInicial = LocalDateTime.now();
        dataLabel.setText(agoraInicial.format(formatadorData));
        horaLabel.setText(agoraInicial.format(formatadorHora));

        timeline.play();
    }

    public void usuarioAtual() {
        String email = SessaoService.getEmailAtual();
        if (email != null) {
            HomeDAO user = new HomeDAO();
            String nome = user.bucarNome(email);
            usuarioLabel.setText("Bem vindo, " + nome);
        }
    }

    @FXML
    public void atualizarTabela() {
        listaAlertas.clear();
        List<AlertaProduto> dadosDoBanco = alertaDAO.buscarProdutosEmAlerta();
        listaAlertas.addAll(dadosDoBanco);
        tabelaAlertas.setItems(listaAlertas);
    }
    private void configurarEstiloLinhas() {
        tabelaAlertas.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(AlertaProduto item, boolean empty) {
                super.updateItem(item, empty);
            }
        });
    }

    public void botaoEstoqueAction() throws IOException {
        trocadorDeTelas("lista-estoque.fxml");
    }

    public void botaoRelatorio() throws IOException {
        MainApplication.trocadorDeTelas("historico.fxml");
    }

    public void botaoSairAction( ) throws IOException {
        sair();
    }
}
