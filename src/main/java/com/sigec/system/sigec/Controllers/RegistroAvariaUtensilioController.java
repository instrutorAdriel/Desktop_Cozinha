package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.DAOS.UtensilioDAO;
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

public class RegistroAvariaUtensilioController implements Initializable {

    @FXML private TextField txtUtensilio;
    @FXML private ChoiceBox<String> cbEstado;
    @FXML private TextArea txtObservacao;

    @FXML private Button btnVoltar;
    @FXML private Button btnSalvar;

    // Variável estática para receber o ID do utensílio selecionado na tela principal
    public static int idUtensilioSelecionado = -1;
    public static String nomeUtensilioSelecionado = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (txtUtensilio != null) {
            txtUtensilio.setText(nomeUtensilioSelecionado);
        }

        if (cbEstado != null) {
            // Valores correspondentes ao ENUM do banco de dados
            cbEstado.setItems(FXCollections.observableArrayList("PRONTO", "EM_MANUTENCAO", "DANIFICADO"));

            // Puxando o estado do banco de dados para o utensílio selecionado
            if (idUtensilioSelecionado != -1) {
                String estadoAtualBanco = UtensilioDAO.buscarUltimoEstadoUtensilio(idUtensilioSelecionado);
                cbEstado.setValue(estadoAtualBanco);
            } else {
                cbEstado.setValue("PRONTO"); // Fallback de segurança
            }
        }
    }

    @FXML
    public void onSalvar(ActionEvent event) {
        String estado = cbEstado.getValue();
        String observacao = txtObservacao.getText();

        if (idUtensilioSelecionado == -1) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Nenhum utensílio foi selecionado para avaria.");
            return;
        }

        boolean sucesso = UtensilioDAO.registrarAvaria(idUtensilioSelecionado, observacao, estado);


        if (sucesso) {
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Avaria registrada com sucesso!");
            fecharModal();
        } else {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível registrar a avaria.");
        }
    }

    @FXML
    public void voltarTela(ActionEvent event) {
        fecharModal();
    }

    private void fecharModal() {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        stage.close();

        // Limpa a seleção após fechar para evitar sujeira de memória
        idUtensilioSelecionado = -1;
        nomeUtensilioSelecionado = "";
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}