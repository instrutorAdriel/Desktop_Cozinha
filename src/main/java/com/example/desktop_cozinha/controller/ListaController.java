package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.model.ListaEstoqueDAO;
import com.example.desktop_cozinha.model.ProdutoListaEstoque;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

/**
 * Controller responsável pela tela de lista de estoque.
 * Realiza filtros e carregamento dos produtos na tabela
 */

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

    // Inicializa a tela e configura os eventos dos filtros

    @FXML
    public void initialize() {
        configurarColunas();

        // Atualiza a tabela quando os filtros forem alterados

        chkNaoPereciveis.setOnAction(event -> onHelloButtonClick());
        chkPereciveis.setOnAction(event -> onHelloButtonClick());
        chkUtensilios.setOnAction(event -> onHelloButtonClick());
        configurarMenuContexto();

        // Carrega todos os produtos inicialmente

        carregarDados("", false,false,false);
    }

    //Configura as colunas da tabela com os atributos do model

    private void configurarColunas() {

        nomeProduto.setCellValueFactory(new PropertyValueFactory<>("nome"));
        tipoProduto.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        quantidadeProduto.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        unidadeProduto.setCellValueFactory(new PropertyValueFactory<>("unidade"));
    }

    // Executa a filtragem dos produtos

    @FXML
    protected void onHelloButtonClick() {
        String textoBusca = FiltrarProdutos.getText();
        boolean naoPereciveis = chkNaoPereciveis.isSelected();
        boolean perecivel = chkPereciveis.isSelected();
        boolean utensilio = chkUtensilios.isSelected();
        carregarDados(textoBusca, naoPereciveis, perecivel, utensilio);

    }

    // Carrega os produtos filtrados na tabela

    private void carregarDados(String textoBusca, boolean naoPereciveis, boolean perecivel, boolean utensilio ) {

        ObservableList<ProdutoListaEstoque> lista =
                FXCollections.observableArrayList(dao.filtrarProdutos(textoBusca,naoPereciveis, perecivel, utensilio));

        ListaEstoque.setItems(lista);
    }

    // Executa a exclusão dos produtos

    private void configurarMenuContexto() {

        ListaEstoque.setRowFactory(tv -> {

            TableRow<ProdutoListaEstoque> row = new TableRow<>();

            ContextMenu menu = new ContextMenu();

            MenuItem deletar = new MenuItem("Deletar");

            deletar.setOnAction(e -> {

                ProdutoListaEstoque produto = row.getItem();

                if (produto != null) {

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

                    alert.setTitle("Confirmação");

                    alert.setHeaderText("Deletar produto");

                    alert.setContentText(
                            "Deseja realmente excluir:\n"
                                    + produto.getNome()
                                    + "\n\nEssa ação não pode ser desfeita."
                    );

                    alert.showAndWait().ifPresent(resposta -> {

                        if (resposta == ButtonType.OK) {

                            dao.deletar(produto.getId());

                            ListaEstoque.getItems().remove(produto);

                            System.out.println("Deletado: " + produto.getNome());
                        }
                    });
                }
            });

            menu.getItems().add(deletar);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(menu)
            );

            return row;
        });
    }

}