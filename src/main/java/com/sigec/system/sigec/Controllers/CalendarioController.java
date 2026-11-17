package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.DAOS.RelatorioInsumosDAO;
import com.sigec.system.sigec.DAOS.RelatorioInsumosDAO.InsumoNecessario;
import com.sigec.system.sigec.DAOS.RelatorioInsumosDAO.Opcao;
import com.sigec.system.sigec.Constructors.User;
import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Services.SessaoService;
import com.sigec.system.sigec.Utils.ButtonBorderLapAnimator;
import com.sigec.system.sigec.Utils.DropdownBorderLapAnimator;
import com.sigec.system.sigec.Utils.ScreenTransitionManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarioController {

    // Opções "coringa" dos filtros (id 0 = sem filtro)
    private static final Opcao TODOS_CURSOS = new Opcao(0, "Todos");
    private static final Opcao TODAS_TURMAS = new Opcao(0, "Todas");

    private static final String MSG_INICIAL =
            "Selecione a unidade e o período e clique em \"Gerar Relatório de Insumos\".";

    private final RelatorioInsumosDAO relatorioDAO = new RelatorioInsumosDAO();

    /** Nome do usuário logado . */
    private String nomeUsuario = "Usuário";

    @FXML private Label dataLabel;
    @FXML private Label horaLabel;
    @FXML private Label usuarioLabel;

    @FXML private VBox submenuEstoque;
    @FXML private Label setaEstoque;
    @FXML private Button btnNavEstoque;

    @FXML private Button btnNavCadastro;
    @FXML private VBox submenuCadastro;
    @FXML private Label setaCadastro;

    @FXML private TableView<InsumoNecessario> tabelaInsumos;
    @FXML private TableColumn<InsumoNecessario, String> colCategoria;
    @FXML private TableColumn<InsumoNecessario, String> colInsumo;
    @FXML private TableColumn<InsumoNecessario, Double> colQuantidadeNecessaria;
    @FXML private TableColumn<InsumoNecessario, String> colUnidade;

    @FXML private ChoiceBox<Opcao> cbUnidade;
    @FXML private ChoiceBox<Opcao> cbCurso;
    @FXML private ChoiceBox<Opcao> cbTurmaLab;

    @FXML private DatePicker dataInicio;
    @FXML private DatePicker dataFim;

    @FXML
    public void initialize() {
        iniciarRelogio();
        carregarUsuarioLogado();
        configurarTabela();
        configurarFiltros();
    }

    // ==================================================================
    // Inicialização
    // ==================================================================
    private void iniciarRelogio() {
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (dataLabel != null) {
            dataLabel.setText(LocalDate.now().format(dataFormatter));
        }
        if (horaLabel != null) {
            DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            Timeline clock = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> horaLabel.setText(LocalTime.now().format(horaFormatter))),
                    new KeyFrame(Duration.seconds(1)));
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();
        }
    }

    private void carregarUsuarioLogado() {
        User usuario = SessaoService.getUsuarioLogado();

        if (usuario != null && usuario.getNome() != null && !usuario.getNome().isBlank()) {
            nomeUsuario = usuario.getNome().trim();
        } else if (SessaoService.getIdUsuarioLogado() > 0) {

            try {
                String nome = relatorioDAO.buscarNomeUsuario(SessaoService.getIdUsuarioLogado());
                nomeUsuario = (nome != null && !nome.isBlank()) ? nome : SessaoService.getNomeExibicao();
            } catch (SQLException e) {
                System.err.println("Não foi possível carregar o nome do usuário: " + e.getMessage());
                nomeUsuario = SessaoService.getNomeExibicao();
            }
        } else {
            nomeUsuario = SessaoService.getNomeExibicao(); // sem sessão: "Administrador"
        }

        if (usuarioLabel != null) {
            usuarioLabel.setText(nomeUsuario);
        }
    }

    private void configurarTabela() {

        colCategoria.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().categoria()));
        colInsumo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nomeProduto()));
        colQuantidadeNecessaria.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().quantidade()));
        colUnidade.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().unidade()));


        // Com CONSTRAINED_RESIZE_POLICY, o maxWidth funciona como "peso" de cada coluna.
        colCategoria.setMaxWidth(1f * Integer.MAX_VALUE * 20);
        colInsumo.setMaxWidth(1f * Integer.MAX_VALUE * 45);
        colQuantidadeNecessaria.setMaxWidth(1f * Integer.MAX_VALUE * 20);
        colUnidade.setMaxWidth(1f * Integer.MAX_VALUE * 15);



        // Exibe 1.200 em vez de 1200.0 e 0,5 em vez de 0.5
        DecimalFormat formato = new DecimalFormat("#,##0.###",
                DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR")));
        colQuantidadeNecessaria.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean vazio) {
                super.updateItem(valor, vazio);
                setText(vazio || valor == null ? null : formato.format(valor));
            }
        });

        tabelaInsumos.setPlaceholder(new Label(MSG_INICIAL));
    }

    private void configurarFiltros() {
        cbCurso.setDisable(true);
        cbTurmaLab.setDisable(true);

        // Mostra o nome completo ao passar o mouse (nomes longos de unidade/turma)
        configurarTooltip(cbUnidade);
        configurarTooltip(cbCurso);
        configurarTooltip(cbTurmaLab);

        carregarUnidades();

        cbUnidade.getSelectionModel().selectedItemProperty()
                .addListener((obs, antiga, nova) -> onUnidadeSelecionada(nova));
        cbCurso.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> onCursoSelecionado(novo));

        // Qualquer mudança de filtro invalida o resultado que está na tela
        cbTurmaLab.getSelectionModel().selectedItemProperty().addListener((obs, a, n) -> limparTabela());
        dataInicio.valueProperty().addListener((obs, a, n) -> limparTabela());
        dataFim.valueProperty().addListener((obs, a, n) -> limparTabela());
    }

    private void configurarTooltip(ChoiceBox<Opcao> choiceBox) {
        choiceBox.valueProperty().addListener((obs, antigo, novo) ->
                choiceBox.setTooltip(novo == null ? null : new Tooltip(novo.nome())));
    }


    // Filtros em cascata: Unidade -> Curso -> Turma

    private void carregarUnidades() {
        try {
            cbUnidade.setItems(FXCollections.observableArrayList(relatorioDAO.listarUnidades()));
            // Se o usuário só tem uma unidade, já deixa selecionada
            if (cbUnidade.getItems().size() == 1) {
                cbUnidade.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            mostrarErro("Não foi possível carregar as unidades.", e);
        }
    }

    private void onUnidadeSelecionada(Opcao unidade) {
        limparTabela();
        cbCurso.getItems().clear();
        cbTurmaLab.getItems().clear();
        cbTurmaLab.setDisable(true);

        if (unidade == null) {
            cbCurso.setDisable(true);
            return;
        }

        try {
            List<Opcao> cursos = new ArrayList<>();
            cursos.add(TODOS_CURSOS);
            cursos.addAll(relatorioDAO.listarCursosPorUnidade(unidade.id()));

            cbCurso.setItems(FXCollections.observableArrayList(cursos));
            cbCurso.setDisable(false);
            cbCurso.getSelectionModel().select(TODOS_CURSOS); // dispara onCursoSelecionado
        } catch (SQLException e) {
            cbCurso.setDisable(true);
            mostrarErro("Não foi possível carregar os cursos da unidade.", e);
        }
    }

    private void onCursoSelecionado(Opcao curso) {
        limparTabela();
        cbTurmaLab.getItems().clear();

        if (curso == null) {
            cbTurmaLab.setDisable(true);
            return;
        }

        // "Todos" os cursos -> turma fica travada em "Todas"
        if (curso.equals(TODOS_CURSOS)) {
            cbTurmaLab.setItems(FXCollections.observableArrayList(TODAS_TURMAS));
            cbTurmaLab.getSelectionModel().select(TODAS_TURMAS);
            cbTurmaLab.setDisable(true);
            return;
        }

        Opcao unidade = cbUnidade.getValue();
        if (unidade == null) {
            cbTurmaLab.setDisable(true);
            return;
        }

        try {
            List<Opcao> turmas = new ArrayList<>();
            turmas.add(TODAS_TURMAS);
            turmas.addAll(relatorioDAO.listarTurmasPorCursoEUnidade(curso.id(), unidade.id()));

            cbTurmaLab.setItems(FXCollections.observableArrayList(turmas));
            cbTurmaLab.getSelectionModel().select(TODAS_TURMAS);
            cbTurmaLab.setDisable(false);
        } catch (SQLException e) {
            cbTurmaLab.setDisable(true);
            mostrarErro("Não foi possível carregar as turmas do curso.", e);
        }
    }


    // Relatório

    @FXML
    public void onGerarRelatorio(ActionEvent event) {
        Opcao unidade = cbUnidade.getValue();
        LocalDate inicio = dataInicio.getValue();
        LocalDate fim = dataFim.getValue();

        if (unidade == null) {
            mostrarAviso("Selecione a unidade.");
            return;
        }
        if (inicio == null || fim == null) {
            mostrarAviso("Selecione a data inicial e a data final.");
            return;
        }
        if (fim.isBefore(inicio)) {
            mostrarAviso("A data final não pode ser anterior à data inicial.");
            return;
        }

        Integer idCurso = idOuNulo(cbCurso.getValue());
        Integer idTurma = idOuNulo(cbTurmaLab.getValue());

        try {
            List<InsumoNecessario> insumos =
                    relatorioDAO.buscarInsumosNecessarios(inicio, fim, unidade.id(), idCurso, idTurma);

            tabelaInsumos.getItems().setAll(insumos);
            if (insumos.isEmpty()) {
                tabelaInsumos.setPlaceholder(new Label("Nenhuma aula agendada com esses filtros no período."));
            }
        } catch (SQLException e) {
            limparTabela();
            mostrarErro("Não foi possível gerar a lista de insumos.", e);
        }
    }


    @FXML
    public void onLimparFiltros(ActionEvent event) {
        cbUnidade.getSelectionModel().clearSelection(); // limpa curso e turma em cascata
        dataInicio.setValue(null);
        dataFim.setValue(null);
        limparTabela();
    }

    private Integer idOuNulo(Opcao opcao) {
        return (opcao == null || opcao.id() == 0) ? null : opcao.id();
    }

    private void limparTabela() {
        tabelaInsumos.getItems().clear();
        tabelaInsumos.setPlaceholder(new Label(MSG_INICIAL));
    }

    private void mostrarAviso(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensagem, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.setTitle("Atenção");
        alerta.showAndWait();
    }

    private void mostrarErro(String mensagem, Exception e) {
        e.printStackTrace();
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensagem + "\n\nDetalhe: " + e.getMessage(), ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.setTitle("Erro");
        alerta.showAndWait();
    }


    // Navegação (sem alterações)

    @FXML
    public void onClickHome(ActionEvent event) throws IOException {
        MainApplication.trocadorDeTelas("homeTest.fxml", ScreenTransitionManager.Direction.BACKWARD);
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
    public void onClickCalendario(ActionEvent event) {
        // Já está na tela de calendário
    }

    @FXML
    public void onClickRelatorio(ActionEvent event) throws IOException {
        MainApplication.trocadorDeTelas("historico.fxml", ScreenTransitionManager.Direction.FORWARD);
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
    public void onClickCadastroUsuario(ActionEvent event) throws IOException {
        MainApplication.trocadorDeTelas("cadastro.fxml", ScreenTransitionManager.Direction.FORWARD);
    }

    @FXML
    public void onClickCadastroTurma(ActionEvent event) throws IOException {
        MainApplication.trocadorDeTelas("cadastro-turma.fxml", ScreenTransitionManager.Direction.FORWARD);
    }

    @FXML
    public void botaoSairAction(ActionEvent event) {
        com.sigec.system.sigec.Utils.SidebarNavigationManager.executarLogout();
    }
}