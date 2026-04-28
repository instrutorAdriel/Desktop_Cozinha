package com.example.desktop_cozinha;

import com.example.desktop_cozinha.controller.ListaController;
import com.example.desktop_cozinha.model.ListaEstoqueDAO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MainApplication extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        trocadorDeTelas("login.fxml");

        primaryStage.setTitle("Sistema Cozinha");
        primaryStage.show();

        public class Main {
            public static void main(String[] args) {

                ListaController controller = new ListaController();

                controller.listarUsuarios();
            }
        }

    }

    public void trocadorDeTelas(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                MainApplication.class.getResource(fxml)
        );

        Scene scene = new Scene(fxmlLoader.load());

        primaryStage.setScene(scene);
    }
}
