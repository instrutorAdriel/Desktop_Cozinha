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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EdicaoUtensilioController implements Initializable {

    @FXML private TextField txtNomeUtensilio;
    @FXML private TextField txtQuantidade;
    @FXML private ChoiceBox<String> cbLaboratorio;
    @FXML private ChoiceBox<String> cbStatus;

    @FXML private Button btnVoltar;
    @FXML private Button btnSalvar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cbLaboratorio != null) {
            // Puxa a lista real da base de dados através do DAO
            List<String> laboratorios = com.sigec.system.sigec.DAOS.LaboratorioDAO.listarNomesLaboratoriosAtivos();
            cbLaboratorio.setItems(FXCollections.observableArrayList(laboratorios));

            // Seleciona o primeiro item por defeito, se a lista não estiver vazia
            if (!laboratorios.isEmpty()) {
                cbLaboratorio.setValue(laboratorios.get(0));
            }
        }

        if (cbStatus != null) {
            // Estes estados refletem a estrutura lógica da base de dados
            cbStatus.setItems(FXCollections.observableArrayList("Pronto", "Em Manutenção", "Danificado"));
            cbStatus.setValue("Pronto");
        }
    }

    @FXML
    public void onSalvar(ActionEvent event) {
        String nome = txtNomeUtensilio.getText();
        String laboratorio = cbLaboratorio.getValue();
        String status = cbStatus.getValue();

        if (nome == null || nome.trim().isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "O nome do utensílio é obrigatório.");
            return;
        }

        // Simulação da chamada ao DAO
        boolean sucesso = true; // UtensilioDAO.atualizarUtensilio(...)

        if (sucesso) {
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Operação realizada com sucesso!");
            fecharModal();
        } else {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar as informações no banco de dados.");
        }
    }

    @FXML
    public void voltarTela(ActionEvent event) {
        fecharModal();
    }

    private void fecharModal() {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        stage.close();
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}