package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Services.SessaoService;
import com.sigec.system.sigec.Utils.BackgroundAnimator;
import com.sigec.system.sigec.Utils.ButtonBorderLapAnimator;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controlador da tela de Histórico e Auditoria de Movimentações.
 */
public class HistoricoController implements Initializable {

    @FXML
    private AnchorPane topBarPane;

    @FXML
    private Label dataLabel;

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label horaLabel;

    @FXML
    private TextField txtpesquisa;

    @FXML
    private Button btnbuscardata;

    @FXML
    private DatePicker dataDE;

    @FXML
    private DatePicker dataATE;

    @FXML
    private ChoiceBox<String> filtro;

    @FXML
    private TableView<?> tableHistorico;

    @FXML
    private TableColumn<?, ?> data_hora;

    @FXML
    private TableColumn<?, ?> nome_produto;

    @FXML
    private TableColumn<?, ?> tipo_estoque;

    @FXML
    private TableColumn<?, ?> quantidade;

    @FXML
    private TableColumn<?, ?> tipo_movimentacao;

    @FXML
    private TableColumn<?, ?> nome_usuario;

    @FXML
    private Button btnNavCadastro;

    @FXML
    private VBox submenuCadastro;

    @FXML
    private Label setaCadastro;

    @FXML
    private Button botaoSair;

    @FXML
    private VBox submenuEstoque;

    @FXML
    private Label setaEstoque;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (topBarPane != null) {
            BackgroundAnimator.startTopBarAnimation(topBarPane);
        }
        configurarDataHora();
        configurarUsuario();
        configurarFiltroCategorias();

        if (txtpesquisa != null) {
            txtpesquisa.setOnAction(this::filtrarHistorico);
        }
    }

    private void configurarUsuario() {
        if (usuarioLabel != null) {
            usuarioLabel.setText(SessaoService.getNomeUsuarioLogado());
        }
    }

    private void configurarFiltroCategorias() {
        if (filtro != null) {
            filtro.setItems(FXCollections.observableArrayList(
                    "Todos",
                    "Entrada",
                    "Saída",
                    "Ajuste",
                    "Descarte"
            ));
            filtro.getSelectionModel().selectFirst();
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
    protected void botaoCalendarioAction(javafx.event.ActionEvent event) {
        try {
            com.sigec.system.sigec.MainApplication.trocadorDeTelas("calendario.fxml");
        } catch (java.io.IOException e) {
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
    public void toggleDropdownEstoque() {
        boolean estaAberto = submenuEstoque.isVisible();
        System.out.println("Botão Estoque clicado! O menu estava aberto? " + estaAberto);

        // Inverte o estado atual
        submenuEstoque.setVisible(!estaAberto);
        submenuEstoque.setManaged(!estaAberto);

        // Atualiza a seta indicativa
        if (!estaAberto) {
            setaEstoque.setText("▲");
        } else {
            setaEstoque.setText("▼");
        }

        System.out.println("Novo estado do menu: " + submenuEstoque.isVisible());
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
        try {
            SessaoService.encerrarSessao();
            MainApplication.trocadorDeTelas("login.fxml");
        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao sair: " + e.getMessage());
        }
    }

    // =========================================================================
    // CONSULTA DO HISTÓRICO
    // =========================================================================

    @FXML
    public void filtrarHistorico(ActionEvent event) {
        String termo = txtpesquisa != null ? txtpesquisa.getText() : "";
        String categoria = filtro != null ? filtro.getValue() : "Todos";
        LocalDate de = dataDE != null ? dataDE.getValue() : null;
        LocalDate ate = dataATE != null ? dataATE.getValue() : null;

        System.out.println("Filtrando histórico: termo=" + termo + ", categoria=" + categoria + ", de=" + de + ", ate=" + ate);
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
