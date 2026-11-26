package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.ListaEstoque;
import com.sigec.system.sigec.DAOS.ProductStorageDAO;
import com.sigec.system.sigec.DAOS.RelatorioInsumosDAO;
import com.sigec.system.sigec.DAOS.RelatorioInsumosDAO.Opcao;
import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Services.SessaoService;
import com.sigec.system.sigec.Utils.BackgroundAnimator;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListaController implements Initializable {

    private static final Opcao TODAS_UNIDADES = new Opcao(0, "Todas");
    private static final Opcao TODOS_CURSOS = new Opcao(0, "Todos");

    private final RelatorioInsumosDAO relatorioDAO = new RelatorioInsumosDAO();
    private boolean atualizandoFiltros = false;

    @FXML
    private AnchorPane topBarPane;

    @FXML
    private Label dataLabel;

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label horaLabel;

    @FXML
    private TextField FiltrarProdutos;

    @FXML
    private ChoiceBox<Opcao> cbUnidade;

    @FXML
    private ChoiceBox<Opcao> cbCurso;

    @FXML
    private CheckBox chkPereciveis;

    @FXML
    private CheckBox chkNaoPereciveis;

    @FXML
    private CheckBox chkUtensilios;

    @FXML
    private TableView<ListaEstoque> ListaEstoque;

    @FXML
    private TableColumn<ListaEstoque, String> nomeProduto;

    @FXML
    private TableColumn<ListaEstoque, String> loteProduto;

    @FXML
    private TableColumn<ListaEstoque, String> tipoProduto;

    @FXML
    private TableColumn<ListaEstoque, Double> quantidadeProduto;

    @FXML
    private TableColumn<ListaEstoque, String> unidadeProduto;

    @FXML
    private Button editar;

    @FXML
    private Button remover;

    @FXML
    private Button btnNavCadastro;

    @FXML
    private VBox submenuCadastro;

    @FXML
    private Label setaCadastro;

    @FXML
    private VBox submenuEstoque;

    @FXML
    private Label setaEstoque;

    @FXML
    private Button btnNavEstoque;

    @FXML
    private Button botaoSair;

    private ObservableList<ListaEstoque> obsLista;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (topBarPane != null) {
            BackgroundAnimator.startTopBarAnimation(topBarPane);
        }
        configurarDataHora();
        configurarUsuario();
        configurarTabela();
        configurarFiltros();

        onPesquisarClick(new ActionEvent());
    }

    private void configurarTabela() {
        obsLista = FXCollections.observableArrayList();
        ListaEstoque.setItems(obsLista);

        nomeProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        loteProduto.setCellValueFactory(new PropertyValueFactory<>("lote"));
        tipoProduto.setCellValueFactory(new PropertyValueFactory<>("tipoProduto"));
        quantidadeProduto.setCellValueFactory(new PropertyValueFactory<>("quantidadeProduto"));
        unidadeProduto.setCellValueFactory(new PropertyValueFactory<>("unidadeProduto"));

        // Faz as colunas preencherem 100% da largura da tabela
        ListaEstoque.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        nomeProduto.prefWidthProperty().bind(ListaEstoque.widthProperty().multiply(0.34));
        loteProduto.prefWidthProperty().bind(ListaEstoque.widthProperty().multiply(0.18));
        tipoProduto.prefWidthProperty().bind(ListaEstoque.widthProperty().multiply(0.24));
        quantidadeProduto.prefWidthProperty().bind(ListaEstoque.widthProperty().multiply(0.14));
        unidadeProduto.prefWidthProperty().bind(ListaEstoque.widthProperty().multiply(0.10));

        // Comparador em português (ordena A-Z corretamente mesmo com acentos como "Açúcar")
        java.text.Collator collator = java.text.Collator.getInstance(java.util.Locale.of("pt", "BR"));
        collator.setStrength(java.text.Collator.PRIMARY);
        java.util.Comparator<String> comparadorTexto = (a, b) -> {
            if (a == null && b == null) return 0;
            if (a == null) return -1;
            if (b == null) return 1;
            return collator.compare(a, b);
        };

        nomeProduto.setComparator(comparadorTexto);
        loteProduto.setComparator(comparadorTexto);
        tipoProduto.setComparator(comparadorTexto);
        unidadeProduto.setComparator(comparadorTexto);

        // Permite ordenar ao clicar no cabeçalho, mas mantém largura e posição fixas
        ListaEstoque.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        for (TableColumn<ListaEstoque, ?> col : ListaEstoque.getColumns()) {
            col.setSortable(true);
            col.setResizable(false);
            col.setReorderable(false);
        }

        // Já inicia ordenado por Produtos de A-Z ao carregar a tela
        nomeProduto.setSortType(TableColumn.SortType.ASCENDING);
        ListaEstoque.getSortOrder().setAll(nomeProduto);


    }

    private void configurarFiltros() {
        if (cbCurso != null) {
            cbCurso.setDisable(true);
            configurarTooltip(cbCurso);
            cbCurso.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
                if (!atualizandoFiltros && novo != null && !cbCurso.isDisabled()) {
                    onPesquisarClick(new ActionEvent());
                }
            });
        }

        if (cbUnidade != null) {
            configurarTooltip(cbUnidade);
            carregarUnidades();

            cbUnidade.getSelectionModel().selectedItemProperty().addListener((obs, antiga, nova) -> {
                onUnidadeSelecionada(nova);
                if (!atualizandoFiltros) {
                    onPesquisarClick(new ActionEvent());
                }
            });
        }
    }

    private void configurarTooltip(ChoiceBox<Opcao> choiceBox) {
        choiceBox.valueProperty().addListener((obs, antigo, novo) ->
                choiceBox.setTooltip(novo == null ? null : new Tooltip(novo.nome())));
    }

    private void carregarUnidades() {
        atualizandoFiltros = true;
        try {
            List<Opcao> unidades = new ArrayList<>();
            unidades.add(TODAS_UNIDADES);
            unidades.addAll(relatorioDAO.listarUnidades());

            cbUnidade.setItems(FXCollections.observableArrayList(unidades));
            cbUnidade.getSelectionModel().select(TODAS_UNIDADES);
            onUnidadeSelecionada(TODAS_UNIDADES);
        } catch (SQLException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar as unidades: " + e.getMessage());
        } finally {
            atualizandoFiltros = false;
        }
    }

    private void onUnidadeSelecionada(Opcao unidade) {
        if (cbCurso == null) {
            return;
        }

        boolean estadoAnterior = atualizandoFiltros;
        atualizandoFiltros = true;
        try {
            cbCurso.getItems().clear();
            cbCurso.getSelectionModel().clearSelection();

            if (unidade == null || unidade.id() == 0) {
                cbCurso.setDisable(true);
                return;
            }

            List<Opcao> cursos = new ArrayList<>();
            cursos.add(TODOS_CURSOS);
            cursos.addAll(relatorioDAO.listarCursosPorUnidade(unidade.id()));

            cbCurso.setItems(FXCollections.observableArrayList(cursos));
            cbCurso.setDisable(false);
            cbCurso.getSelectionModel().select(TODOS_CURSOS);
        } catch (SQLException e) {
            cbCurso.setDisable(true);
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar os cursos da unidade: " + e.getMessage());
        } finally {
            atualizandoFiltros = estadoAnterior;
        }
    }

    @FXML
    public void onPesquisarClick(ActionEvent event) {
        String termoBusca = FiltrarProdutos != null ? FiltrarProdutos.getText() : "";
        String unidade = (cbUnidade != null && cbUnidade.getValue() != null)
                ? cbUnidade.getValue().nome()
                : TODAS_UNIDADES.nome();
        String curso = (cbCurso != null && !cbCurso.isDisabled() && cbCurso.getValue() != null)
                ? cbCurso.getValue().nome()
                : TODOS_CURSOS.nome();

        ProductStorageDAO dao = new ProductStorageDAO();
        List<ListaEstoque> produtosFiltrados = dao.buscarProdutosPorFiltro(termoBusca, unidade, curso);

        obsLista.setAll(produtosFiltrados);
        ListaEstoque.refresh();
    }

    private void configurarUsuario() {
        if (usuarioLabel != null) {
            if (SessaoService.getUsuarioAtual() != null) {
                usuarioLabel.setText(SessaoService.getUsuarioAtual().getNome());
            } else {
                usuarioLabel.setText("Administrador");
            }
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

    // =========================================================================
    // NAVEGAÇÃO
    // =========================================================================

    @FXML
    public void onClickHome(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("home.fxml");
        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao navegar para a tela inicial: " + e.getMessage());
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
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao navegar para a tela de relatórios: " + e.getMessage());
        }
    }

    @FXML
    protected void botaoCalendarioAction(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("calendario.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickSolicitacoes(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("solicitacoes-extras.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void toggleDropdownCadastro(ActionEvent event) {
        if (submenuCadastro != null) {
            boolean expandido = !submenuCadastro.isVisible();
            submenuCadastro.setVisible(expandido);
            submenuCadastro.setManaged(expandido);
            if (setaCadastro != null) {
                setaCadastro.setText(expandido ? "▶" : "▼");
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

    @FXML
    public void onClickCadastroUsuario(ActionEvent event) {
        navegarParaCadastro();
    }

    @FXML
    public void onClickCadastroTurma(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("cadastro-turma.fxml");
        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a tela de Cadastro de Turma: " + e.getMessage());
        }
    }

    private void navegarParaCadastro() {
        try {
            MainApplication.trocadorDeTelas("cadastro.fxml");
        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao navegar para a tela de cadastro: " + e.getMessage());
        }
    }

    @FXML
    public void botaoSairAction(ActionEvent event) {
        com.sigec.system.sigec.Utils.SidebarNavigationManager.executarLogout();
    }

    // =========================================================================
    // AÇÕES DO ESTOQUE
    // =========================================================================

    public static ListaEstoque produtoSelecionadoParaEdicao;

    @FXML
    public void onClickAddProduto(ActionEvent event) {
        try {
            MainApplication.abrirPopUp("CadastroProduto.fxml");
            onPesquisarClick(null);
        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao abrir modal de cadastro de produto: " + e.getMessage());
        }
    }

    @FXML
    public void onEditarProduto(ActionEvent event) {
        ListaEstoque selecionado = ListaEstoque.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione um produto na lista para editar.");
            return;
        }
        produtoSelecionadoParaEdicao = selecionado;

        try {
            MainApplication.abrirPopUp("edicaoProdutos.fxml");
            onPesquisarClick(null);
        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao abrir modal de edição de produto: " + e.getMessage());
        }
    }

    @FXML
    public void onRemoverProdutoClick(ActionEvent event) {
        ListaEstoque selecionado = ListaEstoque.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Por favor, selecione um produto na lista para excluir.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Exclusão de Produto");
        alert.setContentText("Deseja realmente remover o insumo \"" + selecionado.getNomeProduto() + "\" (Lote: " + selecionado.getLote() + ")?");
        Optional<ButtonType> resultado = alert.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            ProductStorageDAO dao = new ProductStorageDAO();
            boolean sucesso = dao.excluirInsumo(selecionado.getIdInsumo(), selecionado.getIdProduto());
            if (sucesso) {
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto removido com sucesso!");
                onPesquisarClick(null);
            } else {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Ocorreu um problema ao excluir o produto do banco.");
            }
        }
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}