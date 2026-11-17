package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.User;
import com.sigec.system.sigec.Constructors.Utensilio;
import com.sigec.system.sigec.DAOS.UnidadeDAO;
import com.sigec.system.sigec.DAOS.UtensilioDAO;
import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Services.SessaoService;
import com.sigec.system.sigec.Utils.ButtonBorderLapAnimator;
import com.sigec.system.sigec.Utils.DropdownBorderLapAnimator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UtensiliosController implements Initializable {

    private static final ExecutorService DB_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "SIGEC-Utensilios-Worker");
        t.setDaemon(true);
        return t;
    });

    @FXML private Label dataLabel;
    @FXML private Label horaLabel;
    @FXML private Label usuarioLabel;

    @FXML private VBox submenuCadastro;
    @FXML private Label setaCadastro;
    @FXML private Button btnNavCadastro;

    @FXML private TextField txtPesquisaUtensilio;
    @FXML private ChoiceBox<String> cbStatus;
    @FXML private ChoiceBox<String> cbLaboratorio;

    @FXML private TableView<Utensilio> tabelaUtensilios;
    @FXML private TableColumn<Utensilio, String> colItem;
    @FXML private TableColumn<Utensilio, Integer> colPatrimonio;
    @FXML private TableColumn<Utensilio, String> colLaboratorio;
    @FXML private TableColumn<Utensilio, String> colStatus;

    @FXML private VBox submenuEstoque;
    @FXML private Label setaEstoque;
    @FXML private Button btnNavEstoque;

    @FXML private ChoiceBox<String> cbUnidade;

    @FXML private Button btnAvaria;

    private ObservableList<Utensilio> observableListUtensilios;
    private boolean ignorarEventosFiltro = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        iniciarRelogio();

        if (SessaoService.getUsuarioAtual() != null) {
            usuarioLabel.setText(SessaoService.getUsuarioAtual().getNome());
        } else {
            usuarioLabel.setText("Administrador");
        }

        configurarColunas();
        observableListUtensilios = FXCollections.observableArrayList();
        tabelaUtensilios.setItems(observableListUtensilios);

        if (cbLaboratorio != null) {
            cbLaboratorio.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (!ignorarEventosFiltro && newValue != null) {
                    onFiltrarClick(new ActionEvent());
                }
            });
        }

        if (cbStatus != null) {
            cbStatus.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (!ignorarEventosFiltro && newValue != null) {
                    onFiltrarClick(new ActionEvent());
                }
            });
        }

        if (cbUnidade != null) {
            cbUnidade.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (!ignorarEventosFiltro && newValue != null) {
                    onFiltrarClick(new ActionEvent());
                }
            });
        }

        carregarOpcoesFiltros();

        if (cbUnidade != null) {
            User usuarioLogado = SessaoService.getUsuarioAtual();
            final int idUsuario = (usuarioLogado != null && usuarioLogado.getIdUsuario() > 0)
                    ? usuarioLogado.getIdUsuario()
                    : 0;

            CompletableFuture.supplyAsync(() ->
                    idUsuario > 0 ? UnidadeDAO.listarNomesUnidadesPorUsuario(idUsuario) : new ArrayList<String>(),
                    DB_EXECUTOR
            ).thenAccept(unidadesVinculadas -> Platform.runLater(() -> {
                ignorarEventosFiltro = true;
                try {
                    cbUnidade.setItems(FXCollections.observableArrayList(unidadesVinculadas));
                    if (!unidadesVinculadas.isEmpty()) {
                        cbUnidade.getSelectionModel().selectFirst();
                    }
                } finally {
                    ignorarEventosFiltro = false;
                }
                onFiltrarClick(new ActionEvent());
            }));
        } else {
            carregarDadosBanco();
        }
    }

    private void carregarOpcoesFiltros() {
        if (cbLaboratorio != null) {
            final String labSelecionado = cbLaboratorio.getValue();
            CompletableFuture.supplyAsync(UtensilioDAO::listarOpcoesLaboratorio, DB_EXECUTOR)
                    .thenAccept(labs -> Platform.runLater(() -> {
                        ignorarEventosFiltro = true;
                        try {
                            cbLaboratorio.setItems(FXCollections.observableArrayList(labs));
                            if (labSelecionado != null && labs.contains(labSelecionado)) {
                                cbLaboratorio.setValue(labSelecionado);
                            } else {
                                cbLaboratorio.setValue("Todos");
                            }
                        } finally {
                            ignorarEventosFiltro = false;
                        }
                    }));
        }

        if (cbStatus != null) {
            final String statusSelecionado = cbStatus.getValue();
            CompletableFuture.supplyAsync(UtensilioDAO::listarOpcoesSituacao, DB_EXECUTOR)
                    .thenAccept(situacoes -> Platform.runLater(() -> {
                        ignorarEventosFiltro = true;
                        try {
                            cbStatus.setItems(FXCollections.observableArrayList(situacoes));
                            if (statusSelecionado != null && situacoes.contains(statusSelecionado)) {
                                cbStatus.setValue(statusSelecionado);
                            } else {
                                cbStatus.setValue("Todos");
                            }
                        } finally {
                            ignorarEventosFiltro = false;
                        }
                    }));
        }
    }

    private void configurarColunas() {
        colItem.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colPatrimonio != null) {
            colPatrimonio.setCellValueFactory(new PropertyValueFactory<>("numeroPatrimonio"));
        }
        colLaboratorio.setCellValueFactory(new PropertyValueFactory<>("laboratorio"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void carregarDadosBanco() {
        CompletableFuture.supplyAsync(UtensilioDAO::listarUtensilios, DB_EXECUTOR)
                .thenAccept(utensiliosDb -> Platform.runLater(() -> {
                    if (observableListUtensilios == null) {
                        observableListUtensilios = FXCollections.observableArrayList(utensiliosDb);
                        tabelaUtensilios.setItems(observableListUtensilios);
                    } else {
                        observableListUtensilios.setAll(utensiliosDb);
                        tabelaUtensilios.refresh();
                    }
                }));
    }

    private void iniciarRelogio() {
        DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatadorHora = DateTimeFormatter.ofPattern("HH:mm:ss");

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            dataLabel.setText(LocalDate.now().format(formatadorData));
            horaLabel.setText(LocalTime.now().format(formatadorHora));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    @FXML
    public void onFiltrarClick(ActionEvent event) {
        final String termo = txtPesquisaUtensilio != null ? txtPesquisaUtensilio.getText() : "";
        final String status = cbStatus != null && cbStatus.getValue() != null ? cbStatus.getValue() : "Todos";
        final String laboratorio = cbLaboratorio != null && cbLaboratorio.getValue() != null ? cbLaboratorio.getValue() : "Todos";
        final String unidade = cbUnidade != null ? cbUnidade.getValue() : null;

        if (unidade == null || unidade.trim().isEmpty()) {
            if (observableListUtensilios == null) {
                observableListUtensilios = FXCollections.observableArrayList();
                tabelaUtensilios.setItems(observableListUtensilios);
            } else {
                observableListUtensilios.clear();
                tabelaUtensilios.refresh();
            }
            return;
        }

        CompletableFuture.supplyAsync(
                () -> UtensilioDAO.buscarUtensiliosPorFiltro(termo, status, laboratorio, unidade),
                DB_EXECUTOR
        ).thenAccept(utensiliosFiltrados -> Platform.runLater(() -> {
            if (observableListUtensilios == null) {
                observableListUtensilios = FXCollections.observableArrayList(utensiliosFiltrados);
                tabelaUtensilios.setItems(observableListUtensilios);
            } else {
                observableListUtensilios.setAll(utensiliosFiltrados);
                tabelaUtensilios.refresh();
            }
        }));
    }

    @FXML
    public void onRegistrarAvariaClick(ActionEvent event) {
        Utensilio utensilioSelecionado = tabelaUtensilios.getSelectionModel().getSelectedItem();

        if (utensilioSelecionado == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenção");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecione um utensílio na tabela para registrar a avaria.");
            alert.showAndWait();
            return;
        }

        try {
            RegistroAvariaUtensilioController.utensilioSelecionado = utensilioSelecionado;
            MainApplication.abrirPopUp("registroAvariaUtensilio.fxml");
            carregarOpcoesFiltros();
            onFiltrarClick(new ActionEvent());

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir a tela de registro de avaria: " + e.getMessage());
        }
    }

    @FXML
    public void onEditarClick(ActionEvent event) {
        Utensilio utensilioSelecionado = tabelaUtensilios.getSelectionModel().getSelectedItem();

        if (utensilioSelecionado == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atenção");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecione um utensílio na tabela para editar.");
            alert.showAndWait();
            return;
        }

        try {
            EdicaoUtensilioController.utensilioParaEditar = utensilioSelecionado;
            MainApplication.abrirPopUp("EdicaoUtensilio.fxml");
            carregarOpcoesFiltros();
            onFiltrarClick(new ActionEvent());

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir a tela de edição de utensílio: " + e.getMessage());
        }
    }

    @FXML
    public void onAdicionarClick(ActionEvent event) {
        try {
            MainApplication.abrirPopUp("CadastroUtensilio.fxml");
            carregarOpcoesFiltros();
            onFiltrarClick(new ActionEvent());

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir a tela de cadastro de utensílio: " + e.getMessage());
        }
    }

    @FXML
    public void onClickHome(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("homeTest.fxml"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void onClickRelatorio(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("historico.fxml"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void onClickSolicitacoes(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("solicitacoes-extras.fxml"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void botaoCalendarioAction(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("calendario.fxml"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void onClickCadastroUsuario(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("cadastro.fxml"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void onClickCadastroTurma(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("cadastro-turma.fxml"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void botaoSairAction(ActionEvent event) {
        com.sigec.system.sigec.Utils.SidebarNavigationManager.executarLogout();
    }

    @FXML
    public void toggleDropdownCadastro(ActionEvent event) {
        if (submenuCadastro != null) {
            boolean expandido = !submenuCadastro.isVisible();
            submenuCadastro.setVisible(expandido);
            submenuCadastro.setManaged(expandido);
            if (setaCadastro != null) {
                setaCadastro.setText(expandido ? "▲" : "▼");
            }
            if (expandido && btnNavCadastro != null) {
                ButtonBorderLapAnimator.animarDropdown(btnNavCadastro, submenuCadastro);
            } else if (!expandido && btnNavCadastro != null) {
                ButtonBorderLapAnimator.cancelarAnimacaoAtiva();
                btnNavCadastro.getStyleClass().remove("btn-dropdown-ativo");
                btnNavCadastro.setStyle(null);
                submenuCadastro.getStyleClass().remove("submenu-lateral-ativo");
                submenuCadastro.setStyle(null);
            }
        }
    }

    @FXML
    public void toggleDropdownEstoque(ActionEvent event) {
        if (submenuEstoque != null && btnNavEstoque != null) {
            boolean expandido = !submenuEstoque.isVisible();

            if (setaEstoque != null) {
                setaEstoque.setText(expandido ? "▲" : "▼");
            }

            if (expandido) {
                submenuEstoque.setVisible(true);
                submenuEstoque.setManaged(true);
                DropdownBorderLapAnimator.animarDropdown(btnNavEstoque, submenuEstoque);
            } else {
                ButtonBorderLapAnimator.cancelarAnimacaoAtiva();
                btnNavEstoque.getStyleClass().remove("btn-dropdown-ativo");
                btnNavEstoque.setStyle(null);
                submenuEstoque.getStyleClass().remove("submenu-lateral-ativo");
                submenuEstoque.setStyle(null);
                submenuEstoque.setVisible(false);
                submenuEstoque.setManaged(false);
            }
        }
    }

    @FXML
    public void onClickInsumos(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("lista-estoque.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao carregar a tela de Insumos: " + e.getMessage());
        }
    }

    @FXML
    public void onClickUtensilios(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("gestao-utensilios.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao carregar a tela de Utensílios: " + e.getMessage());
        }
    }
}
