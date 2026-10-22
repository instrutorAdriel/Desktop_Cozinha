package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Utils.ButtonBorderLapAnimator;
import com.sigec.system.sigec.Utils.DropdownBorderLapAnimator;
import com.sigec.system.sigec.Utils.ScreenTransitionManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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
    private VBox submenuEstoque;

    @FXML
    private Label setaEstoque;

    @FXML
    private Button btnNavCadastro;

    @FXML
    private VBox submenuCadastro;

    @FXML
    private Label setaCadastro;

    @FXML
    private Button btnNavEstoque;

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
        MainApplication.trocadorDeTelas("homeTest.fxml", ScreenTransitionManager.Direction.BACKWARD);
    }


    @FXML
    public void onClickSolicitacoes(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("solicitacoes-extras.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickCalendario(ActionEvent event) {
        // Já está na tela de calendário
    }



    @FXML
    public void onClickRelatorio(ActionEvent event) throws IOException {
        MainApplication.trocadorDeTelas("historico.fxml", ScreenTransitionManager.Direction.FORWARD);
    }

    @FXML
    public void toggleDropdownCadastro(ActionEvent event) {
        if (submenuCadastro != null) {
            boolean expandido = !submenuCadastro.isVisible();
            submenuCadastro.setVisible(expandido);
            submenuCadastro.setManaged(expandido);
            if (setaCadastro != null) {
                setaCadastro.setText(expandido ? "▶" : "▼");
            }
            if (expandido && btnNavCadastro != null) {
                ButtonBorderLapAnimator.animarDropdown(btnNavCadastro, submenuCadastro);
            } else if (!expandido && btnNavCadastro != null) {
                ButtonBorderLapAnimator.cancelarAnimacaoAtiva();
                btnNavCadastro.getStyleClass().remove("btn-dropdown-ativo");
                btnNavCadastro.setStyle(null);
                submenuCadastro.getStyleClass().remove("submenu-lateral-ativo");
                submenuCadastro.setStyle(null);
            }
        }
    }

    @FXML
    public void toggleDropdownEstoque(ActionEvent event) {
        if (submenuEstoque != null && btnNavEstoque != null) {
            boolean expandido = !submenuEstoque.isVisible();

            if (setaEstoque != null) {
                setaEstoque.setText(expandido ? "▲" : "▼");
            }

            if (expandido) {
                submenuEstoque.setVisible(true);
                submenuEstoque.setManaged(true);

                DropdownBorderLapAnimator.animarDropdown(btnNavEstoque, submenuEstoque);
            } else {
                ButtonBorderLapAnimator.cancelarAnimacaoAtiva();

                btnNavEstoque.getStyleClass().remove("btn-dropdown-ativo");
                btnNavEstoque.setStyle(null);
                submenuEstoque.getStyleClass().remove("submenu-lateral-ativo");
                submenuEstoque.setStyle(null);

                submenuEstoque.setVisible(false);
                submenuEstoque.setManaged(false);
            }
        }
    }

    @FXML
    public void onClickInsumos(javafx.event.ActionEvent event) {
        try {
            com.sigec.system.sigec.MainApplication.trocadorDeTelas("lista-estoque.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao carregar a tela de Insumos: " + e.getMessage());
        }
    }

    @FXML
    public void onClickUtensilios(javafx.event.ActionEvent event) {
        try {
            com.sigec.system.sigec.MainApplication.trocadorDeTelas("gestao-utensilios.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao carregar a tela de Utensílios: " + e.getMessage());
        }
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