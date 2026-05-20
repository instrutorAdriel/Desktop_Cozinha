package com.example.desktop_cozinha.controller;
import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.HomeDAO;
import com.example.desktop_cozinha.model.ListaEstoqueDAO;
import com.example.desktop_cozinha.model.RemoverDAO;
import com.example.desktop_cozinha.model.ProdutoListaEstoque;
import com.example.desktop_cozinha.services.SessaoService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.example.desktop_cozinha.MainApplication.sair;

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
    private Button editar;

    @FXML
    private Button remover;
    @FXML
    private Integer idProdutoEmRemover;

    @FXML
    private Label dataLabel;

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label horaLabel;

    @FXML
    private Button sair;


    // ── Estado e DAO ───────────────────────────────────────────────────────────

    private final ListaEstoqueDAO dao = new ListaEstoqueDAO();

    @FXML
    public void initialize() {
        configurarColunas();
        chkNaoPereciveis.setOnAction(event -> onHelloButtonClick());
        chkPereciveis.setOnAction(event -> onHelloButtonClick());
        chkUtensilios.setOnAction(event -> onHelloButtonClick());
        carregarDados("", false,false,false);
        configurarRelogio();
        usuarioAtual();
    }

    public void configurarRelogio() {
        DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatadorHora = DateTimeFormatter.ofPattern("HH:mm");

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime agora = LocalDateTime.now();
            dataLabel.setText(agora.format(formatadorData));
            horaLabel.setText(agora.format(formatadorHora));
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);

        LocalDateTime agoraInicial = LocalDateTime.now();
        dataLabel.setText(agoraInicial.format(formatadorData));
        horaLabel.setText(agoraInicial.format(formatadorHora));

        timeline.play();
    }

    public void usuarioAtual() {
        String email = SessaoService.getEmailAtual();
        if (email != null) {
            HomeDAO user = new HomeDAO();
            String nome = user.bucarNome(email);
            usuarioLabel.setText("Bem vindo, " + nome);
        }
    }

    public void botaoSairAction( ) throws IOException {
        sair();
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
            RemoverDAO daoRemover = new RemoverDAO();
            // Chama o metodo deletarProduto passando o ID do produto
            boolean sucesso = daoRemover.deletarProduto(produtoSelecionado.getId());

            if (sucesso) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto removido com sucesso!");
                onHelloButtonClick();
                //filtrar(); // Atualiza a tabela para refletir a mudança
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

            // 6.  Recarrega a lista para mostrar a edição que acabou de ser feita
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
    }

    public void onClickHome() throws IOException {
        MainApplication.trocadorDeTelas("home.fxml");
    }

    public void onClickRelatorio() throws IOException {
        //MainApplication.trocadorDeTelas("relatorio.fxml");
    }

    public void onClickAddProduto() throws IOException {
        MainApplication.abrirPopUp("CadastroProduto.fxml");
    }
}


