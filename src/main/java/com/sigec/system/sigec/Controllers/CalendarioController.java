package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Utils.ScreenTransitionManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CalendarioController {

    @FXML
    private Label dataLabel;

    @FXML
    private Label horaLabel;

    @FXML
    public void initialize() {
        // Formata e injeta a data atual (Ex: 19/10/2026)
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (dataLabel != null) {
            dataLabel.setText(LocalDate.now().format(dataFormatter));
        }

        // Cria a linha do tempo para atualizar o relógio a cada segundo (Ex: 11:04:15)
        if (horaLabel != null) {
            DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
                horaLabel.setText(LocalTime.now().format(horaFormatter));
            }), new KeyFrame(Duration.seconds(1)));

            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();
        }
    }

    @FXML
    public void onBuscarInsumos(ActionEvent event) {
        // Lógica para filtrar as datas da tabela
    }

    @FXML
    public void onGerarRelatorio(ActionEvent event) {
        // Lógica para gerar o relatório PDF/XLSX
    }

    @FXML
    public void onClickHome(ActionEvent event) throws IOException {
        MainApplication.trocadorDeTelas("home.fxml", ScreenTransitionManager.Direction.BACKWARD);
    }

    @FXML
    public void onClickCalendario(ActionEvent event) {
        // Já está na tela de calendário
    }

    @FXML
    public void onClickEstoque(ActionEvent event) throws IOException {
        MainApplication.trocadorDeTelas("lista-estoque.fxml", ScreenTransitionManager.Direction.FORWARD);
    }

    @FXML
    public void onClickRelatorio(ActionEvent event) throws IOException {
        MainApplication.trocadorDeTelas("historico.fxml", ScreenTransitionManager.Direction.FORWARD);
    }

    @FXML
    public void toggleDropdownCadastro(ActionEvent event) {
        // O gerenciamento visual do dropdown é feito pelo SidebarNavigationManager
    }

    @FXML
    public void onClickCadastroUsuario(ActionEvent event) throws IOException {
        MainApplication.trocadorDeTelas("cadastro.fxml", ScreenTransitionManager.Direction.FORWARD);
    }

    @FXML
    public void onClickCadastroTurma(ActionEvent event) throws IOException {
        MainApplication.trocadorDeTelas("cadastro-turma.fxml", ScreenTransitionManager.Direction.FORWARD);
    }

    @FXML
    public void botaoSairAction(ActionEvent event) throws IOException {
        MainApplication.trocadorDeTelas("login.fxml");
    }
}