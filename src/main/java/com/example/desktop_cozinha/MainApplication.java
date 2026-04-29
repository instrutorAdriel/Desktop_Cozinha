package com.example.desktop_cozinha;

import com.sun.tools.javac.Main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    // Guarda o Stage principal para ser acessado de qualquer controller
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Carrega a tela inicial (carrega tela inicial)
        trocadorDeTelas("confirmaEmail.fxml");

        primaryStage.setTitle("Sistema genérico");
        primaryStage.show();
    }

    /**
     * Metodo estático para trocar de tela a partir de qualquer controller.
     *
     * @param telaFxml Nome do arquivo .fxml (ex: "cadastro.fxml")
     */
    public static void trocadorDeTelas(String telaFxml) throws IOException {
        // Carrega o arquivo .fxml do diretório resources/com/example/demo/
        FXMLLoader loader = new FXMLLoader(
                MainApplication.class.getResource(telaFxml)
        );

        // Cria a cena com o conteúdo carregado
        Scene scene = new Scene(loader.load());

        // Define a cena no Stage principal
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}
