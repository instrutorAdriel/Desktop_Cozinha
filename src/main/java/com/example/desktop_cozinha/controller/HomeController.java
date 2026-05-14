package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.model.HomeDAO;
import com.example.desktop_cozinha.services.SessaoService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.util.Duration;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private Label dataLabel;

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label horaLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
}
