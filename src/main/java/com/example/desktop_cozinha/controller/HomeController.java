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

import static com.example.desktop_cozinha.services.SessaoService.emailAtual;

public class HomeController implements Initializable {
    @FXML
    private Label emailLabel;

    @FXML
    private Label usuarioLabel;

    public void configurarRelogio(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        Timeline time = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            emailLabel.setText(dtf.format(LocalDateTime.now()));
        }));

        time.setCycleCount(Timeline.INDEFINITE);

        emailLabel.setText(LocalDateTime.now().format(dtf));

        time.play();

    }

    public void usuarioAtual(){
        String email = SessaoService.getEmailAtual();
        IO.println(email);
        if  (email != null){
            HomeDAO user = new HomeDAO();
            String nome = user.bucarNome(email);
            IO.println(nome);
            usuarioLabel.setText(nome);
        }
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarRelogio();
        usuarioAtual();
    }
}
