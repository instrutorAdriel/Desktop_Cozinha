package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.AlertaEstoqueDTO;
import com.sigec.system.sigec.Constructors.UnidadeDTO;
import com.sigec.system.sigec.DAOS.HomeDAO;
import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Services.SessaoService;
import com.sigec.system.sigec.Utils.BackgroundAnimator;
import com.sigec.system.sigec.Utils.ButtonBorderLapAnimator;
import com.sigec.system.sigec.Utils.DropdownBorderLapAnimator;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador da tela Principal (Home/Dashboard) do SIGEC.
 */
public class HomeController implements Initializable {

    @FXML
    private AnchorPane topBarPane;

    @FXML
    private Label dataLabel;

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label horaLabel;

    @FXML
    private ImageView logoSenac;

    @FXML
    private Label logoSigec;

    // --- TABELA 1: VENCIDOS ---
    @FXML
    private TableView<AlertaEstoqueDTO> tabelaAlertas;
    @FXML
    private TableColumn<AlertaEstoqueDTO, String> colProduto;
    @FXML
    private TableColumn<AlertaEstoqueDTO, LocalDate> colValidade;
    @FXML
    private TableColumn<AlertaEstoqueDTO, String> colStatus; // Categoria

    // --- TABELA 2: PRÓXIMOS AO VENCIMENTO ---
    @FXML
    private TableView<AlertaEstoqueDTO> tabelaProximos;
    @FXML
    private TableColumn<AlertaEstoqueDTO, String> colProximosProduto;
    @FXML
    private TableColumn<AlertaEstoqueDTO, LocalDate> colProximosVencimento;
    @FXML
    private TableColumn<AlertaEstoqueDTO, String> colProximosLote;
    @FXML
    private TableColumn<AlertaEstoqueDTO, Integer> colProximosQtd;

    // --- TABELA 3: ESTOQUE BAIXO ---
    @FXML
    private TableView<AlertaEstoqueDTO> tabelaEstoqueBaixo;
    @FXML
    private TableColumn<AlertaEstoqueDTO, String> colBaixoItem;
    @FXML
    private TableColumn<AlertaEstoqueDTO, Integer> colBaixoQtd;
    @FXML
    private TableColumn<AlertaEstoqueDTO, Integer> colBaixoMin;

    // --- TABELA 4: ESTOQUE ZERADO ---
    @FXML
    private TableView<AlertaEstoqueDTO> tabelaEstoqueZerado;
    @FXML
    private TableColumn<AlertaEstoqueDTO, String> colZeradoItem;
    @FXML
    private TableColumn<AlertaEstoqueDTO, String> colZeradoCategoria;

    @FXML
    private Button btnNavCadastro;

    @FXML
    private VBox submenuCadastro;

    @FXML
    private Label setaCadastro;

    // --- VARIÁVEIS DO MENU DROPDOWN DE ESTOQUE ---
    @FXML
    private Button btnNavEstoque;

    @FXML
    private VBox submenuEstoque;

    @FXML
    private Label setaEstoque;
    // ----------------------------------------------
    @FXML
    private ChoiceBox<UnidadeDTO> cbUnidade;

    @FXML
    private Button btnNavCalendario;

    @FXML
    private Button botaoSair;

    private final HomeDAO homeDAO = new HomeDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (topBarPane != null) {
            BackgroundAnimator.startTopBarAnimation(topBarPane);
        }
        configurarDataHora();
        configurarUsuario();
        configurarTabelas();

