package com.sigec.system.sigec;

import com.sigec.system.sigec.Utils.ScreenTransitionManager;
import com.sigec.system.sigec.Utils.TabelaUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    private static Stage primaryStage;
    private static StackPane rootContainer;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        rootContainer = new StackPane();

        // Define a cena inicial usando o StackPane como raiz
        Scene scene = new Scene(rootContainer, 1366, 768);
        primaryStage.setScene(scene);

        // Sempre que qualquer tela nova entrar no rootContainer, trava TODAS as TableViews dela
        rootContainer.getChildren().addListener((javafx.collections.ListChangeListener<Node>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Node adicionado : change.getAddedSubList()) {
                        if (adicionado instanceof Parent parent) {
                            aplicarTravaGlobalEmTabelas(parent);
                        }
                    }
                }
            }
        });

        // Carrega a primeira tela
        trocadorDeTelas("login.fxml");

        primaryStage.setTitle("Sistema de Gestão de Estoque da Cozinha");
        stage.getIcons().add(
                new Image(
                        MainApplication.class.getResource(
                                "/com/sigec/system/sigec/imagens/logo-white.png"
                        ).toExternalForm()
                )
        );
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void trocadorDeTelas(String fxml) throws IOException {
        trocadorDeTelas(fxml, ScreenTransitionManager.Direction.AUTO);
    }

    public static void trocadorDeTelas(String fxml, ScreenTransitionManager.Direction direction) throws IOException {
        if (rootContainer == null) {
            throw new IllegalStateException("rootContainer não foi inicializado.");
        }
        ScreenTransitionManager.trocarTela(rootContainer, fxml, direction);
        aplicarTravaGlobalEmTabelas(rootContainer);
    }

    public static void abrirPopUp(String fxml) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(fxml));
            Parent root = fxmlLoader.load();

            aplicarTravaGlobalEmTabelas(root);

            Stage popupStage = new Stage();
            Scene scene = new Scene(root);

            popupStage.setScene(scene);
            popupStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            popupStage.initOwner(primaryStage);
            popupStage.setResizable(true);
            popupStage.showAndWait();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void aplicarTravaGlobalEmTabelas(Parent container) {
        Platform.runLater(() -> {
            for (Node node : container.lookupAll(".table-view")) {
                if (node instanceof TableView<?> tabela) {
                    TabelaUtils.configurarTabelaFixa(tabela);
                }
            }
        });
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static StackPane getRootContainer() {
        return rootContainer;
    }
}