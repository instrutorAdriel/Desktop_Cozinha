package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.model.ListaEstoqueDAO;
import com.example.desktop_cozinha.model.ProdutoListaEstoque;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

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
}