package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.User;
import com.sigec.system.sigec.Constructors.Utensilio;
import com.sigec.system.sigec.DAOS.CategoriaUtensilioDAO;
import com.sigec.system.sigec.DAOS.LaboratorioDAO;
import com.sigec.system.sigec.DAOS.UnidadeDAO;
import com.sigec.system.sigec.DAOS.UtensilioDAO;
import com.sigec.system.sigec.Services.SessaoService;
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

public class CadastroUtensilioController implements Initializable {

    private static final ExecutorService DB_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "SIGEC-CadastroUtensilio-Worker");
        t.setDaemon(true);
        return t;
    });

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
    private boolean ignorarEventoUnidade = false;

    private Map<String, Integer> mapaUnidades = new LinkedHashMap<>();
    private Map<String, Integer> mapaCategorias = new LinkedHashMap<>();
    private Map<String, Integer> mapaLaboratorios = new LinkedHashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cbUnidade != null) {
            cbUnidade.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (!ignorarEventoUnidade) {
                    carregarLaboratoriosPorUnidadeAsync(newValue);
                }
            });
        }

        carregarDadosIniciaisEmParalelo();
    }

    private void carregarDadosIniciaisEmParalelo() {
        User usuarioLogado = SessaoService.getUsuarioAtual();
        final int idUsuario = (usuarioLogado != null && usuarioLogado.getIdUsuario() > 0)
                ? usuarioLogado.getIdUsuario()
                : 0;

        // Thread 1: Carrega unidades do usuário e já busca os laboratórios da primeira unidade
        CompletableFuture.runAsync(() -> {
            Map<String, Integer> unidadesCarregadas = (idUsuario > 0)
                    ? UnidadeDAO.listarUnidadesPorUsuarioMap(idUsuario)
                    : new LinkedHashMap<>();

            String primeiraUnidade = unidadesCarregadas.isEmpty()
                    ? null
                    : unidadesCarregadas.keySet().iterator().next();
            int idPrimeiraUnidade = (primeiraUnidade != null)
                    ? unidadesCarregadas.getOrDefault(primeiraUnidade, 0)
                    : 0;

            Map<String, Integer> labsIniciais = (idPrimeiraUnidade > 0)
                    ? LaboratorioDAO.listarLaboratoriosAtivosPorUnidadeMap(idPrimeiraUnidade)
                    : new LinkedHashMap<>();

            Platform.runLater(() -> {
                mapaUnidades = unidadesCarregadas;
                mapaLaboratorios = labsIniciais;

                if (cbUnidade != null) {
                    ignorarEventoUnidade = true;
                    try {
                        cbUnidade.setItems(FXCollections.observableArrayList(mapaUnidades.keySet()));
                        if (primeiraUnidade != null) {
                            cbUnidade.setValue(primeiraUnidade);
                        }
                    } finally {
                        ignorarEventoUnidade = false;
                    }
                }

                if (cbLaboratorio != null) {
                    cbLaboratorio.setItems(FXCollections.observableArrayList(mapaLaboratorios.keySet()));
                    if (!mapaLaboratorios.isEmpty()) {
                        cbLaboratorio.setValue(mapaLaboratorios.keySet().iterator().next());
                    } else {
                        cbLaboratorio.setValue(null);
                    }
                }
            });
        }, DB_EXECUTOR);

        // Thread 2 (em paralelo): Carrega categorias ativas
        CompletableFuture.supplyAsync(CategoriaUtensilioDAO::listarCategoriasAtivas, DB_EXECUTOR)
                .thenAccept(categoriasCarregadas -> Platform.runLater(() -> {
                    mapaCategorias = categoriasCarregadas != null ? categoriasCarregadas : new LinkedHashMap<>();
                    if (cbCategoria != null) {
                        cbCategoria.setItems(FXCollections.observableArrayList(mapaCategorias.keySet()));
                        if (!mapaCategorias.isEmpty()) {
                            cbCategoria.setValue(mapaCategorias.keySet().iterator().next());
                        }
                    }
                }));
    }

    private void carregarLaboratoriosPorUnidadeAsync(String nomeUnidade) {
        if (cbLaboratorio == null) {
            return;
        }

        int idUnidade = (nomeUnidade != null) ? mapaUnidades.getOrDefault(nomeUnidade, 0) : 0;
        if (idUnidade <= 0) {
            mapaLaboratorios = new LinkedHashMap<>();
            cbLaboratorio.setItems(FXCollections.observableArrayList());
            cbLaboratorio.setValue(null);
            return;
        }

        CompletableFuture.supplyAsync(() -> LaboratorioDAO.listarLaboratoriosAtivosPorUnidadeMap(idUnidade), DB_EXECUTOR)
                .thenAccept(labs -> Platform.runLater(() -> {
                    mapaLaboratorios = labs != null ? labs : new LinkedHashMap<>();
                    cbLaboratorio.setItems(FXCollections.observableArrayList(mapaLaboratorios.keySet()));
                    if (!mapaLaboratorios.isEmpty()) {
                        cbLaboratorio.setValue(mapaLaboratorios.keySet().iterator().next());
                    } else {
                        cbLaboratorio.setValue(null);
                    }
                }));
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

        final int qtd;
        final int patrimonio;
        try {
            int qtdTemp = 1;
            if (txtQuantidade != null && txtQuantidade.getText() != null && !txtQuantidade.getText().trim().isEmpty()) {
                qtdTemp = Integer.parseInt(txtQuantidade.getText().trim());
            }
            qtd = qtdTemp;
            patrimonio = Integer.parseInt(strPatrimonio.trim());
        } catch (NumberFormatException e) {
            exibirPopupEstilizado(Alert.AlertType.ERROR, "Erro", "O Nº do Patrimônio deve ser um número válido.", false);
            return;
        }

        int idUnidade = mapaUnidades.getOrDefault(nomeUnidade, 0);
        Integer idCategoria = mapaCategorias.get(nomeCategoria);
        Integer idLaboratorio = mapaLaboratorios.get(nomeLaboratorio);

        if (idCategoria == null || idLaboratorio == null) {
            exibirPopupEstilizado(Alert.AlertType.WARNING, "Atenção", "Aguarde o carregamento das opções antes de salvar.", false);
            return;
        }

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

        if (btnSalvar != null) {
            btnSalvar.setDisable(true);
        }

        CompletableFuture.runAsync(() -> {
            if (UtensilioDAO.existeNumeroPatrimonio(patrimonio)) {
                Platform.runLater(() -> {
                    if (btnSalvar != null) btnSalvar.setDisable(false);
                    exibirPopupEstilizado(
                            Alert.AlertType.ERROR,
                            "Erro no Cadastro",
                            "Nao foi possivel cadastrar o produto ERROR 890: \nNumero de patrimonio ja existe!",
                            false
                    );
                });
                return;
            }

            boolean sucesso = UtensilioDAO.adicionarUtensilio(novoUtensilio);
            Platform.runLater(() -> {
                if (btnSalvar != null) btnSalvar.setDisable(false);
                if (sucesso) {
                    exibirPopupEstilizado(Alert.AlertType.INFORMATION, "Sucesso", "Utensílio adicionado com sucesso!", true);
                } else {
                    exibirPopupEstilizado(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar as informações na base de dados.", false);
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
