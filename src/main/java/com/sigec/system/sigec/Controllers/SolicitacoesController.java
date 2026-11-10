package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.DAOS.CursoDAO;
import com.sigec.system.sigec.DAOS.SolicitacaoDAO;
import com.sigec.system.sigec.DAOS.UnidadeDAO;
import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Utils.ButtonBorderLapAnimator;
import com.sigec.system.sigec.Utils.DropdownBorderLapAnimator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SolicitacoesController {

    @FXML
    private Label dataLabel;
    @FXML
    private Label horaLabel;
    @FXML
    private Label usuarioLabel;
    @FXML
    private VBox submenuCadastro;
    @FXML
    private Label setaCadastro;
    @FXML
    private Button btnNavCadastro;
    @FXML
    private TextField txtPesquisa;
    @FXML
    private ChoiceBox<String> cbStatusFiltro;
    @FXML
    private TableView<SolicitacaoMock> tabelaSolicitacoes;
    @FXML
    private TableColumn<SolicitacaoMock, String> colData;
    @FXML
    private TableColumn<SolicitacaoMock, String> colSolicitante;
    @FXML
    private TableColumn<SolicitacaoMock, String> colTurma;
    @FXML
    private TableColumn<SolicitacaoMock, String> colInsumo;
    @FXML
    private TableColumn<SolicitacaoMock, String> colQtd;
    @FXML
    private TableColumn<SolicitacaoMock, String> colJustificativa;
    @FXML
    private TableColumn<SolicitacaoMock, String> colStatus;
    @FXML
    private VBox submenuEstoque;
    @FXML
    private Label setaEstoque;
    @FXML
    private Button btnNavEstoque;

    @FXML
    private ChoiceBox<String> cbUnidade;
    @FXML
    private ChoiceBox<String> cbCurso;

    private ObservableList<SolicitacaoMock> listaSolicitacoes = FXCollections.observableArrayList();
    private FilteredList<SolicitacaoMock> filteredData;

    @FXML
    public void initialize() {
        iniciarRelogio();
        usuarioLabel.setText("Administrador");

        configurarTabela();
        configurarFiltroEBusca();

        carregarUnidadesDoBanco();
        carregarCursosDoBanco();

        cbStatusFiltro.setItems(FXCollections.observableArrayList("Todos", "Pendente", "Aprovado", "Recusado"));
        cbStatusFiltro.setValue("Todos");

        // FILTROS AUTOMÁTICOS (Unidade, Curso e Status)
        if (cbUnidade != null) {
            cbUnidade.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        }
        if (cbCurso != null) {
            cbCurso.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        }
        if (cbStatusFiltro != null) {
            cbStatusFiltro.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        }

        carregarDadosDoBanco();
    }

    private void configurarTabela() {
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colSolicitante.setCellValueFactory(new PropertyValueFactory<>("solicitante"));
        colTurma.setCellValueFactory(new PropertyValueFactory<>("turma"));
        colInsumo.setCellValueFactory(new PropertyValueFactory<>("insumo"));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colJustificativa.setCellValueFactory(new PropertyValueFactory<>("justificativa"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tabelaSolicitacoes.setItems(listaSolicitacoes);
    }

    private void configurarFiltroEBusca() {
        filteredData = new FilteredList<>(listaSolicitacoes, p -> true);

        SortedList<SolicitacaoMock> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tabelaSolicitacoes.comparatorProperty());

        tabelaSolicitacoes.setItems(sortedData);
    }

    private void aplicarFiltros() {
        String textoBusca = txtPesquisa != null && txtPesquisa.getText() != null ? txtPesquisa.getText().toLowerCase().trim() : "";
        String statusSelecionado = cbStatusFiltro != null ? cbStatusFiltro.getValue() : "Todos";
        String cursoSelecionado = cbCurso != null ? cbCurso.getValue() : "Todos";

        filteredData.setPredicate(solicitacao -> {
            // 1. Filtro por Status (Acionado no clique do botão Filtrar)
            if (statusSelecionado != null && !statusSelecionado.equalsIgnoreCase("Todos")) {
                if (!solicitacao.getStatus().equalsIgnoreCase(statusSelecionado)) {
                    return false;
                }
            }

            // 2. Filtro por Curso (Automático no evento de seleção)
            if (cursoSelecionado != null && !cursoSelecionado.equalsIgnoreCase("Todos")) {
                if (solicitacao.getTurma() == null || !solicitacao.getTurma().toLowerCase().contains(cursoSelecionado.toLowerCase())) {
                    return false;
                }
            }

            // 3. Filtro por Texto (Acionado no clique do botão Filtrar)
            if (!textoBusca.isEmpty()) {
                boolean bateInsumo = solicitacao.getInsumo() != null && solicitacao.getInsumo().toLowerCase().contains(textoBusca);
                boolean bateSolicitante = solicitacao.getSolicitante() != null && solicitacao.getSolicitante().toLowerCase().contains(textoBusca);

                return bateInsumo || bateSolicitante;
            }

            return true;
        });
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

    // ==========================================
    // AÇÕES DA TELA DE SOLICITAÇÕES
    // ==========================================
    @FXML
    void onFiltrarClick(ActionEvent event) {
        // Recarrega os dados do banco e aplica os filtros de busca por Texto e Status
        carregarDadosDoBanco();
    }

    @FXML
    void onAprovarClick(ActionEvent event) {
        SolicitacaoMock selecionada = tabelaSolicitacoes.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Seleção Necessária", "Selecione uma solicitação na tabela para aprovar.");
            return;
        }

        if (!selecionada.getStatus().equalsIgnoreCase("Pendente")) {
            exibirAlerta(Alert.AlertType.INFORMATION, "Ação Não Permitida", "Esta solicitação já foi finalizada (" + selecionada.getStatus() + ") e não pode ser alterada.");
            return;
        }

        boolean sucesso = SolicitacaoDAO.atualizarStatus(selecionada.getId(), "A", selecionada.getTipo());

        if (sucesso) {
            selecionada.setStatus("Aprovado");
            tabelaSolicitacoes.refresh();
            aplicarFiltros();
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Solicitação do item '" + selecionada.getInsumo() + "' foi APROVADA no banco!");
        } else {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível atualizar a solicitação no banco de dados.");
        }
    }

    @FXML
    void onRecusarClick(ActionEvent event) {
        SolicitacaoMock selecionada = tabelaSolicitacoes.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Seleção Necessária", "Selecione uma solicitação na tabela para recusar.");
            return;
        }

        if (!selecionada.getStatus().equalsIgnoreCase("Pendente")) {
            exibirAlerta(Alert.AlertType.INFORMATION, "Ação Não Permitida", "Esta solicitação já foi finalizada (" + selecionada.getStatus() + ") e não pode ser alterada.");
            return;
        }

        boolean sucesso = SolicitacaoDAO.atualizarStatus(selecionada.getId(), "R", selecionada.getTipo());

        if (sucesso) {
            selecionada.setStatus("Recusado");
            tabelaSolicitacoes.refresh();
            aplicarFiltros();
            exibirAlerta(Alert.AlertType.WARNING, "Recusado", "Solicitação do item '" + selecionada.getInsumo() + "' foi RECUSADA no banco!");
        } else {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível atualizar a solicitação no banco de dados.");
        }
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    // ==========================================
    // NAVEGAÇÃO DO MENU LATERAL
    // ==========================================

    @FXML
    public void onClickHome(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("homeTest.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickRelatorio(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("historico.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickSolicitacoes(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("solicitacoes-extras.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void botaoCalendarioAction(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("calendario.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickCadastroUsuario(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("cadastro.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickCadastroTurma(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("cadastro-turma.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void botaoSairAction(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("login.fxml");
        } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickUtensilios(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("gestao-utensilios.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarDadosDoBanco() {
        listaSolicitacoes.clear();
        List<SolicitacaoMock> dados = SolicitacaoDAO.listarTodasSolicitacoes();
        listaSolicitacoes.addAll(dados);
        aplicarFiltros();
    }

    private void carregarUnidadesDoBanco() {
        // Utiliza o método nativo da UnidadeDAO mantendo a classe DAO original intocada
        List<String> unidadesDB = UnidadeDAO.listarNomesUnidades();
        ObservableList<String> items = FXCollections.observableArrayList(unidadesDB);

        cbUnidade.setItems(items);

        if (items.contains("Todas")) {
            cbUnidade.setValue("Todas");
        } else if (!items.isEmpty()) {
            cbUnidade.getSelectionModel().selectFirst();
        }
    }

    private void carregarCursosDoBanco() {
        List<String> cursosDB = CursoDAO.listarNomesCursos();
        ObservableList<String> items = FXCollections.observableArrayList(cursosDB);

        cbCurso.setItems(items);
        if (items.contains("Todos")) {
            cbCurso.setValue("Todos");
        } else if (!items.isEmpty()) {
            cbCurso.getSelectionModel().selectFirst();
        }
    }

    public static class SolicitacaoMock {
        private int id;
        private String data;
        private String solicitante;
        private String turma;
        private String insumo;
        private String quantidade;
        private String justificativa;
        private String status;
        private String tipo;

        public SolicitacaoMock(int id, String data, String solicitante, String turma, String insumo, String quantidade,
                               String justificativa, String status, String tipo) {
            this.id = id;
            this.data = data;
            this.solicitante = solicitante;
            this.turma = turma;
            this.insumo = insumo;
            this.quantidade = quantidade;
            this.justificativa = justificativa;
            this.status = status;
            this.tipo = tipo;
        }

        public int getId() { return id; }
        public String getData() { return data; }
        public String getSolicitante() { return solicitante; }
        public String getTurma() { return turma; }
        public String getInsumo() { return insumo; }
        public String getQuantidade() { return quantidade; }
        public String getJustificativa() { return justificativa; }
        public String getStatus() { return status; }
        public String getTipo() { return tipo; }
        public void setStatus(String status) { this.status = status; }
    }
}