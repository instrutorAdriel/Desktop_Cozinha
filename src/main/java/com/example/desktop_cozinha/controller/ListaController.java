package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.model.ListaEstoqueDAO;
import com.example.desktop_cozinha.model.ProdutoListaEstoque;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Controller da tela principal de listagem do estoque.
 */
public class ListaController {

    // ── TableView ──────────────────────────────────────────────────────────────

    @FXML
    private TableView<ProdutoListaEstoque> ListaEstoque;

    @FXML
    private TableColumn<ProdutoListaEstoque, String> nomeProduto;

    @FXML
    private TableColumn<ProdutoListaEstoque, String> tipoProduto;

    @FXML
    private TableColumn<ProdutoListaEstoque, String> quantidadeProduto;

    @FXML
    private TableColumn<ProdutoListaEstoque, String> unidadeProduto;

    // ── Filtros ────────────────────────────────────────────────────────────────

    @FXML
    private CheckBox chkNaoPereciveis;

    @FXML
    private CheckBox chkPereciveis;

    @FXML
    private CheckBox chkUtensilios;

    @FXML
    private TextField FiltrarProdutos;

    // ── Hyperlinks ─────────────────────────────────────────────────────────────

    @FXML
    private Hyperlink editar;

    @FXML
    private Hyperlink remover;

    // ── Estado e DAO ───────────────────────────────────────────────────────────

    private final ListaEstoqueDAO dao = new ListaEstoqueDAO();

    // ── Inicialização ──────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        configurarColunas();

        // Listeners dos checkboxes acionam o filtro automaticamente
        chkNaoPereciveis.setOnAction(event -> filtrar());
        chkPereciveis.setOnAction(event -> filtrar());
        chkUtensilios.setOnAction(event -> filtrar());

        // Listener do campo de texto para filtrar ao digitar (UX melhorado)
        FiltrarProdutos.textProperty().addListener((obs, antigo, novo) -> filtrar());

        carregarDados("", false, false, false);
    }

    private void configurarColunas() {
        nomeProduto.setCellValueFactory(new PropertyValueFactory<>("nome"));
        tipoProduto.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        quantidadeProduto.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        unidadeProduto.setCellValueFactory(new PropertyValueFactory<>("unidade"));
    }

    // ── Ações de filtro ────────────────────────────────────────────────────────

    /** Chamado pelo botão "Buscar" e pelos listeners internos. */
    @FXML
    protected void onHelloButtonClick() {
        filtrar();
    }

    private void filtrar() {
        carregarDados(
                FiltrarProdutos.getText(),
                chkNaoPereciveis.isSelected(),
                chkPereciveis.isSelected(),
                chkUtensilios.isSelected()
        );
    }

    private void carregarDados(String textoBusca, boolean naoPereciveis, boolean perecivel, boolean utensilio) {
        ObservableList<ProdutoListaEstoque> lista =
                FXCollections.observableArrayList(dao.filtrarProdutos(textoBusca, naoPereciveis, perecivel, utensilio));
        ListaEstoque.setItems(lista);
    }

    // ── Ação: Editar ───────────────────────────────────────────────────────────

    @FXML
    protected void onEditarProduto(javafx.event.ActionEvent actionEvent) {
        ProdutoListaEstoque produtoSelecionado = ListaEstoque.getSelectionModel().getSelectedItem();

        if (produtoSelecionado == null) {
            mostrarAviso("Por favor, selecione um produto na lista para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/desktop_cozinha/edicaoProdutos.fxml"));
            Parent root = loader.load();

            EdicaoProdutosController controllerEdicao = loader.getController();
            controllerEdicao.preencherDadosParaEdicao(produtoSelecionado);

            Stage stage = (Stage) ListaEstoque.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir a tela de edição. Tente novamente.");
        }
    }

    // ── Ação: Remover ──────────────────────────────────────────────────────────

    /**
     * CORRIGIDO: era referenciado no FXML como "#onRemoverProduto" mas o método
     * não existia no controller. Agora está implementado corretamente.
     */
    @FXML
    protected void onRemoverProduto(javafx.event.ActionEvent actionEvent) {
        ProdutoListaEstoque produtoSelecionado = ListaEstoque.getSelectionModel().getSelectedItem();

        if (produtoSelecionado == null) {
            mostrarAviso("Por favor, selecione um produto na lista para remover.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/desktop_cozinha/remover-Lista.fxml"));
            Parent root = loader.load();

            RemoverProdutosController controllerRemover = loader.getController();
            controllerRemover.preencherDadosParaRemocao(produtoSelecionado);

            Stage stage = (Stage) ListaEstoque.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir a tela de remoção. Tente novamente.");
        }
    }

    // ── Utilitários de UI ──────────────────────────────────────────────────────

    private void mostrarAviso(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Atenção");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void mostrarErro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
