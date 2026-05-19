package com.example.desktop_cozinha;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class MainApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        trocadorDeTelas("login.fxml");

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

    public static void abrirPopUp(String fxml) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(fxml));
            Parent root = fxmlLoader.load();

            Stage popupStage = new Stage();

            Scene scene = new Scene(root);

            popupStage.setScene(scene);
            popupStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            popupStage.initOwner(primaryStage);
            popupStage.setResizable(false);
            popupStage.showAndWait();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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
