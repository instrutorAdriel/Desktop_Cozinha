package com.sigec.system.sigec.Controllers;

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
import javafx.scene.control.DateCell;


import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CadastroProdutoController implements Initializable {

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtTipoProduto.setItems(FXCollections.observableArrayList(CategoriaDAO.listarNomesCategorias()));
        txtUnidadeDeMedida.setItems(FXCollections.observableArrayList("kg", "g", "L", "ml", "un"));
        cbUnidade.setItems(FXCollections.observableArrayList(UnidadeDAO.listarNomesUnidades()));

        if (cbUnidade.getItems().size() == 1) {
            cbUnidade.getSelectionModel().selectFirst();
        }

        txtDataValidade.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                boolean passado = empty || date.isBefore(LocalDate.now());
                setDisable(passado);
                if (passado) setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #9e9e9e;");
            }
        });
        txtDataValidade.setEditable(false); // impede digitar uma data passada à mão

    }

    @FXML
    public void onCadastrar(ActionEvent event) {
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

        if (validade != null && validade.isBefore(LocalDate.now())) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "A data de validade não pode ser no passado.");
            return;
        }

        ProductStorageDAO dao = new ProductStorageDAO();

        if (dao.verificarProdutoExisteAtivo(nome)) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Já existe um produto ativo com esse nome.");
            return;
        }

        boolean sucesso = dao.adicionarProduto(nome, categoria, quantidade, estoqueMinimo,
                unidadeMedida, lote, validade, unidade);

        if (sucesso) {
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto cadastrado com sucesso!");
            voltarTela(null);
        } else {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível cadastrar o produto.");
        }
    }

    @FXML
    public void voltarTela(ActionEvent event) {
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
