package com.example.desktop_cozinha;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        trocadorDeTelas("lista-estoque.fxml");

        primaryStage.setTitle("Sistema Cozinha");
        primaryStage.show();
    }

    public static void trocadorDeTelas(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                MainApplication.class.getResource(fxml)
        );

        Scene scene = new Scene(fxmlLoader.load(), 500,400);

        primaryStage.setScene(scene);
    }
}