        // Configuração do ChoiceBox Dinâmico de Unidades
        if (cbUnidade != null) {
            // 1. Busca as unidades ativas no banco de dados através da DAO
            List<UnidadeDTO> unidades = homeDAO.listarUnidades();
            cbUnidade.setItems(FXCollections.observableArrayList(unidades));

            // 2. Define o Conversor visual: exibe apenas o nome na tela, mas guarda o ID internamente
            cbUnidade.setConverter(new javafx.util.StringConverter<UnidadeDTO>() {
                @Override
                public String toString(UnidadeDTO unidade) {
                    return unidade != null ? unidade.getNome() : "";
                }

                @Override
                public UnidadeDTO fromString(String string) {
                    return null; // Operação reversa não é usada no ChoiceBox
                }
            });

            // 3. Listener: Sempre que a seleção mudar, buscar os dados daquela unidade
            cbUnidade.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    carregarDadosDashboard(newValue.getId());
                }
            });

            // 4. Marca o primeiro item automaticamente (dispara o listener na inicialização)
            if (!unidades.isEmpty()) {
                cbUnidade.getSelectionModel().selectFirst();
            }
        }
    }

    private void configurarUsuario() {
        if (usuarioLabel != null) {
            usuarioLabel.setText(SessaoService.getNomeUsuarioLogado());
        }
    }

    private void configurarDataHora() {
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        if (dataLabel != null) {
            dataLabel.setText(LocalDate.now().format(dataFormatter));
        }

        if (horaLabel != null) {
            horaLabel.setText(LocalTime.now().format(horaFormatter));
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event ->
                    horaLabel.setText(LocalTime.now().format(horaFormatter))
            ));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
    }

    private void configurarTabelas() {
        // Tabela Vencidos
        if (tabelaAlertas != null && colProduto != null) {
            colProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
            colValidade.setCellValueFactory(new PropertyValueFactory<>("dataValidade"));
            colStatus.setCellValueFactory(new PropertyValueFactory<>("nomeCategoria"));
            formatarColunaData(colValidade);
        }

        // Tabela Próximos
        if (tabelaProximos != null && colProximosProduto != null) {
            colProximosProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
            colProximosVencimento.setCellValueFactory(new PropertyValueFactory<>("dataValidade"));
            colProximosLote.setCellValueFactory(new PropertyValueFactory<>("lote"));
            colProximosQtd.setCellValueFactory(new PropertyValueFactory<>("qtdAtual"));
            formatarColunaData(colProximosVencimento);
        }

        // Tabela Estoque Baixo
        if (tabelaEstoqueBaixo != null && colBaixoItem != null) {
            colBaixoItem.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
            colBaixoQtd.setCellValueFactory(new PropertyValueFactory<>("qtdAtual"));
            colBaixoMin.setCellValueFactory(new PropertyValueFactory<>("qtdMinima"));
        }

        // Tabela Estoque Zerado
        if (tabelaEstoqueZerado != null && colZeradoItem != null) {
            colZeradoItem.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
            colZeradoCategoria.setCellValueFactory(new PropertyValueFactory<>("nomeCategoria"));
        }
    }

    private void carregarDadosDashboard(int idUnidade) {
        if (tabelaAlertas != null) {
            tabelaAlertas.setItems(FXCollections.observableArrayList(homeDAO.listarProdutosVencidos(idUnidade)));
        }
        if (tabelaProximos != null) {
            tabelaProximos.setItems(FXCollections.observableArrayList(homeDAO.listarProximosVencimento(idUnidade)));
        }
        if (tabelaEstoqueBaixo != null) {
            tabelaEstoqueBaixo.setItems(FXCollections.observableArrayList(homeDAO.listarEstoqueBaixo(idUnidade)));
        }
        if (tabelaEstoqueZerado != null) {
            tabelaEstoqueZerado.setItems(FXCollections.observableArrayList(homeDAO.listarEstoqueZerado(idUnidade)));
        }
    }

    private void formatarColunaData(TableColumn<AlertaEstoqueDTO, LocalDate> coluna) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        coluna.setCellFactory(column -> new TableCell<AlertaEstoqueDTO, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });
    }

    public void prepararAnimacao() {
        if (logoSenac != null && logoSigec != null) {
            logoSenac.setOpacity(0);
            logoSigec.setOpacity(0);
        }
    }

    public void iniciarAnimacaoLogos() {
        if (logoSenac != null && logoSigec != null) {
            FadeTransition ft1 = new FadeTransition(Duration.millis(800), logoSenac);
            ft1.setToValue(1);

            FadeTransition ft2 = new FadeTransition(Duration.millis(800), logoSigec);
            ft2.setToValue(1);

            ft1.play();
            ft2.play();
        }
    }

    // =========================================================================
    // NAVEGAÇÃO
    // =========================================================================

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
            exibirErroNavegacao("Insumos", e);
        }
    }

    @FXML
    public void onClickUtensilios(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("gestao-utensilios.fxml");
        } catch (IOException e) {
            exibirErroNavegacao("Utensílios", e);
        }
    }


    @FXML
    public void botaoEstoqueAction(ActionEvent event) {
        toggleDropdownEstoque(event);
    }

    @FXML
    public void onClickEstoque(ActionEvent event) {
        toggleDropdownEstoque(event);
    }

    // DEMAIS BOTÕES
    @FXML
    public void onClickSolicitacoes(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("solicitacoes-extras.fxml");
        } catch (IOException e) {
            exibirErroNavegacao("Solicitações", e);
        }
    }

    @FXML
    public void onClickRelatorio(ActionEvent event) {
        navegarParaRelatorio();
    }

    private void navegarParaRelatorio() {
        try {
            MainApplication.trocadorDeTelas("historico.fxml");
        } catch (IOException e) {
            exibirErroNavegacao("Relatório", e);
        }
    }

    @FXML
    public void onClickCadastroUsuario(ActionEvent event) {
        navegarParaCadastro();
    }

    @FXML
    public void onClickCadastroTurma(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("cadastro-turma.fxml");
        } catch (IOException e) {
            exibirErroNavegacao("Cadastro de Turma", e);
        }
    }

    @FXML
    public void onClickCadastro(ActionEvent event) {
        toggleDropdownCadastro(event);
    }

    @FXML
    public void botaoCadastroAction(ActionEvent event) {
        toggleDropdownCadastro(event);
    }

    private void navegarParaCadastro() {
        try {
            MainApplication.trocadorDeTelas("cadastro.fxml");
        } catch (IOException e) {
            exibirErroNavegacao("Cadastro de Usuário", e);
        }
    }

    @FXML
    public void botaoCalendarioAction(ActionEvent event) {
        navegarParaCalendario();
    }

    @FXML
    public void onClickCalendario(ActionEvent event) {
        navegarParaCalendario();
    }

    private void navegarParaCalendario() {
        try {
            MainApplication.trocadorDeTelas("calendario.fxml");
        } catch (IOException e) {
            exibirErroNavegacao("Calendário", e);
        }
    }

    @FXML
    public void botaoSairAction(ActionEvent event) {
        try {
            SessaoService.encerrarSessao();
            MainApplication.trocadorDeTelas("login.fxml");
        } catch (IOException e) {
            exibirErroNavegacao("Login", e);
        }
    }

    private void exibirErroNavegacao(String destino, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de Navegação");
        alert.setHeaderText(null);
        alert.setContentText("Não foi possível carregar a tela de " + destino + ": " + e.getMessage());
        alert.showAndWait();
    }
}