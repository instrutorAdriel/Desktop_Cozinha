package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.DAOS.ProductStorageDAO;
import com.sigec.system.sigec.Utils.FormNavigationUtil;

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
import java.util.ResourceBundle;

public class CadastroProdutoController implements Initializable {

    @FXML private TextField txtNomeProduto;
    @FXML private TextField txtQtdAtual;
    @FXML private TextField txtEstoqueMinimo;
    @FXML private ChoiceBox<String> txtTipoProduto;
    @FXML private ChoiceBox<String> txtUnidadeDeMedida;
    @FXML private DatePicker txtDataValidade;
    @FXML private Button btnVoltar;
    @FXML private Button btnCadastro;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarSeletores();
        FormNavigationUtil.encadearCampos(btnCadastro, txtNomeProduto, txtQtdAtual, txtEstoqueMinimo);
    }

    private void configurarSeletores() {
        if (txtTipoProduto != null) {
            txtTipoProduto.setItems(FXCollections.observableArrayList("Perecível", "Não Perecível", "Utensílio"));
            txtTipoProduto.getSelectionModel().selectFirst();
        }
        if (txtUnidadeDeMedida != null) {
            txtUnidadeDeMedida.setItems(FXCollections.observableArrayList("kg", "g", "L", "ml", "un", "pct", "cx"));
            txtUnidadeDeMedida.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void onCadastrar(ActionEvent event) {
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
            // Substitui vírgula por ponto caso o usuário digite "50,5" em vez de "50.5"
            quantidadeFormatada = Double.parseDouble(qtd.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Formato Inválido", "A quantidade deve ser um número válido.");
            return;
        }

        // Envia para o banco de dados
        ProductStorageDAO dao = new ProductStorageDAO();
        boolean sucesso = dao.adicionarProduto(nome, tipo, quantidadeFormatada, unidade);

        if (sucesso) {
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto '" + nome.trim() + "' cadastrado com sucesso no estoque!");
            fecharJanela();
        } else {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar o produto no banco de dados.");
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