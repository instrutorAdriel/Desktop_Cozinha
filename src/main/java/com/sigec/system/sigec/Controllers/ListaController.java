package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.ListaEstoque;
import com.sigec.system.sigec.DAOS.ProductStorageDAO;
import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Services.SessaoService;
import com.sigec.system.sigec.Utils.BackgroundAnimator;
import com.sigec.system.sigec.Utils.ButtonBorderLapAnimator;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.List;

public class ListaController implements Initializable {

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
    private Button botaoSair;
    @FXML
    private ComboBox<String> SelecionarUnidade;

    public static String unidadeSelecionada;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (topBarPane != null) {
            BackgroundAnimator.startTopBarAnimation(topBarPane);
        }
        configurarDataHora();
        configurarUsuario();

        if (SelecionarUnidade != null) {
            SelecionarUnidade.getItems().addAll(
                    "07 - CEP ENNIUS MUNIZ",
                    "08 - CEP JÓ - TAGUATINGA",
                    "09 - CEP TALAL CEILÂNDIA",
                    "10 - CEP JOAQUIM - GAMA",
                    "11 - CEP SOBRADINHO",
                    "12 - CEP MS GASTRONOMIA",
                    "24 - CEP AÇÕES MÓVEIS",
                    "25 - POLO SÃO SEBASTIÃO",
                    "26 - POLO PÁTIO BRASIL",
                    "27 - POLO BRAZLÂNDIA",
                    "28 - POLO SANTA MARIA",
                    "29 - POLO RECANTO",
                    "32 - POLO SENAC BELEZA",
                    "34 - CEP CORPORATIVO"
            );

            SelecionarUnidade.getSelectionModel().selectFirst();
            unidadeSelecionada = SelecionarUnidade.getValue();

            SelecionarUnidade.setOnAction(e -> {
                unidadeSelecionada = SelecionarUnidade.getValue();
            });
        }

        if (FiltrarProdutos != null) {
            FiltrarProdutos.setOnAction(this::onPesquisarClick);
        }

        // Configuração das colunas
        nomeProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        tipoProduto.setCellValueFactory(new PropertyValueFactory<>("tipoProduto"));
        quantidadeProduto.setCellValueFactory(new PropertyValueFactory<>("quantidadeProduto"));
        unidadeProduto.setCellValueFactory(new PropertyValueFactory<>("unidadeProduto"));

        // Carrega os dados do banco
        carregarTabelaEstoque();
    }

    private void carregarTabelaEstoque() {
        ProductStorageDAO dao = new ProductStorageDAO();
        List<ListaEstoque> produtos = dao.buscarTodosProdutosEstoque();
        ObservableList<ListaEstoque> obsLista = FXCollections.observableArrayList(produtos);
        ListaEstoque.setItems(obsLista);
        ListaEstoque.refresh();
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

    @FXML
    public void botaoRelatorio(ActionEvent event) {
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
    // AÇÕES DO ESTOQUE
    // =========================================================================

    @FXML
    public void onPesquisarClick(ActionEvent event) {
        String termo = FiltrarProdutos != null ? FiltrarProdutos.getText() : "";
        System.out.println("Pesquisando produtos com termo: " + termo);
    }

    @FXML
    public void onHelloButtonClick(ActionEvent event) {
        onPesquisarClick(event);
    }

    public static ListaEstoque produtoSelecionadoParaEdicao;

    @FXML
    public void onClickAddProduto(ActionEvent event) {
        try {
            MainApplication.abrirPopUp("CadastroProduto.fxml");
            carregarTabelaEstoque();
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
            carregarTabelaEstoque();
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
        alert.setContentText("Deseja realmente remover o produto selecionado?");

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {

            ProductStorageDAO dao = new ProductStorageDAO();
            boolean sucesso = dao.excluirProduto(selecionado.getIdProduto());

            if (sucesso) {
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto removido com sucesso!");
                carregarTabelaEstoque(); // Atualiza a tabela para o item sumir da tela
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