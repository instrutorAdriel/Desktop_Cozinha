package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.Utensilio;
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

    public static Utensilio utensilioSelecionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (cbEstado != null) {
            // Valores correspondentes ao ENUM do banco de dados
            cbEstado.setItems(FXCollections.observableArrayList("PRONTO", "EM_MANUTENCAO", "DANIFICADO"));
        }


        if (utensilioSelecionado != null) {
            if (txtUtensilio != null) {
                txtUtensilio.setText(utensilioSelecionado.getNome());
            }

            if (cbEstado != null) {
                String estadoAtualBanco = UtensilioDAO.buscarUltimoEstadoUtensilio(utensilioSelecionado.getIdUtensilio());
                cbEstado.setValue(estadoAtualBanco);
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


        boolean sucesso = UtensilioDAO.registrarAvaria(utensilioSelecionado.getIdUtensilio(), observacao, estado);

        if (sucesso) {
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Avaria registada com sucesso!");
            fecharModal();
        } else {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível registar a avaria.");
        }
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