package com.sigec.system.sigec.Controllers;

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
import java.util.List;
import java.util.ResourceBundle;

public class UtensiliosController implements Initializable {

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
    @FXML private TableColumn<Utensilio, Integer> colQtdTotal;
    @FXML private TableColumn<Utensilio, Integer> colQtdDisponivel;

    @FXML private VBox submenuEstoque;
    @FXML private Label setaEstoque;
    @FXML private Button btnNavEstoque;

    @FXML private ChoiceBox<String> cbUnidade;
    // cbCurso foi removido

    @FXML private Button btnAvaria;

    private ObservableList<Utensilio> observableListUtensilios;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        iniciarRelogio();

        if (SessaoService.getUsuarioAtual() != null) {
            usuarioLabel.setText(SessaoService.getUsuarioAtual().getNome());
        } else {
            usuarioLabel.setText("Administrador");
        }

        configurarColunas();
        carregarDadosBanco();
        carregarOpcoesFiltros();

        if (cbLaboratorio != null) {
            cbLaboratorio.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    onFiltrarClick(new ActionEvent());
                }
            });
        }

        if (cbStatus != null) {
            cbStatus.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    onFiltrarClick(new ActionEvent());
                }
            });
        }

        if (cbUnidade != null) {
            cbUnidade.setItems(FXCollections.observableArrayList(UnidadeDAO.listarNomesUnidades()));
            cbUnidade.setValue("Todas");

            cbUnidade.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    onFiltrarClick(new ActionEvent());
                }
            });
        }
    }

    private void carregarOpcoesFiltros() {
        if (cbLaboratorio != null) {
            String labSelecionado = cbLaboratorio.getValue();
            List<String> labs = UtensilioDAO.listarOpcoesLaboratorio();
            cbLaboratorio.setItems(FXCollections.observableArrayList(labs));
            if (labSelecionado != null && labs.contains(labSelecionado)) {
                cbLaboratorio.setValue(labSelecionado);
            } else {
                cbLaboratorio.setValue("Todos");
            }
        }

        if (cbStatus != null) {
            String statusSelecionado = cbStatus.getValue();
            List<String> situacoes = UtensilioDAO.listarOpcoesSituacao();
            cbStatus.setItems(FXCollections.observableArrayList(situacoes));
            if (statusSelecionado != null && situacoes.contains(statusSelecionado)) {
                cbStatus.setValue(statusSelecionado);
            } else {
                cbStatus.setValue("Todos");
            }
        }
    }

    private void configurarColunas() {
        colItem.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colPatrimonio != null) {
            colPatrimonio.setCellValueFactory(new PropertyValueFactory<>("numeroPatrimonio"));
        }
        colLaboratorio.setCellValueFactory(new PropertyValueFactory<>("laboratorio"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colQtdTotal.setCellValueFactory(new PropertyValueFactory<>("qtdTotal"));
        colQtdDisponivel.setCellValueFactory(new PropertyValueFactory<>("qtdDisponivel"));
    }

    private void carregarDadosBanco() {
        List<Utensilio> utensiliosDb = UtensilioDAO.listarUtensilios();
        observableListUtensilios = FXCollections.observableArrayList(utensiliosDb);
        tabelaUtensilios.setItems(observableListUtensilios);
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
        String termo = txtPesquisaUtensilio != null ? txtPesquisaUtensilio.getText() : "";
        String status = cbStatus != null ? cbStatus.getValue() : "Todos";
        String laboratorio = cbLaboratorio != null ? cbLaboratorio.getValue() : "Todos";
        String unidade = cbUnidade != null ? cbUnidade.getValue() : "Todas";

        List<Utensilio> utensiliosFiltrados = UtensilioDAO.buscarUtensiliosPorFiltro(termo, status, laboratorio, unidade);

        if (observableListUtensilios == null) {
            observableListUtensilios = FXCollections.observableArrayList(utensiliosFiltrados);
            tabelaUtensilios.setItems(observableListUtensilios);
        } else {
            observableListUtensilios.setAll(utensiliosFiltrados);
            tabelaUtensilios.refresh();
        }
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
