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

public class UtensiliosController {

    // ==========================================
    // ELEMENTOS DO CABEÇALHO (STATUS BAR)
    // ==========================================
    @FXML private Label dataLabel;
    @FXML private Label horaLabel;
    @FXML private Label usuarioLabel;

    // ==========================================
    // ELEMENTOS DO MENU LATERAL
    // ==========================================
    @FXML private VBox submenuCadastro;
    @FXML private Label setaCadastro;
    @FXML
    private Button btnNavCadastro;

    @FXML
    private VBox submenuEstoque;

    @FXML
    private Label setaEstoque;

    @FXML
    private Button btnNavEstoque;

    // ==========================================
    // ELEMENTOS DA TELA CENTRAL
    // ==========================================
    @FXML private TextField txtPesquisaUtensilio;
    @FXML private ChoiceBox<String> cbLaboratorio;
    @FXML private ChoiceBox<String> cbStatus;

    @FXML private TableView<UtensilioMock> tabelaUtensilios;
    @FXML private TableColumn<UtensilioMock, String> colItem;
    @FXML private TableColumn<UtensilioMock, String> colLaboratorio;
    @FXML private TableColumn<UtensilioMock, String> colStatus;
    @FXML private TableColumn<UtensilioMock, Integer> colQtdTotal;
    @FXML private TableColumn<UtensilioMock, Integer> colQtdDisponivel;

    // Lista observável para preencher a tabela
    private ObservableList<UtensilioMock> listaUtensilios = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        iniciarRelogio();

        usuarioLabel.setText("Administrador");

        // Preenche os ChoiceBoxes de filtro
        cbLaboratorio.setItems(FXCollections.observableArrayList("Todos", "Cozinha Quente", "Cozinha Fria", "Confeitaria", "Panificação"));
        cbLaboratorio.setValue("Todos");

        cbStatus.setItems(FXCollections.observableArrayList("Todos", "Disponível", "Em Uso", "Requer Conserto"));
        cbStatus.setValue("Todos");

        configurarTabela();
        carregarDadosDeTeste();
    }

    // ==========================================
    // CONFIGURAÇÕES INICIAIS
    // ==========================================
    private void configurarTabela() {
        colItem.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colLaboratorio.setCellValueFactory(new PropertyValueFactory<>("laboratorio"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colQtdTotal.setCellValueFactory(new PropertyValueFactory<>("qtdTotal"));
        colQtdDisponivel.setCellValueFactory(new PropertyValueFactory<>("qtdDisponivel"));

        tabelaUtensilios.setItems(listaUtensilios);
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
    // AÇÕES DA TELA DE UTENSÍLIOS
    // ==========================================
    @FXML
    void onFiltrarClick(ActionEvent event) {
        String pesquisa = txtPesquisaUtensilio.getText();
        String lab = cbLaboratorio.getValue();
        String status = cbStatus.getValue();
        System.out.println("Filtrando por: " + pesquisa + " | Lab: " + lab + " | Status: " + status);
    }

    @FXML
    void onAdicionarClick(ActionEvent event) {
        System.out.println("Abrir modal de Adicionar Utensílio");
    }

    @FXML
    void onEditarClick(ActionEvent event) {
        UtensilioMock selecionado = tabelaUtensilios.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            System.out.println("Editando utensílio: " + selecionado.getNome());
        } else {
            System.out.println("Nenhum item selecionado!");
        }
    }

    @FXML
    void onRegistrarAvariaClick(ActionEvent event) {
        UtensilioMock selecionado = tabelaUtensilios.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            System.out.println("Abrindo modal de Avaria para o utensílio: " + selecionado.getNome());
        } else {
            System.out.println("Selecione um utensílio na tabela para registrar avaria.");
        }
    }

    // ==========================================
    // DADOS MOCK (Exemplo Visual)
    // ==========================================
    private void carregarDadosDeTeste() {
        listaUtensilios.addAll(
                new UtensilioMock("Balança Digital", "Cozinha Quente", "Disponível", 5, 4, 1),
                new UtensilioMock("Fouet (Batedor)", "Confeitaria", "Em Uso", 12, 10, 0),
                new UtensilioMock("Assadeira Retangular", "Panificação", "Disponível", 20, 18, 2),
                new UtensilioMock("Faca do Chef 8\"", "Cozinha Fria", "Requer Conserto", 8, 5, 3)
        );
    }

    public static class UtensilioMock {
        private String nome;
        private String laboratorio;
        private String status;
        private int qtdTotal;
        private int qtdDisponivel;
        private int qtdAvariada;

        public UtensilioMock(String nome, String lab, String status, int total, int disponivel, int avariada) {
            this.nome = nome;
            this.laboratorio = lab;
            this.status = status;
            this.qtdTotal = total;
            this.qtdDisponivel = disponivel;
            this.qtdAvariada = avariada;
        }

        public String getNome() { return nome; }
        public String getLaboratorio() { return laboratorio; }
        public String getStatus() { return status; }
        public int getQtdTotal() { return qtdTotal; }
        public int getQtdDisponivel() { return qtdDisponivel; }
        public int getQtdAvariada() { return qtdAvariada; }
    }
}