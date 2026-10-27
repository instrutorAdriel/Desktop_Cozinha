package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.Utensilio;
import com.sigec.system.sigec.DAOS.UtensilioDAO;
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

    // IDs corrigidos para bater exatamente com o gestao-utensilios.fxml
    @FXML private TextField txtPesquisaUtensilio;
    @FXML private ChoiceBox<String> cbStatus;
    @FXML private ChoiceBox<String> cbLaboratorio;

    @FXML private TableView<Utensilio> tabelaUtensilios;
    @FXML private TableColumn<Utensilio, String> colItem;
    @FXML private TableColumn<Utensilio, String> colLaboratorio;
    @FXML private TableColumn<Utensilio, String> colStatus;
    @FXML private TableColumn<Utensilio, Integer> colQtdTotal;
    @FXML private TableColumn<Utensilio, Integer> colQtdDisponivel;

    @FXML private VBox submenuEstoque;
    @FXML private Label setaEstoque;
    @FXML private Button btnNavEstoque;

    @FXML private ChoiceBox<String> cbUnidade;
    @FXML private ChoiceBox<String> cbCurso;

    private ObservableList<Utensilio> observableListUtensilios;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        iniciarRelogio();
        usuarioLabel.setText("Administrador");

        // Preenche os ChoiceBoxes de Filtro
        if (cbStatus != null) {
            cbStatus.setItems(FXCollections.observableArrayList("Todos", "Disponível", "Em uso", "Conserto"));
            cbStatus.setValue("Todos");
        }

        if (cbLaboratorio != null) {
            cbLaboratorio.setItems(FXCollections.observableArrayList("Todos", "Estoque Central", "Cozinha Quente", "Cozinha Oriental"));
            cbLaboratorio.setValue("Todos");
        }

        configurarColunas();
        carregarDadosBanco();
    }

    private void configurarColunas() {
        colItem.setCellValueFactory(new PropertyValueFactory<>("nome"));
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

    // ==========================================
    // AÇÕES DA TELA (Adicionados os métodos ausentes)
    // ==========================================
    @FXML
    public void onFiltrarClick(ActionEvent event) {
        System.out.println("Filtrando status: " + cbStatus.getValue() + " | Busca: " + txtPesquisaUtensilio.getText());
    }

    @FXML
    public void onRegistrarAvariaClick(ActionEvent event) {
        System.out.println("Abrindo tela de registro de avaria...");
    }

    @FXML
    public void onEditarClick(ActionEvent event) {
        System.out.println("Editando utensílio...");
    }

    @FXML
    public void onAdicionarClick(ActionEvent event) {
        System.out.println("Adicionando novo utensílio...");
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