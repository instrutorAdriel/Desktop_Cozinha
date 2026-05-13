package com.example.desktop_cozinha;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class MainApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        trocadorDeTelas("historico.fxml");

        primaryStage.setTitle("Sistema Cozinha");
        primaryStage.show();
    }

    public static void trocadorDeTelas(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                MainApplication.class.getResource(fxml)
        );

        Scene scene = new Scene(fxmlLoader.load(),1366,768);

        primaryStage.setScene(scene);
    }
    public static void sair () throws IOException {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Sair");
        alerta.setHeaderText(null);
        alerta.setContentText("Deseja realmente sair?");
        ButtonType sim = new ButtonType("SIM");
        ButtonType nao = new ButtonType("NÃO");
        alerta.getButtonTypes().setAll(sim,nao);
        Optional<ButtonType> escolha = alerta.showAndWait();
        if (escolha.get() == sim) {
            MainApplication.trocadorDeTelas("login.fxml");
        }
        else {
            return;
        }
    }
}
