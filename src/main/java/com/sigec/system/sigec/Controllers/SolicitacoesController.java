package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Utils.ButtonBorderLapAnimator;
import com.sigec.system.sigec.Utils.DropdownBorderLapAnimator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SolicitacoesController {

    @FXML private Label dataLabel;
    @FXML private Label horaLabel;
    @FXML private Label usuarioLabel;

    @FXML private VBox submenuCadastro;
    @FXML private Label setaCadastro;
    @FXML
    private Button btnNavCadastro;

    @FXML private TextField txtPesquisa;
    @FXML private ChoiceBox<String> cbStatusFiltro;

    @FXML private TableView<SolicitacaoMock> tabelaSolicitacoes;
    @FXML private TableColumn<SolicitacaoMock, String> colData;
    @FXML private TableColumn<SolicitacaoMock, String> colSolicitante;
    @FXML private TableColumn<SolicitacaoMock, String> colTurma;
    @FXML private TableColumn<SolicitacaoMock, String> colInsumo;
    @FXML private TableColumn<SolicitacaoMock, String> colQtd;
    @FXML private TableColumn<SolicitacaoMock, String> colJustificativa;
    @FXML private TableColumn<SolicitacaoMock, String> colStatus;
    @FXML
    private VBox submenuEstoque;

    @FXML
    private Label setaEstoque;

    @FXML
    private Button btnNavEstoque;

    private ObservableList<SolicitacaoMock> listaSolicitacoes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        iniciarRelogio();
        usuarioLabel.setText("Administrador");

        // Preenche o ChoiceBox de Filtro
        cbStatusFiltro.setItems(FXCollections.observableArrayList("Todos", "Pendente", "Aprovado", "Recusado"));
        cbStatusFiltro.setValue("Pendente");

        configurarTabela();
        carregarDadosDeTeste();
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
        System.out.println("Filtrando status: " + cbStatusFiltro.getValue() + " | Busca: " + txtPesquisa.getText());
    }

    @FXML
    void onAprovarClick(ActionEvent event) {
        SolicitacaoMock selecionada = tabelaSolicitacoes.getSelectionModel().getSelectedItem();
        if (selecionada != null && selecionada.getStatus().equals("Pendente")) {
            // Lógica para abater do estoque e atualizar no banco
            selecionada.setStatus("Aprovado");
            tabelaSolicitacoes.refresh();
            System.out.println("Solicitação Aprovada: " + selecionada.getInsumo());
        } else {
            System.out.println("Selecione uma solicitação 'Pendente' para aprovar.");
        }
    }

    @FXML
    void onRecusarClick(ActionEvent event) {
        SolicitacaoMock selecionada = tabelaSolicitacoes.getSelectionModel().getSelectedItem();
        if (selecionada != null && selecionada.getStatus().equals("Pendente")) {
            // Aqui chamaria um modal pedindo o motivo da recusa
            selecionada.setStatus("Recusado");
            tabelaSolicitacoes.refresh();
            System.out.println("Solicitação Recusada: " + selecionada.getInsumo());
        } else {
            System.out.println("Selecione uma solicitação 'Pendente' para recusar.");
        }
    }

    // ==========================================
    // AÇÕES DO MENU LATERAL
    // ==========================================

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
        try { MainApplication.trocadorDeTelas("login.fxml"); } catch (Exception e) { e.printStackTrace(); }
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
    public void onClickInsumos(javafx.event.ActionEvent event) {
        try {
            com.sigec.system.sigec.MainApplication.trocadorDeTelas("lista-estoque.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao carregar a tela de Insumos: " + e.getMessage());
        }
    }

    @FXML
    public void onClickUtensilios(javafx.event.ActionEvent event) {
        try {
            com.sigec.system.sigec.MainApplication.trocadorDeTelas("gestao-utensilios.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao carregar a tela de Utensílios: " + e.getMessage());
        }
    }


    // ==========================================
    // DADOS MOCK (Exemplo visual)
    // ==========================================
    private void carregarDadosDeTeste() {
        listaSolicitacoes.addAll(
                new SolicitacaoMock("20/10/2026 14:30", "Prof. João Gastronomia", "Cozinha Quente A", "Farinha de Trigo", "2 kg", "Receita rendeu menos que o esperado", "Pendente"),
                new SolicitacaoMock("20/10/2026 15:10", "Prof. Maria Confeitaria", "Confeitaria B", "Chocolate Meio Amargo", "500 g", "Teste extra de temperagem", "Pendente"),
                new SolicitacaoMock("19/10/2026 09:00", "Prof. Carlos", "Panificação", "Fermento Biológico", "50 g", "O fermento anterior estava inativo", "Aprovado")
        );
    }

    // CLASSE INTERNA MOCK PARA A TABELA
    public static class SolicitacaoMock {
        private String data;
        private String solicitante;
        private String turma;
        private String insumo;
        private String quantidade;
        private String justificativa;
        private String status;

        public SolicitacaoMock(String data, String solicitante, String turma, String insumo, String quantidade, String justificativa, String status) {
            this.data = data; this.solicitante = solicitante; this.turma = turma;
            this.insumo = insumo; this.quantidade = quantidade; this.justificativa = justificativa; this.status = status;
        }

        public String getData() { return data; }
        public String getSolicitante() { return solicitante; }
        public String getTurma() { return turma; }
        public String getInsumo() { return insumo; }
        public String getQuantidade() { return quantidade; }
        public String getJustificativa() { return justificativa; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}