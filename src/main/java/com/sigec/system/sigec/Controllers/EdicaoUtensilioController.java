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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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

    @FXML private VBox popupOverlay;
    @FXML private VBox popupCard;
    @FXML private Label lblPopupIcone;
    @FXML private Label lblPopupTitulo;
    @FXML private Label lblPopupMensagem;
    @FXML private Button btnPopupOk;

    private boolean fecharModalAoConfirmarPopup = false;

    public static Utensilio utensilioParaEditar = null;
    private Map<String, Integer> mapaCategorias;
    private Map<String, Integer> mapaLaboratorios;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cbLaboratorio != null) {
            mapaLaboratorios = LaboratorioDAO.listarLaboratoriosAtivosMap();
            cbLaboratorio.setItems(FXCollections.observableArrayList(mapaLaboratorios.keySet()));
        }

        if (cbCategoria != null) {
            mapaCategorias = CategoriaUtensilioDAO.listarCategoriasAtivas();
            cbCategoria.setItems(FXCollections.observableArrayList(mapaCategorias.keySet()));
            if (!mapaCategorias.isEmpty()) {
                cbCategoria.setValue(mapaCategorias.keySet().iterator().next());
            }
        }

        if (utensilioParaEditar != null) {
            txtNomeUtensilio.setText(utensilioParaEditar.getNome());
            if (txtQuantidade != null) {
                txtQuantidade.setText(String.valueOf(utensilioParaEditar.getQtdTotal()));
            }
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
        String strPatrimonio = txtPatrimonio.getText();
        String nomeCategoria = cbCategoria.getValue();
        String nomeLaboratorio = cbLaboratorio.getValue();

        if (nome == null || nome.trim().isEmpty() || strPatrimonio == null || strPatrimonio.trim().isEmpty()) {
            exibirPopupEstilizado(Alert.AlertType.WARNING, "Atenção", "Preencha todos os campos obrigatórios.", false);
            return;
        }

        try {
            int qtd = utensilioParaEditar.getQtdTotal() > 0 ? utensilioParaEditar.getQtdTotal() : 1;
            if (txtQuantidade != null && txtQuantidade.getText() != null && !txtQuantidade.getText().trim().isEmpty()) {
                qtd = Integer.parseInt(txtQuantidade.getText().trim());
            }

            int patrimonio = Integer.parseInt(strPatrimonio.trim());

            if (UtensilioDAO.existeNumeroPatrimonio(patrimonio, utensilioParaEditar.getIdUtensilio())) {
                exibirPopupEstilizado(
                        Alert.AlertType.ERROR,
                        "Erro na Edição",
                        "Nao foi possivel cadastrar o produto ERROR 890: \nNumero de patrimonio ja existe!",
                        false
                );
                return;
            }

            int idCategoria = mapaCategorias.getOrDefault(nomeCategoria, 1);
            int idLaboratorio = mapaLaboratorios.getOrDefault(nomeLaboratorio, 1);

            utensilioParaEditar.setNome(nome.trim());
            utensilioParaEditar.setQtdTotal(qtd);
            utensilioParaEditar.setQtdDisponivel(qtd);
            utensilioParaEditar.setNumeroPatrimonio(patrimonio);
            utensilioParaEditar.setIdCategoria(idCategoria);
            utensilioParaEditar.setIdLaboratorio(idLaboratorio);

            boolean sucesso = UtensilioDAO.atualizarUtensilio(utensilioParaEditar);

            if (sucesso) {
                exibirPopupEstilizado(Alert.AlertType.INFORMATION, "Sucesso", "Utensílio atualizado com sucesso!", true);
            } else {
                exibirPopupEstilizado(Alert.AlertType.ERROR, "Erro", "Não foi possível atualizar as informações no banco de dados.", false);
            }
        } catch (NumberFormatException e) {
            exibirPopupEstilizado(Alert.AlertType.ERROR, "Erro", "O Nº do Patrimônio deve ser um número válido.", false);
        }
    }

    @FXML
    public void onFecharPopup(ActionEvent event) {
        if (popupOverlay != null) {
            popupOverlay.setVisible(false);
            popupOverlay.setManaged(false);
        }
        if (fecharModalAoConfirmarPopup) {
            fecharModal();
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

    private void exibirPopupEstilizado(Alert.AlertType tipo, String titulo, String mensagem, boolean fecharAoConfirmar) {
        this.fecharModalAoConfirmarPopup = fecharAoConfirmar;

        if (popupOverlay != null && lblPopupTitulo != null && lblPopupMensagem != null) {
            lblPopupTitulo.setText(titulo);
            lblPopupMensagem.setText(mensagem);

            if (lblPopupIcone != null) {
                if (tipo == Alert.AlertType.INFORMATION) {
                    lblPopupIcone.setText("✔");
                    lblPopupIcone.setStyle("-fx-background-color: #dcfce7; -fx-background-radius: 24px; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
                } else if (tipo == Alert.AlertType.WARNING) {
                    lblPopupIcone.setText("⚠");
                    lblPopupIcone.setStyle("-fx-background-color: #fef3c7; -fx-background-radius: 24px; -fx-text-fill: #d97706; -fx-font-weight: bold;");
                } else {
                    lblPopupIcone.setText("✖");
                    lblPopupIcone.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 24px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
                }
            }

            popupOverlay.setVisible(true);
            popupOverlay.setManaged(true);
            if (btnPopupOk != null) {
                btnPopupOk.requestFocus();
            }
        } else {
            Alert alert = new Alert(tipo);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensagem);
            alert.showAndWait();
            if (fecharAoConfirmar) {
                fecharModal();
            }
        }
    }
}
