package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.model.ListaEstoqueDAO;
import com.example.desktop_cozinha.model.ProdutoListaEstoque;
import com.example.desktop_cozinha.model.removerDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;

import static com.example.desktop_cozinha.MainApplication.trocadorDeTelas;


public class ListaController {

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

    @FXML
    private CheckBox chkNaoPereciveis;

    @FXML
    private CheckBox chkPereciveis;

    @FXML
    private CheckBox chkUtensilios;

    @FXML
    private TextField FiltrarProdutos;

    @FXML
    private Hyperlink editar;

    @FXML
    private Hyperlink remover;

    private Integer idProdutoEmRemover;

    private final ListaEstoqueDAO dao = new ListaEstoqueDAO();

    @FXML
    public void initialize() {
        configurarColunas();
        chkNaoPereciveis.setOnAction(event -> onHelloButtonClick());
        chkPereciveis.setOnAction(event -> onHelloButtonClick());
        chkUtensilios.setOnAction(event -> onHelloButtonClick());
        carregarDados("", false,false,false);
    }

    private void configurarColunas() {

        nomeProduto.setCellValueFactory(new PropertyValueFactory<>("nome"));
        tipoProduto.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        quantidadeProduto.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        unidadeProduto.setCellValueFactory(new PropertyValueFactory<>("unidade"));
    }

    @FXML
    protected void onHelloButtonClick() {
        String textoBusca = FiltrarProdutos.getText();
        boolean naoPereciveis = chkNaoPereciveis.isSelected();
        boolean perecivel = chkPereciveis.isSelected();
        boolean utensilio = chkUtensilios.isSelected();
        carregarDados(textoBusca, naoPereciveis, perecivel, utensilio);

    }
    private void carregarDados(String textoBusca, boolean naoPereciveis, boolean perecivel, boolean utensilio ) {

        ObservableList<ProdutoListaEstoque> lista =
                FXCollections.observableArrayList(dao.filtrarProdutos(textoBusca,naoPereciveis, perecivel, utensilio));

        ListaEstoque.setItems(lista);
    }
    @FXML
    protected void onEditarProduto(javafx.event.ActionEvent actionEvent) {
        ProdutoListaEstoque produtoSelecionado = ListaEstoque.getSelectionModel().getSelectedItem();

        if (produtoSelecionado == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Atenção");
            alerta.setContentText("Por favor, selecione um produto na lista para editar.");
            alerta.showAndWait();
            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/desktop_cozinha/edicaoProdutos.fxml"));
            Parent root = loader.load();

            EdicaoProdutosController controllerEdicao = loader.getController();

            // Passa o produto selecionado para a sua tela
            controllerEdicao.preencherDadosParaEdicao(produtoSelecionado);

            // Usa a ListaEstoque para pegar a Scene e o Window
            Stage stage = (Stage) ListaEstoque.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();


        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir a tela de edição!");
        }
    }

    protected void onRemoverProdutoClick() throws Exception {
        removerDAO dao = new removerDAO();

        dao.deletarProduto(idProdutoEmRemover);
    }

    protected void onAlterarProduto(javafx.event.ActionEvent actionEvent) {
        ProdutoListaEstoque produtoSelecionado = ListaEstoque.getSelectionModel().getSelectedItem();

        if (produtoSelecionado == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Atenção");
            alerta.setContentText("Por favor, selecione um produto na lista para remover.");
            alerta.showAndWait();
            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/desktop_cozinha/remover-Lista.fxml"));
            Parent root = loader.load();

            removerProdutosController controllerRemover = loader.getController();

            // Passa o produto selecionado para a sua tela
            controllerRemover.preencherDadosParaEdicao(produtoSelecionado);

            // Usa a ListaEstoque para pegar a Scene e o Window
            Stage stage = (Stage) ListaEstoque.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();


        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir a tela de edição!");
        }
    }

}