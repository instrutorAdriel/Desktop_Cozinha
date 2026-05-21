package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.Historico;
import com.example.desktop_cozinha.model.HistoricoDAO;
import com.example.desktop_cozinha.model.HomeDAO;
import com.example.desktop_cozinha.services.SessaoService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoricoController {
    @FXML private Button btnbuscar;
    @FXML private TextField txtpesquisa;
    @FXML private TableView<Historico> tableHistorico;
    @FXML private TableColumn<Historico, String> nome_produto;
    @FXML private TableColumn<Historico, String> nome_usuario;
    @FXML private TableColumn<Historico, String> tipo_movimentacao;
    @FXML private TableColumn<Historico, Integer> quantidade;
    @FXML private TableColumn<Historico, String> tipo_estoque;
    @FXML private TableColumn<Historico, String> data_hora;
    @FXML private ChoiceBox<String> filtro;
    @FXML private Button btnsair;
    @FXML private DatePicker dataDE;
    @FXML private DatePicker dataATE;
    @FXML private Button btnbuscardata;
    @FXML private Label usuarioLabel;
    @FXML private Label dataLabel;
    @FXML private Label horaLabel;

    @FXML
    public void initialize() {
        configurarRelogio();
        usuarioAtual();
        configurarColunas();

        // 1. Adiciona as opções visíveis para o usuário (incluindo "Todos")
        filtro.getItems().addAll("Todos", "Entrada", "Saída", "Descarte");
// 2. Define "Todos" como valor padrão inicial
        filtro.setValue("Todos");
        // 3. Ovinte para disparar a busca sempre que o usuário mudar o filtro    filtro.setOnAction(event -> realizarBusca());
        // Executa a busca automática sempre que mudar o ChoiceBox
        filtro.valueProperty().addListener((observable, oldValue, newValue) -> filtrarHistorico());

        // Busca inicial carregando tudo
        filtrarHistorico();
    }

    private void configurarColunas() {
        data_hora.setCellValueFactory(new PropertyValueFactory<>("data_hora"));
        nome_produto.setCellValueFactory(new PropertyValueFactory<>("nome_produto"));
        tipo_movimentacao.setCellValueFactory(new PropertyValueFactory<>("tipo_movimentacao"));
        quantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        tipo_estoque.setCellValueFactory(new PropertyValueFactory<>("tipo_estoque"));
        nome_usuario.setCellValueFactory(new PropertyValueFactory<>("nome_usuario"));
    }

    public String choiceboxmarcado() {
        if (filtro.getValue() == null || filtro.getValue().equals("Todos")) {
            return "Todos";
        }
        return filtro.getValue(); // Retorna "Entrada" ou "Saída" direto
    }

    /**
     * Método centralizador. Ele lê os inputs de texto, tipo e data e atualiza a tabela.
     */
    @FXML
    public void filtrarHistorico() {
        String pesquisa = txtpesquisa.getText();
        String tipoFiltro = choiceboxmarcado();

        String stringDe = null;
        String stringAte = null;

        // Se ambas as datas estiverem preenchidas, nós passamos para o filtro
        if (dataDE.getValue() != null && dataATE.getValue() != null) {
            stringDe = String.valueOf(dataDE.getValue());
            stringAte = String.valueOf(dataATE.getValue());
        }

        // O DAO resolve a combinação de filtros que estiver ativa de forma limpa
        List<Historico> resultado = HistoricoDAO.filtrar(pesquisa, tipoFiltro, stringDe, stringAte);
        atualizarTabela(resultado);
    }

    /**
     * Ação do botão de buscar data. Valida o preenchimento e dispara o filtro geral.
     */


    private void atualizarTabela(List<Historico> lista) {
        ObservableList<Historico> observableList = FXCollections.observableArrayList(lista);
        tableHistorico.setItems(observableList);
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

    @FXML
    public void botaoSair() throws IOException {
        MainApplication.sair();
    }

    public void onClickHome() throws IOException {
        MainApplication.trocadorDeTelas("home.fxml");
    }

    public void onClickEstoque() throws IOException {
        MainApplication.trocadorDeTelas("lista-estoque.fxml");
    }

}