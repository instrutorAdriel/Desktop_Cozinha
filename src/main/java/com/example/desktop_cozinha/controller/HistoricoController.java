package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.Historico;
import com.example.desktop_cozinha.model.HistoricoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HistoricoController {
    @FXML private Button btnbuscar;
    @FXML private TextField txtpesquisa;
    @FXML private TableView<Historico> tableHistorico;
    @FXML private TableColumn<Historico, String> nome_produto;
    @FXML private TableColumn<Historico, String> nome_usuario;
    @FXML private TableColumn<Historico, String> tipo_movimentacao;
    @FXML private TableColumn<Historico, Integer> quantidade;
    @FXML private TableColumn<Historico, String> observacao;
    @FXML private TableColumn<Historico, String> data_hora;
    @FXML private ChoiceBox<String> filtro;
    @FXML private Button btnsair;
    @FXML private DatePicker dataDE;
    @FXML private DatePicker dataATE;
    @FXML private Button btnbuscardata;

    @FXML
    public void initialize() {
        configurarColunas();
        // Adicionei "Produto" nas opções para bater com a busca nova
        filtro.getItems().addAll("Ambos", "Entrada", "Saída");
        filtro.setValue("Ambos");

        // ADICIONE ISSO: Escuta as mudanças no ChoiceBox
        filtro.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                filtrarHistorico(); // Chama o método de busca automaticamente
            }
        });

        filtrarHistorico();
    }

    private void configurarColunas() {
        data_hora.setCellValueFactory(new PropertyValueFactory<>("data_hora"));
        nome_produto.setCellValueFactory(new PropertyValueFactory<>("nome_produto"));
        tipo_movimentacao.setCellValueFactory(new PropertyValueFactory<>("tipo_movimentacao"));
        quantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade_movimentada"));
        observacao.setCellValueFactory(new PropertyValueFactory<>("observacao"));
        nome_usuario.setCellValueFactory(new PropertyValueFactory<>("nome_usuario"));
    }

    public String choiceboxmarcado(){
        if (filtro.getValue() == "Ambos") {
            IO.println("entrou");
            return "Ambos";

        }
        else  if (filtro.getValue() == "Entrada") {
            IO.println("entrou la ele ");
            return "Entrada";
        }
        else {
            return "Saída";
        }
    }

    @FXML
    public void filtrarHistorico() {
        String tipoFiltro = filtro.getValue().toLowerCase();
        String pesquisa = txtpesquisa.getText();
        List<Historico> resultado;

        // Se houver texto no campo de pesquisa, prioriza a busca por produto
        if (pesquisa != null && !pesquisa.isEmpty()) {
            filtro.setValue(tipoFiltro);
            resultado = HistoricoDAO.buscarPorProduto(pesquisa);
        } else {
            // Caso contrário, usa o filtro do ChoiceBox
            resultado = switch (tipoFiltro) {
                case "saída" -> HistoricoDAO.imprimirHistoricoporsaida();
                case "entrada" -> HistoricoDAO.imprimirHistoricoporentrada();
                default -> HistoricoDAO.imprimirHistoricoCompleto();
            };
        }

        atualizarTabela(resultado);
    }

    private void atualizarTabela(List<Historico> lista) {
        ObservableList<Historico> observableList = FXCollections.observableArrayList(lista);
        tableHistorico.setItems(observableList);
    }
    @FXML
    public void botaoSair () throws IOException {
        MainApplication.sair();
    }
    @FXML
    public void buscarDataHora () throws  IOException {
        String de = String.valueOf(dataDE.getValue());
        String ate = String.valueOf(dataATE.getValue());
        List<Historico> resultado;


        if (dataDE.getValue() == null || dataATE.getValue() == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Alerta de busca");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor insira as datas de busca");
            alerta.showAndWait();
        }
        else {
            resultado = HistoricoDAO.buscarPorData(de,ate);
            atualizarTabela(resultado);

        }
    }
}