package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.Utensilio;
import com.sigec.system.sigec.DAOS.UtensilioDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistroAvariaUtensilioController implements Initializable {

    private static final ExecutorService DB_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "SIGEC-RegistroAvaria-Worker");
        t.setDaemon(true);
        return t;
    });

    @FXML private TextField txtUtensilio;
    @FXML private ChoiceBox<String> cbEstado;
    @FXML private TextArea txtObservacao;

    @FXML private Button btnVoltar;
    @FXML private Button btnSalvar;

    public static Utensilio utensilioSelecionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cbEstado != null) {
            // Valores correspondentes ao ENUM do banco de dados
            cbEstado.setItems(FXCollections.observableArrayList("PRONTO", "EM_MANUTENCAO", "DANIFICADO"));
        }

        final Utensilio alvo = utensilioSelecionado;
        if (alvo != null) {
            if (txtUtensilio != null) {
                txtUtensilio.setText(alvo.getNome());
            }

            if (cbEstado != null) {
                // Preenche imediatamente com o status já carregado na tabela (0ms de espera)
                String statusMemoria = (alvo.getStatus() != null && !alvo.getStatus().isBlank())
                        ? alvo.getStatus().trim()
                        : "PRONTO";
                cbEstado.setValue(statusMemoria);

                // Confirma em background o último estado diretamente no banco sem travar a abertura da janela
                final int idUtensilio = alvo.getIdUtensilio();
                CompletableFuture.supplyAsync(() -> UtensilioDAO.buscarUltimoEstadoUtensilio(idUtensilio), DB_EXECUTOR)
                        .thenAccept(estadoAtualBanco -> Platform.runLater(() -> {
                            if (cbEstado != null && estadoAtualBanco != null) {
                                cbEstado.setValue(estadoAtualBanco);
                            }
                        }));
            }
        } else {
            if (cbEstado != null) {
                cbEstado.setValue("PRONTO");
            }
        }
    }

    @FXML
    public void onSalvar(ActionEvent event) {
        String estado = cbEstado.getValue();
        String observacao = txtObservacao.getText();

        if (utensilioSelecionado == null) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Nenhum utensílio foi selecionado para avaria.");
            return;
        }

        final int idUtensilio = utensilioSelecionado.getIdUtensilio();
        if (btnSalvar != null) {
            btnSalvar.setDisable(true);
        }

        CompletableFuture.supplyAsync(() -> UtensilioDAO.registrarAvaria(idUtensilio, observacao, estado), DB_EXECUTOR)
                .thenAccept(sucesso -> Platform.runLater(() -> {
                    if (btnSalvar != null) {
                        btnSalvar.setDisable(false);
                    }
                    if (sucesso) {
                        exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Avaria registada com sucesso!");
                        fecharModal();
                    } else {
                        exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível registar a avaria.");
                    }
                }));
    }

    @FXML
    public void voltarTela(ActionEvent event) {
        fecharModal();
    }

    private void fecharModal() {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        stage.close();

        utensilioSelecionado = null;
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}