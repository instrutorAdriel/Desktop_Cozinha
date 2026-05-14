package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
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
            // 1. Instanciamos o FXMLLoader para carregar a tela de edição
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("edicaoProdutos.fxml"));
            Parent root = fxmlLoader.load();

            // 2. Recuperamos o controller da tela de edição pelo fxmlLoader
            EdicaoProdutosController controllerEdicao = fxmlLoader.getController();

            // 3. Passamos o produto selecionado para o controller ANTES de abrir a janela
            controllerEdicao.preencherDadosParaEdicao(produtoSelecionado);

            // 4. Criamos e configuramos o Stage (Janela) do Pop-up
            Stage popupStage = new Stage();
            popupStage.setScene(new Scene(root));

            // Define que é um Pop-up (Modal)
            popupStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            // Trava o Pop-up na janela atual da lista de estoque
            popupStage.initOwner(ListaEstoque.getScene().getWindow());
            popupStage.setResizable(false);

            // 5. Exibe o Pop-up e trava a execução desta tela até ele ser fechado
            popupStage.showAndWait();

            // 6. Opcional (Mas recomendado): Recarrega a lista para mostrar a edição que acabou de ser feita
            onHelloButtonClick();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir a tela de edição!");
        }
    }
}