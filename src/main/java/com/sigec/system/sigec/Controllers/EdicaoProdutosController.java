package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.ListaEstoque;
import com.sigec.system.sigec.DAOS.CategoriaDAO;
import com.sigec.system.sigec.DAOS.ProductStorageDAO;
import com.sigec.system.sigec.DAOS.UnidadeDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class EdicaoProdutosController implements Initializable {

    @FXML private TextField txtNomeProduto;
    @FXML private TextField txtQtdAtual;
    @FXML private TextField txtEstoqueMinimo;
    @FXML private ChoiceBox<String> txtTipoProduto;
    @FXML private ChoiceBox<String> txtUnidadeDeMedida;
    @FXML private TextField txtLote;
    @FXML private ChoiceBox<String> cbUnidade;
    @FXML private DatePicker txtDataValidade;
    @FXML private Button btnVoltar;
    @FXML private Button btnCadastro;

    private ListaEstoque produtoOriginal;
    private final ProductStorageDAO dao = new ProductStorageDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtTipoProduto.setItems(FXCollections.observableArrayList(CategoriaDAO.listarNomesCategorias()));
        txtUnidadeDeMedida.setItems(FXCollections.observableArrayList("kg", "g", "L", "ml", "un"));

        List<String> unidadesUsuario = UnidadeDAO.listarUnidadesParaCadastro();
        cbUnidade.setItems(FXCollections.observableArrayList(unidadesUsuario));

        produtoOriginal = ListaController.produtoSelecionadoParaEdicao;
        if (produtoOriginal == null) return;

        txtNomeProduto.setText(produtoOriginal.getNomeProduto());
        txtQtdAtual.setText(String.valueOf(produtoOriginal.getQuantidadeProduto()));
        txtTipoProduto.setValue(produtoOriginal.getTipoProduto());
        txtUnidadeDeMedida.setValue(produtoOriginal.getUnidadeProduto());

        ProductStorageDAO.DetalhesInsumo d = dao.buscarDetalhesInsumo(produtoOriginal.getIdInsumo());
        if (d != null) {
            txtQtdAtual.setText(String.valueOf(d.quantidade));
            txtEstoqueMinimo.setText(String.valueOf(d.estoqueMinimo));
            txtLote.setText(d.lote != null ? d.lote : "");
            txtDataValidade.setValue(d.dataValidade);
            if (d.nomeUnidade != null) {
                cbUnidade.setValue(d.nomeUnidade.trim());
            }
        }
        if (cbUnidade.getValue() == null && !cbUnidade.getItems().isEmpty()) {
            cbUnidade.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void onEditar(ActionEvent event) {
        if (produtoOriginal == null) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Nenhum produto selecionado.");
            return;
        }

        String nome = txtNomeProduto.getText();
        String categoria = txtTipoProduto.getValue();
        String unidadeMedida = txtUnidadeDeMedida.getValue();
        String unidade = cbUnidade.getValue();
        String lote = txtLote.getText();
        LocalDate validade = txtDataValidade.getValue();

        if (nome == null || nome.isBlank() || categoria == null || unidadeMedida == null || unidade == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Preencha os campos obrigatórios.");
            return;
        }

        double quantidade;
        double estoqueMinimo;
        try {
            quantidade = Double.parseDouble(txtQtdAtual.getText().replace(",", "."));
            String min = txtEstoqueMinimo.getText();
            estoqueMinimo = (min == null || min.isBlank()) ? 0 : Double.parseDouble(min.replace(",", "."));
        } catch (NumberFormatException | NullPointerException e) {
            exibirAlerta(Alert.AlertType.WARNING, "Erro de Validação", "Quantidade ou estoque mínimo inválido.");
            return;
        }

        if (!nome.trim().equalsIgnoreCase(produtoOriginal.getNomeProduto().trim())
                && dao.verificarProdutoExisteAtivo(nome)) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Já existe um produto ativo com esse nome.");
            return;
        }

        boolean sucesso = dao.atualizarProduto(produtoOriginal.getIdProduto(), produtoOriginal.getIdInsumo(), nome, categoria, quantidade,
                estoqueMinimo, unidadeMedida, lote, validade, unidade);

        if (sucesso) {
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto editado com sucesso!");
            voltarTela(null);
        } else {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível editar o produto.");
        }
    }

    @FXML
    public void voltarTela(ActionEvent event) {
        ListaController.produtoSelecionadoParaEdicao = null;
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        stage.close();
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}