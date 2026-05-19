package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.Notificacao;
import com.example.desktop_cozinha.model.ProductDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class notificacaoController {

    private static final int DIAS_AVISO_VALIDADE = 7;

    // --- Injeção da tabela e colunas ---
    @FXML private TableView<Notificacao> tabelaNotificacao;
    @FXML private TableColumn<Notificacao, Integer> colId;
    @FXML private TableColumn<Notificacao, String>  colNome;
    @FXML private TableColumn<Notificacao, String>  colTipo;
    @FXML private TableColumn<Notificacao, Integer> colQuantidade;
    @FXML private TableColumn<Notificacao, String>  colUnidade;
    @FXML private TableColumn<Notificacao, String>  colValidade;
    @FXML private TableColumn<Notificacao, Integer> colEstoqueMin;
    @FXML private TableColumn<Notificacao, Button> colStatus;

    // --- Chamado automaticamente ao abrir a tela ---
    @FXML
    public void initialize() throws SQLException {
        configurarColunas();
        AtualizaTabela();
        EstoqueGeral();
    }

    private void configurarColunas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade_atual"));
        colUnidade.setCellValueFactory(new PropertyValueFactory<>("unidade_medida"));
        colValidade.setCellValueFactory(new PropertyValueFactory<>("data_validade"));
        colEstoqueMin.setCellValueFactory(new PropertyValueFactory<>("estoque_minimo"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @FXML
    public void AtualizaTabela() throws SQLException {
        List<Notificacao> itens = ProductDAO.relatorioProduto();
        ObservableList<Notificacao> lista = FXCollections.observableArrayList(itens);
        tabelaNotificacao.setItems(lista);
    }

    @FXML
    public void EstoqueGeral() throws SQLException {
        List<Notificacao> itens = ProductDAO.relatorioProduto();

        StringBuilder mensagemEstoque = new StringBuilder();
        StringBuilder mensagemValidade = new StringBuilder();

        LocalDate hoje = LocalDate.now();

        for (Notificacao item : itens) {

            // IF 1: Estoque baixo
            if (item.getQuantidade_atual() <= item.getEstoque_minimo()) {
                mensagemEstoque.append("• ").append(item.getNomeProduto())
                        .append(" — ").append(item.getQuantidade_atual())
                        .append(" ").append(item.getUnidade_medida())
                        .append(" (mínimo: ").append(item.getEstoque_minimo()).append(")\n");
            }

            // IF 2 e 3: Validade
            try {
                LocalDate validade = LocalDate.parse(item.getData_validade());
                long diasRestantes = ChronoUnit.DAYS.between(hoje, validade);

                // IF 2: Perto de vencer
                if (diasRestantes >= 0 && diasRestantes <= DIAS_AVISO_VALIDADE) {
                    mensagemValidade.append("• ").append(item.getNomeProduto())
                            .append(colStatus)
                            .append(" — vence em ").append(diasRestantes).append(" dia(s)\n");

                }

                // IF 3: Já vencido
                if (diasRestantes < 0) {
                    mensagemValidade.append("• ").append(item.getNomeProduto())
                            .append(" — VENCIDO há ").append(Math.abs(diasRestantes)).append(" dia(s)\n");
                }

            } catch (Exception e) {
                System.out.println("Data inválida para o produto: " + item.getNomeProduto());
            }
        }

        if (!mensagemEstoque.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Estoque Baixo");
            alert.setHeaderText("Produtos com estoque abaixo do mínimo:");
            alert.setContentText(mensagemEstoque.toString());
            alert.showAndWait();
        }

        if (!mensagemValidade.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Atenção — Validade");
            alert.setHeaderText("Produtos próximos do vencimento ou vencidos:");
            alert.setContentText(mensagemValidade.toString());
            alert.showAndWait();
        }

        if (mensagemEstoque.isEmpty() && mensagemValidade.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Tudo OK");
            alert.setHeaderText(null);
            alert.setContentText("Nenhum produto com problema encontrado.");
            alert.showAndWait();
        }
    }

    public void OnNotificacaoClick() throws Exception {
        MainApplication.trocadorDeTelas("noti.fxml");
    }
}
