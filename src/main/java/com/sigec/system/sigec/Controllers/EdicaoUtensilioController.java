package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.Utensilio;
import com.sigec.system.sigec.DAOS.UtensilioDAO;
import com.sigec.system.sigec.DAOS.LaboratorioDAO;
import com.sigec.system.sigec.DAOS.CategoriaUtensilioDAO;
import javafx.application.Platform;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EdicaoUtensilioController implements Initializable {

    private static final ExecutorService DB_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "SIGEC-EdicaoUtensilio-Worker");
        t.setDaemon(true);
        return t;
    });

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
    private Map<String, Integer> mapaCategorias = new LinkedHashMap<>();
    private Map<String, Integer> mapaLaboratorios = new LinkedHashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final Utensilio alvo = utensilioParaEditar;

        if (alvo != null) {
            txtNomeUtensilio.setText(alvo.getNome());
            if (txtQuantidade != null) {
                txtQuantidade.setText(String.valueOf(alvo.getQtdTotal()));
            }
            txtPatrimonio.setText(String.valueOf(alvo.getNumeroPatrimonio()));
        }

        // Thread 1: Carrega laboratórios ativos em paralelo
        if (cbLaboratorio != null) {
            CompletableFuture.supplyAsync(LaboratorioDAO::listarLaboratoriosAtivosMap, DB_EXECUTOR)
                    .thenAccept(labs -> Platform.runLater(() -> {
                        mapaLaboratorios = labs != null ? labs : new LinkedHashMap<>();
                        cbLaboratorio.setItems(FXCollections.observableArrayList(mapaLaboratorios.keySet()));
                        if (alvo != null && alvo.getLaboratorio() != null) {
                            cbLaboratorio.setValue(alvo.getLaboratorio());
                        }
                    }));
        }

        // Thread 2: Carrega categorias ativas em paralelo
        if (cbCategoria != null) {
            CompletableFuture.supplyAsync(CategoriaUtensilioDAO::listarCategoriasAtivas, DB_EXECUTOR)
                    .thenAccept(cats -> Platform.runLater(() -> {
                        mapaCategorias = cats != null ? cats : new LinkedHashMap<>();
                        cbCategoria.setItems(FXCollections.observableArrayList(mapaCategorias.keySet()));

                        String categoriaSelecionada = null;
                        if (alvo != null && alvo.getIdCategoria() > 0) {
                            for (Map.Entry<String, Integer> entry : mapaCategorias.entrySet()) {
                                if (entry.getValue() != null && entry.getValue() == alvo.getIdCategoria()) {
                                    categoriaSelecionada = entry.getKey();
                                    break;
                                }
                            }
                        }

                        if (categoriaSelecionada != null) {
                            cbCategoria.setValue(categoriaSelecionada);
                        } else if (!mapaCategorias.isEmpty()) {
                            cbCategoria.setValue(mapaCategorias.keySet().iterator().next());
                        }
                    }));
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

        final int qtd;
        final int patrimonio;
        try {
            int qtdTemp = utensilioParaEditar.getQtdTotal() > 0 ? utensilioParaEditar.getQtdTotal() : 1;
            if (txtQuantidade != null && txtQuantidade.getText() != null && !txtQuantidade.getText().trim().isEmpty()) {
                qtdTemp = Integer.parseInt(txtQuantidade.getText().trim());
            }
            qtd = qtdTemp;
            patrimonio = Integer.parseInt(strPatrimonio.trim());
        } catch (NumberFormatException e) {
            exibirPopupEstilizado(Alert.AlertType.ERROR, "Erro", "O Nº do Patrimônio deve ser um número válido.", false);
            return;
        }

        final Utensilio alvo = utensilioParaEditar;
        int idCategoria = mapaCategorias.getOrDefault(nomeCategoria, alvo.getIdCategoria() > 0 ? alvo.getIdCategoria() : 1);
        int idLaboratorio = mapaLaboratorios.getOrDefault(nomeLaboratorio, alvo.getIdLaboratorio() > 0 ? alvo.getIdLaboratorio() : 1);

        if (btnSalvar != null) {
            btnSalvar.setDisable(true);
        }

        CompletableFuture.runAsync(() -> {
            if (UtensilioDAO.existeNumeroPatrimonio(patrimonio, alvo.getIdUtensilio())) {
                Platform.runLater(() -> {
                    if (btnSalvar != null) btnSalvar.setDisable(false);
                    exibirPopupEstilizado(
                            Alert.AlertType.ERROR,
                            "Erro na Edição",
                            "Nao foi possivel cadastrar o produto ERROR 890: \nNumero de patrimonio ja existe!",
                            false
                    );
                });
                return;
            }

            alvo.setNome(nome.trim());
            alvo.setQtdTotal(qtd);
            alvo.setQtdDisponivel(qtd);
            alvo.setNumeroPatrimonio(patrimonio);
            alvo.setIdCategoria(idCategoria);
            alvo.setIdLaboratorio(idLaboratorio);

            boolean sucesso = UtensilioDAO.atualizarUtensilio(alvo);

            Platform.runLater(() -> {
                if (btnSalvar != null) btnSalvar.setDisable(false);
                if (sucesso) {
                    exibirPopupEstilizado(Alert.AlertType.INFORMATION, "Sucesso", "Utensílio atualizado com sucesso!", true);
                } else {
                    exibirPopupEstilizado(Alert.AlertType.ERROR, "Erro", "Não foi possível atualizar as informações no banco de dados.", false);
                }
            });
        }, DB_EXECUTOR);
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
