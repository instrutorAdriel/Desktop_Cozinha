package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.model.Historico;
import com.example.desktop_cozinha.model.HistoricoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class HistoricoController {

    @FXML private TextField txtpesquisa;
    @FXML private TableView<Historico> tableHistorico; // Mudei o nome para não confundir com a classe
    @FXML private TableColumn<Historico, String> nome_produto;
    @FXML private TableColumn<Historico, String> nome_usuario;
    @FXML private TableColumn<Historico, String> tipo_movimentacao;
    @FXML private TableColumn<Historico, Integer> quantidade; // Quantidade costuma ser Integer
    @FXML private TableColumn<Historico, String> observacao;
    @FXML private TableColumn<Historico, String> data_hora;
    @FXML private ChoiceBox<String> filtro;

    @FXML
    public void initialize() {
        configurarColunas();
        filtro.getItems().addAll("Produto", "Usuário", "Tipo", "Data/Hora");
        filtro.setValue("Produto"); // Define um padrão para não dar erro de null

        // Opcional: Carregar a tabela ao abrir a tela
        filtrarHistorico();
    }

    private void configurarColunas() {
        // IMPORTANTE: O nome dentro de "" deve ser exatamente igual à variável na classe Historico
        nome_produto.setCellValueFactory(new PropertyValueFactory<>("nome_produto"));
        nome_usuario.setCellValueFactory(new PropertyValueFactory<>("nome_usuario"));
        tipo_movimentacao.setCellValueFactory(new PropertyValueFactory<>("tipo_movimentacao")); // Verifique se na classe Historico está assim
        quantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade_movimentada"));
        observacao.setCellValueFactory(new PropertyValueFactory<>("observacao"));
        data_hora.setCellValueFactory(new PropertyValueFactory<>("data_hora"));
    }

    @FXML
    public void filtrarHistorico() {
        String termoBusca = txtpesquisa.getText();
        String tipoFiltro = filtro.getValue();
        List<Historico> resultado;

        // Aqui está a correção: pegamos o retorno do DAO e usamos na lista
        if (termoBusca == null || termoBusca.isEmpty()) {
            resultado = HistoricoDAO.imprimirHistoricoCompleto();
        } else {
            switch (tipoFiltro) {
                case "Produto" -> resultado = HistoricoDAO.imprimirHistoricoporproduto(termoBusca);
                case "Usuário" -> resultado = HistoricoDAO.imprimirHistoricoporusuario(termoBusca);
                case "tipo_movimentacao"    -> resultado = HistoricoDAO.imprimirHistoricoportipo(termoBusca);
                case "Data/Hora" -> resultado = HistoricoDAO.imprimirHistoricopordata(termoBusca);
                default -> resultado = HistoricoDAO.imprimirHistoricoCompleto();
            }
        }

        atualizarTabela(resultado);
    }

    private void atualizarTabela(List<Historico> lista) {
        ObservableList<Historico> observableList = FXCollections.observableArrayList(lista);
        tableHistorico.setItems(observableList);
    }
}