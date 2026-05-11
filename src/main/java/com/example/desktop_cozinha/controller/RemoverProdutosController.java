package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.model.ProdutoListaEstoque;
import com.example.desktop_cozinha.model.removerDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

import java.util.Optional;

import static com.example.desktop_cozinha.MainApplication.trocadorDeTelas;

/**
 * Controller da tela de confirmação de remoção de produto.
 */
public class RemoverProdutosController {

    @FXML
    private Button btnRemoverProduto;

    @FXML
    private Button btnCancelarProduto;

    private Integer idProdutoEmRemover;

    /**
     * Recebe o produto selecionado na tela anterior e salva seu ID.
     */
    public void preencherDadosParaRemocao(ProdutoListaEstoque produto) {
        if (produto != null) {
            this.idProdutoEmRemover = produto.getId();
        }
    }

    /**
     * Volta para a tela de lista de estoque sem remover nada.
     */
    @FXML
    protected void voltarTela() {
        try {
            trocadorDeTelas("lista-estoque.fxml");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível voltar para a tela anterior.");
        }
    }

    /**
     * Executa a remoção do produto após confirmação do usuário.
     */
    @FXML
    protected void onRemoverProdutoClick() {
        if (idProdutoEmRemover == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção", "Nenhum produto selecionado para remoção.");
            return;
        }

        // Pede confirmação antes de deletar
        Optional<ButtonType> resultado = mostrarConfirmacao(
                "Confirmar Remoção",
                "Tem certeza que deseja remover este produto?\nEssa ação não pode ser desfeita."
        );

        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return; // Usuário cancelou
        }

        removerDAO dao = new removerDAO();
        boolean sucesso = dao.deletarProduto(idProdutoEmRemover);

        if (sucesso) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto removido com sucesso!");
            voltarTela(); // Retorna para a lista após remover
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível remover o produto. Tente novamente.");
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

    private Optional<ButtonType> mostrarConfirmacao(String titulo, String mensagem) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle(titulo);
        confirmacao.setHeaderText(null);
        confirmacao.setContentText(mensagem);
        return confirmacao.showAndWait();
    }
}
