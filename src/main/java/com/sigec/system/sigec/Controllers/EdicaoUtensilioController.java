package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.Utensilio;
import com.sigec.system.sigec.DAOS.UtensilioDAO;
import com.sigec.system.sigec.DAOS.LaboratorioDAO;
import com.sigec.system.sigec.DAOS.CategoriaUtensilioDAO;
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
import java.util.Map;
import java.util.ResourceBundle;

public class EdicaoUtensilioController implements Initializable {

    @FXML private TextField txtNomeUtensilio;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtPatrimonio;
    @FXML private ChoiceBox<String> cbCategoria;
    @FXML private ChoiceBox<String> cbLaboratorio;

    @FXML private Button btnVoltar;
    @FXML private Button btnSalvar;

    public static Utensilio utensilioParaEditar = null;
    private Map<String, Integer> mapaCategorias;
    private Map<String, Integer> mapaLaboratorios;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Carrega os mapas do banco
        if (cbLaboratorio != null) {
            mapaLaboratorios = LaboratorioDAO.listarLaboratoriosAtivosMap();
            cbLaboratorio.setItems(FXCollections.observableArrayList(mapaLaboratorios.keySet()));
        }

        if (cbCategoria != null) {
            mapaCategorias = CategoriaUtensilioDAO.listarCategoriasAtivas();
            cbCategoria.setItems(FXCollections.observableArrayList(mapaCategorias.keySet()));
        }

        // Preenche os campos com os dados do utensílio selecionado
        if (utensilioParaEditar != null) {
            txtNomeUtensilio.setText(utensilioParaEditar.getNome());
            txtQuantidade.setText(String.valueOf(utensilioParaEditar.getQtdTotal()));
            txtPatrimonio.setText(String.valueOf(utensilioParaEditar.getNumeroPatrimonio()));

            if (utensilioParaEditar.getLaboratorio() != null) {
                cbLaboratorio.setValue(utensilioParaEditar.getLaboratorio());
            }
        }
    }

    @FXML
    public void onSalvar(ActionEvent event) {
        if (utensilioParaEditar == null) return;

        String nome = txtNomeUtensilio.getText();
        String strQtd = txtQuantidade.getText();
        String strPatrimonio = txtPatrimonio.getText();
        String nomeCategoria = cbCategoria.getValue();
        String nomeLaboratorio = cbLaboratorio.getValue();

        if (nome == null || nome.trim().isEmpty() || strQtd == null || strQtd.trim().isEmpty() || strPatrimonio == null || strPatrimonio.trim().isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Preencha todos os campos obrigatórios.");
            return;
        }

        try {
            int qtd = Integer.parseInt(strQtd);
            int patrimonio = Integer.parseInt(strPatrimonio);
            int idCategoria = mapaCategorias.getOrDefault(nomeCategoria, 1);
            int idLaboratorio = mapaLaboratorios.getOrDefault(nomeLaboratorio, 1);

            // Atualiza os dados no objeto
            utensilioParaEditar.setNome(nome);
            utensilioParaEditar.setQtdTotal(qtd);
            utensilioParaEditar.setQtdDisponivel(qtd);
            utensilioParaEditar.setNumeroPatrimonio(patrimonio);
            utensilioParaEditar.setIdCategoria(idCategoria);
            utensilioParaEditar.setIdLaboratorio(idLaboratorio);

            // Executa o UPDATE no DAO
            boolean sucesso = UtensilioDAO.atualizarUtensilio(utensilioParaEditar);

            if (sucesso) {
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Utensílio atualizado com sucesso!");
                fecharModal();
            } else {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível atualizar as informações no banco de dados.");
            }
        } catch (NumberFormatException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Quantidade e Patrimônio devem ser números válidos.");
        }
    }

    @FXML
    public void voltarTela(ActionEvent event) {
        fecharModal();
    }

    private void fecharModal() {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        stage.close();
        utensilioParaEditar = null;
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}