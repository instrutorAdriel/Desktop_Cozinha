package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
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
import javafx.stage.Stage;

import java.util.Optional;

import static com.example.desktop_cozinha.MainApplication.abrirPopUp;

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
    @FXML
    private Button pesquisar;

    @FXML
    private Button cadastrar;

    @FXML
    private Button editar;

    @FXML
    private Button remover;
    private Integer idProdutoEmRemover;

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


        carregarDados("", false, false, false);
    }

    private void configurarColunas() {
        nomeProduto.setCellValueFactory(new PropertyValueFactory<>("nome"));
        tipoProduto.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        quantidadeProduto.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        unidadeProduto.setCellValueFactory(new PropertyValueFactory<>("unidade"));
    }

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


    @FXML
    protected void cadastrarProduto() throws Exception {
        MainApplication.abrirPopUp("view/CadastroProduto.fxml");
    }




    /**
     * Executa a remoção do produto após confirmação do usuário.
     */

    // metodo para injetar o id do produto no pop-up para remover produto
    @FXML
    protected void onRemoverProdutoClick  (javafx.event.ActionEvent actionEvent) {
        // 1. Recupera o produto selecionado na TableView
        ProdutoListaEstoque produtoSelecionado = ListaEstoque.getSelectionModel().getSelectedItem();

        if (produtoSelecionado == null) {
            mostrarAviso("Por favor, selecione um produto na lista para remover.");
            return;
        }

        // 2. Cria o pop-up de confirmação nativo do JavaFX
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir Produto: " + produtoSelecionado.getNome());
        alert.setContentText("Tem certeza que deseja remover este item? Esta ação não pode ser desfeita.");

        // 3. Exibe o pop-up e aguarda a resposta (showAndWait trava a execução até o clique)
        Optional<ButtonType> result = alert.showAndWait();

        // 4. Se o usuário clicou em OK, prossegue com a remoção
        if (result.isPresent() && result.get() == ButtonType.OK) {
            removerDAO daoRemover = new removerDAO();
            // Chama o metodo deletarProduto passando o ID do produto
            boolean sucesso = daoRemover.deletarProduto(produtoSelecionado.getId());

            if (sucesso) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto removido com sucesso!");
                filtrar(); // Atualiza a tabela para refletir a mudança
            } else {
                mostrarErro("Erro ao tentar remover o produto do sistema.");
            }
        }
    }

    // ── Utilitários de UI ──────────────────────────────────────────────────────

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }



    // ── Ação: Editar ───────────────────────────────────────────────────────────

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
    }}


