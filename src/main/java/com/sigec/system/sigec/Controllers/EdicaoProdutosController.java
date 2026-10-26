package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.DAOS.ProductStorageDAO;
import com.sigec.system.sigec.Constructors.ListaEstoque;
import com.sigec.system.sigec.Utils.FormNavigationUtil;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EdicaoProdutosController implements Initializable {

    @FXML private TextField txtNomeProduto;
    @FXML private TextField txtQtdAtual;
    @FXML private TextField txtEstoqueMinimo;
    @FXML private ChoiceBox<String> txtTipoProduto;
    @FXML private ChoiceBox<String> txtUnidadeDeMedida;
    @FXML private DatePicker txtDataValidade;
    @FXML private Button btnVoltar;
    @FXML private Button btnCadastro;

    @FXML
    private ComboBox<String> SelecionarUnidade;

    public static String unidadeSelecionada;

    private int idProdutoEditando;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarSeletores();
        FormNavigationUtil.encadearCampos(btnCadastro, txtNomeProduto, txtQtdAtual, txtEstoqueMinimo);
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
        // Puxa os dados da tabela e preenche os campos automaticamente
        carregarDadosDoProdutoSelecionado();
    }

    private void configurarSeletores() {
        if (txtTipoProduto != null) {
            // Aqui estão os novos tipos de produtos!
            txtTipoProduto.setItems(FXCollections.observableArrayList(
                    "Hortifruti",
                    "Proteína",
                    "Frios",
                    "Mercearia"
            ));
            txtTipoProduto.getSelectionModel().selectFirst();
        }

        if (txtUnidadeDeMedida != null) {
            txtUnidadeDeMedida.setItems(FXCollections.observableArrayList(
                    "kg", "g", "L", "ml", "un", "pct", "cx"
            ));
            txtUnidadeDeMedida.getSelectionModel().selectFirst();
        }
    }

    private void carregarDadosDoProdutoSelecionado() {
        ListaEstoque produto = ListaController.produtoSelecionadoParaEdicao;

        if (produto != null) {
            idProdutoEditando = produto.getIdProduto();

            if (txtNomeProduto != null) txtNomeProduto.setText(produto.getNomeProduto());
            if (txtQtdAtual != null) txtQtdAtual.setText(String.valueOf(produto.getQuantidadeProduto()));

            // Tratamento especial para o tipo de produto, caso o que venha do banco não esteja na lista inicial
            if (txtTipoProduto != null) {
                String tipo = produto.getTipoProduto();
                if (!txtTipoProduto.getItems().contains(tipo)) {
                    txtTipoProduto.getItems().add(tipo);
                }
                txtTipoProduto.setValue(tipo);
            }

            if (txtUnidadeDeMedida != null) {
                String unidade = produto.getUnidadeProduto();
                if (!txtUnidadeDeMedida.getItems().contains(unidade)) {
                    txtUnidadeDeMedida.getItems().add(unidade);
                }
                txtUnidadeDeMedida.setValue(unidade);
            }
        }
    }

    @FXML
    public void onEditar(ActionEvent event) {
        String nome = txtNomeProduto != null ? txtNomeProduto.getText() : "";
        String qtd = txtQtdAtual != null ? txtQtdAtual.getText() : "";
        String tipo = txtTipoProduto != null ? txtTipoProduto.getValue() : "";
        String unidade = txtUnidadeDeMedida != null ? txtUnidadeDeMedida.getValue() : "";

        if (nome.trim().isEmpty() || qtd.trim().isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Campos Obrigatórios", "Preencha o nome e a quantidade do produto.");
            return;
        }

        double quantidadeFormatada;
        try {
            quantidadeFormatada = Double.parseDouble(qtd.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Formato Inválido", "A quantidade deve ser um número válido.");
            return;
        }

        // Envia atualização para o banco
        ProductStorageDAO dao = new ProductStorageDAO();
        boolean sucesso = dao.atualizarProduto(idProdutoEditando, nome, tipo, quantidadeFormatada, unidade);

        if (sucesso) {
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto '" + nome.trim() + "' atualizado com sucesso!");
            fecharJanela();
        } else {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível atualizar o produto no banco.");
        }
    }

    @FXML
    public void voltarTela(ActionEvent event) {
        fecharJanela();
    }

    private void fecharJanela() {
        if (btnVoltar != null && btnVoltar.getScene() != null && btnVoltar.getScene().getWindow() != null) {
            ((Stage) btnVoltar.getScene().getWindow()).close();
        } else if (btnCadastro != null && btnCadastro.getScene() != null && btnCadastro.getScene().getWindow() != null) {
            ((Stage) btnCadastro.getScene().getWindow()).close();
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