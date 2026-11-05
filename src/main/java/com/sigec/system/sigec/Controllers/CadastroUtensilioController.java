package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.User;
import com.sigec.system.sigec.Constructors.Utensilio;
import com.sigec.system.sigec.DAOS.CategoriaUtensilioDAO;
import com.sigec.system.sigec.DAOS.LaboratorioDAO;
import com.sigec.system.sigec.DAOS.UnidadeDAO;
import com.sigec.system.sigec.DAOS.UtensilioDAO;
import com.sigec.system.sigec.Services.SessaoService;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CadastroUtensilioController implements Initializable {

    @FXML private TextField txtNomeUtensilio;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtPatrimonio;
    @FXML private ChoiceBox<String> cbUnidade;
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

    private Map<String, Integer> mapaUnidades = new LinkedHashMap<>();
    private Map<String, Integer> mapaCategorias;
    private Map<String, Integer> mapaLaboratorios;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cbUnidade != null) {
            User usuarioLogado = SessaoService.getUsuarioAtual();
            if (usuarioLogado != null && usuarioLogado.getIdUsuario() > 0) {
                mapaUnidades = UnidadeDAO.listarUnidadesPorUsuarioMap(usuarioLogado.getIdUsuario());
            }
            cbUnidade.setItems(FXCollections.observableArrayList(mapaUnidades.keySet()));
            if (!mapaUnidades.isEmpty()) {
                cbUnidade.setValue(mapaUnidades.keySet().iterator().next());
            }
        }

        if (cbLaboratorio != null) {
            mapaLaboratorios = LaboratorioDAO.listarLaboratoriosAtivosMap();
            cbLaboratorio.setItems(FXCollections.observableArrayList(mapaLaboratorios.keySet()));

            if (!mapaLaboratorios.isEmpty()) {
                cbLaboratorio.setValue(mapaLaboratorios.keySet().iterator().next());
            }
        }

        if (cbCategoria != null) {
            mapaCategorias = CategoriaUtensilioDAO.listarCategoriasAtivas();
            cbCategoria.setItems(FXCollections.observableArrayList(mapaCategorias.keySet()));

            if (!mapaCategorias.isEmpty()) {
                cbCategoria.setValue(mapaCategorias.keySet().iterator().next());
            }
        }
    }

    @FXML
    public void onSalvar(ActionEvent event) {
        String nome = txtNomeUtensilio.getText();
        String strPatrimonio = txtPatrimonio.getText();

        String nomeUnidade = cbUnidade != null ? cbUnidade.getValue() : null;
        String nomeCategoria = cbCategoria.getValue();
        String nomeLaboratorio = cbLaboratorio.getValue();

        if (nome == null || nome.trim().isEmpty() ||
                strPatrimonio == null || strPatrimonio.trim().isEmpty() ||
                nomeUnidade == null || nomeCategoria == null || nomeLaboratorio == null) {

            exibirPopupEstilizado(Alert.AlertType.WARNING, "Atenção", "Preencha todos os campos obrigatórios.", false);
            return;
        }

        try {
            int qtd = 1;
            if (txtQuantidade != null && txtQuantidade.getText() != null && !txtQuantidade.getText().trim().isEmpty()) {
                qtd = Integer.parseInt(txtQuantidade.getText().trim());
            }

            int patrimonio = Integer.parseInt(strPatrimonio.trim());

            if (UtensilioDAO.existeNumeroPatrimonio(patrimonio)) {
                exibirPopupEstilizado(
                        Alert.AlertType.ERROR,
                        "Erro no Cadastro",
                        "Nao foi possivel cadastrar o produto ERROR 890: \nNumero de patrimonio ja existe!",
                        false
                );
                return;
            }

            int idUnidade = mapaUnidades.getOrDefault(nomeUnidade, 0);
            int idCategoria = mapaCategorias.get(nomeCategoria);
            int idLaboratorio = mapaLaboratorios.get(nomeLaboratorio);

            Utensilio novoUtensilio = new Utensilio(
                    0,
                    nome.trim(),
                    nomeLaboratorio,
                    "A",
                    qtd,
                    qtd,
                    patrimonio,
                    idUnidade,
                    idCategoria,
                    idLaboratorio
            );

            boolean sucesso = UtensilioDAO.adicionarUtensilio(novoUtensilio);

            if (sucesso) {
                exibirPopupEstilizado(Alert.AlertType.INFORMATION, "Sucesso", "Utensílio adicionado com sucesso!", true);
            } else {
                exibirPopupEstilizado(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar as informações na base de dados.", false);
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
