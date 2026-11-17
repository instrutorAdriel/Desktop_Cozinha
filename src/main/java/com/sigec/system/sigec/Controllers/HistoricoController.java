package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.UnidadeDTO;
import com.sigec.system.sigec.Constructors.CursoDTO;
import com.sigec.system.sigec.DAOS.HistoryDAO;
import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Constructors.User;
import com.sigec.system.sigec.Services.SessaoService;
import com.sigec.system.sigec.Utils.BackgroundAnimator;
import com.sigec.system.sigec.Utils.ButtonBorderLapAnimator;
import com.sigec.system.sigec.Utils.DropdownBorderLapAnimator;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class HistoricoController implements Initializable {

    private static final DateTimeFormatter DATE_PICKER_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    @FXML private AnchorPane topBarPane;
    @FXML private Label dataLabel;
    @FXML private Label usuarioLabel;
    @FXML private Label horaLabel;
    @FXML private TextField txtpesquisa;
    @FXML @SuppressWarnings("unused") private Button btnbuscardata;
    @FXML @SuppressWarnings("unused") private DatePicker dataDE;
    @FXML @SuppressWarnings("unused") private DatePicker dataATE;
    @FXML private ChoiceBox<String> filtro;
    @FXML private ChoiceBox<UnidadeDTO> cbUnidade;
    @FXML private ChoiceBox<CursoDTO> cbCurso;

    @FXML private TableView<HistoryDAO> tableHistorico;
    @FXML private TableColumn<HistoryDAO, String> codMXM;
    @FXML private TableColumn<HistoryDAO, String> nome_produto;
    @FXML private TableColumn<HistoryDAO, String> tipo_estoque;
    @FXML private TableColumn<HistoryDAO, String> quantidade;
    @FXML private TableColumn<HistoryDAO, String> qtdcompra;
    private final AtomicLong requisicaoTabela = new AtomicLong();

    @FXML private Button btnNavCadastro;
    @FXML private VBox submenuCadastro;
    @FXML private Label setaCadastro;
    @FXML @SuppressWarnings("unused") private Button botaoSair;
    @FXML private VBox submenuEstoque;
    @FXML private Label setaEstoque;
    @FXML private Button btnNavEstoque;
    @FXML @SuppressWarnings("unused") private Button btnExportarCompras;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (topBarPane != null) {
            BackgroundAnimator.startTopBarAnimation(topBarPane);
        }
        configurarDataHora();
        configurarUsuario();
        configurarFiltroCategorias();
        configurarDatePicker(dataDE);
        configurarDatePicker(dataATE);

        configurarTabela();

        if (cbCurso != null) {
            cbCurso.setConverter(new StringConverter<>() {
                @Override public String toString(CursoDTO curso) { return curso != null ? curso.getNome() : ""; }
                @Override public CursoDTO fromString(String string) { return null; }
            });
            cbCurso.getSelectionModel().selectedItemProperty().addListener(
                    (obs, antigo, novo) -> atualizarTabela());
        }

        if (cbUnidade != null) {
            User usuarioLogado = SessaoService.getUsuarioAtual();
            List<UnidadeDTO> unidades = usuarioLogado == null || usuarioLogado.getIdUsuario() <= 0
                    ? List.of()
                    : HistoryDAO.listarUnidadesDoUsuario(usuarioLogado.getIdUsuario());
            cbUnidade.setItems(FXCollections.observableArrayList(unidades));
            cbUnidade.setConverter(new StringConverter<>() {
                @Override public String toString(UnidadeDTO unidade) { return unidade != null ? unidade.getNome() : ""; }
                @Override public UnidadeDTO fromString(String string) { return null; }
            });
            cbUnidade.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
                carregarCursos();
            });
            cbUnidade.getSelectionModel().selectFirst();
        }

        if (dataDE != null) {
            dataDE.valueProperty().addListener((obs, antiga, nova) -> atualizarTabela());
        }
        if (dataATE != null) {
            dataATE.valueProperty().addListener((obs, antiga, nova) -> atualizarTabela());
        }
        atualizarTabela();
    }

    private void configurarDatePicker(DatePicker datePicker) {
        if (datePicker == null) return;

        datePicker.setEditable(true);
        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate data) {
                return data == null ? "" : DATE_PICKER_FORMATTER.format(data);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.isBlank()) return null;
                try {
                    return LocalDate.parse(texto, DATE_PICKER_FORMATTER);
                } catch (DateTimeParseException e) {
                    return datePicker.getValue();
                }
            }
        });
        datePicker.getEditor().setTextFormatter(new TextFormatter<String>(change -> {
            String texto = change.getControlNewText();
            if (texto.length() > 10) return null;

            for (int i = 0; i < texto.length(); i++) {
                char caractere = texto.charAt(i);
                boolean separador = i == 2 || i == 5;
                if (separador ? caractere != '/' : caractere < '0' || caractere > '9') {
                    return null;
                }
            }

            if (texto.length() >= 2) {
                int dia = Integer.parseInt(texto.substring(0, 2));
                if (dia < 1 || dia > 31) return null;
            }
            if (texto.length() >= 5) {
                int mes = Integer.parseInt(texto.substring(3, 5));
                if (mes < 1 || mes > 12) return null;
            }
            if (texto.length() == 10) {
                try {
                    LocalDate.parse(texto, DATE_PICKER_FORMATTER);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
            return change;
        }));
        datePicker.getEditor().focusedProperty().addListener((observable, antigo, focado) -> {
            if (!focado) {
                try {
                    LocalDate dataDigitada = LocalDate.parse(
                            datePicker.getEditor().getText(), DATE_PICKER_FORMATTER);
                    datePicker.setValue(dataDigitada);
                } catch (DateTimeParseException e) {
                    datePicker.getEditor().setText(datePicker.getValue() == null
                            ? ""
                            : DATE_PICKER_FORMATTER.format(datePicker.getValue()));
                }
            }
        });
    }

    private void carregarCursos() {
        if (cbCurso == null) {
            atualizarTabela();
            return;
        }
        List<CursoDTO> cursos = new ArrayList<>();
        cursos.add(new CursoDTO(0, "Todos"));
        cursos.addAll(HistoryDAO.listarCursosAtivos());
        cbCurso.setItems(FXCollections.observableArrayList(cursos));
        cbCurso.getSelectionModel().selectFirst();
        atualizarTabela();
    }

    private void configurarTabela() {
        if (codMXM != null) codMXM.setCellValueFactory(new PropertyValueFactory<>("codMXM"));
        if (nome_produto != null) nome_produto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        if (tipo_estoque != null) tipo_estoque.setCellValueFactory(new PropertyValueFactory<>("nomeCategoria"));
        if (quantidade != null) {
            quantidade.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
                    formatarQuantidade(cellData.getValue().getQtdAtual(), cellData.getValue().getUnidadeMedida())));
        }
        if (qtdcompra != null) {
            qtdcompra.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
                    formatarQuantidade(cellData.getValue().getQtdComprar(), cellData.getValue().getUnidadeMedida())));
        }
    }

    public void carregarDadosTabela(int idUnidade) {
        if (tableHistorico == null) return;
        CursoDTO curso = cbCurso != null ? cbCurso.getValue() : null;
        carregarTabelaEmSegundoPlano(idUnidade, curso != null ? curso.getId() : 0, null, null, "");
    }

    @FXML
    public void onExportarComprasAction(ActionEvent event) {
        System.out.println("Iniciando a exportação de compras...");
    }

    private void configurarUsuario() {
        if (usuarioLabel != null) {
            usuarioLabel.setText(SessaoService.getNomeUsuarioLogado());
        }
    }

    private void configurarFiltroCategorias() {
        if (filtro != null) {
            filtro.setItems(FXCollections.observableArrayList("Todos", "Entrada", "Saída", "Ajuste", "Descarte"));
            filtro.getSelectionModel().selectFirst();
        }
    }

    private void configurarDataHora() {
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        if (dataLabel != null) dataLabel.setText(LocalDate.now().format(dataFormatter));

        if (horaLabel != null) {
            horaLabel.setText(LocalTime.now().format(horaFormatter));
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event ->
                    horaLabel.setText(LocalTime.now().format(horaFormatter))
            ));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
    }

    // =========================================================================
    // NAVEGAÇÃO
    // =========================================================================

    @FXML public void onClickHome(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("home.fxml"); } catch (IOException e) { exibirAlerta("Erro ao navegar: " + e.getMessage()); }
    }
    @FXML protected void botaoCalendarioAction(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("calendario.fxml"); } catch (IOException e) { System.err.println(e.getMessage()); }
    }
    @FXML public void onClickSolicitacoes(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("solicitacoes-extras.fxml"); } catch (IOException e) { System.err.println(e.getMessage()); }
    }
    @FXML public void toggleDropdownCadastro(ActionEvent event) {
        if (submenuCadastro != null) {
            boolean expandido = !submenuCadastro.isVisible();
            submenuCadastro.setVisible(expandido);
            submenuCadastro.setManaged(expandido);
            if (setaCadastro != null) setaCadastro.setText(expandido ? "▶" : "▼");
        }
    }
    @FXML public void toggleDropdownEstoque(ActionEvent event) {
        if (submenuEstoque != null && btnNavEstoque != null) {
            boolean expandido = !submenuEstoque.isVisible();
            if (setaEstoque != null) setaEstoque.setText(expandido ? "▲" : "▼");
            submenuEstoque.setVisible(expandido);
            submenuEstoque.setManaged(expandido);
        }
    }
    @FXML public void onClickInsumos(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("lista-estoque.fxml"); } catch (IOException e) { System.err.println(e.getMessage()); }
    }
    @FXML public void onClickUtensilios(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("gestao-utensilios.fxml"); } catch (IOException e) { System.err.println(e.getMessage()); }
    }
    @FXML public void onClickCadastroUsuario(ActionEvent event) { navegarParaCadastro(); }
    @FXML public void onClickCadastroTurma(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("cadastro-turma.fxml"); } catch (IOException e) { exibirAlerta(e.getMessage()); }
    }
    private void navegarParaCadastro() {
        try { MainApplication.trocadorDeTelas("cadastro.fxml"); } catch (IOException e) { exibirAlerta(e.getMessage()); }
    }
    @FXML public void botaoSairAction(ActionEvent event) {
        com.sigec.system.sigec.Utils.SidebarNavigationManager.executarLogout();
    }

    // =========================================================================
    // Os filtros de turma e período são opcionais e independentes.
    // =========================================================================

    @FXML
    public void filtrarHistorico(ActionEvent event) {
        atualizarTabela();
    }

    private void atualizarTabela() {
        if (tableHistorico == null) return;
        UnidadeDTO unidadeSelecionada = cbUnidade != null ? cbUnidade.getValue() : null;
        if (unidadeSelecionada == null) {
            requisicaoTabela.incrementAndGet();
            tableHistorico.setItems(FXCollections.observableArrayList());
            return;
        }
        int idUnidade = unidadeSelecionada.getId();

        CursoDTO cursoSelecionado = cbCurso != null ? cbCurso.getValue() : null;
        int idCurso = cursoSelecionado != null ? cursoSelecionado.getId() : 0;

        LocalDate dataInicio = dataDE != null ? dataDE.getValue() : null;
        LocalDate dataFim = dataATE != null ? dataATE.getValue() : null;

        String termo = txtpesquisa != null ? txtpesquisa.getText().trim() : "";

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            requisicaoTabela.incrementAndGet();
            exibirAlerta("A data inicial não pode ser superior à data final.");
            return;
        }

        LocalDate inicioConsulta = dataInicio != null ? dataInicio : dataFim;
        LocalDate fimConsulta = dataFim != null ? dataFim : dataInicio;
        carregarTabelaEmSegundoPlano(idUnidade, idCurso, inicioConsulta, fimConsulta, termo);
    }

    private void carregarTabelaEmSegundoPlano(
            int idUnidade, int idCurso, LocalDate dataInicio, LocalDate dataFim, String termo) {
        long requisicao = requisicaoTabela.incrementAndGet();
        Thread thread = new Thread(() -> {
            try {
                List<HistoryDAO> resultados = dataInicio != null || dataFim != null
                        ? HistoryDAO.listarComprasPorPeriodo(idUnidade, idCurso, dataInicio, dataFim)
                        : HistoryDAO.listarListaDeCompras(idUnidade, idCurso);

                if (!termo.isEmpty()) {
                    String termoLower = termo.toLowerCase();
                    resultados = resultados.stream().filter(item -> {
                        if (item.getNomeProduto() == null) return false;
                        String nomeProd = item.getNomeProduto().toLowerCase();
                        if (nomeProd.contains(termoLower)) return true;

                        int tolerancia = termoLower.length() > 5 ? 2 : 1;
                        return calcularDistanciaLevenshtein(nomeProd, termoLower) <= tolerancia;
                    }).collect(Collectors.toList());
                }

                List<HistoryDAO> resultadosFinais = resultados;
                Platform.runLater(() -> {
                    if (requisicaoTabela.get() == requisicao) {
                        tableHistorico.setItems(FXCollections.observableArrayList(resultadosFinais));
                    }
                });
            } catch (RuntimeException e) {
                Platform.runLater(() -> {
                    if (requisicaoTabela.get() == requisicao) {
                        exibirAlerta("Não foi possível carregar o relatório: " + e.getMessage());
                    }
                });
            }
        }, "SIGEC-RelatorioLoader");
        thread.setDaemon(true);
        thread.start();
    }

    private String formatarQuantidade(double valor, String unidade) {
        String quantidadeFormatada = BigDecimal.valueOf(valor).stripTrailingZeros().toPlainString();
        return unidade == null || unidade.isBlank()
                ? quantidadeFormatada
                : quantidadeFormatada + " " + unidade;
    }

    private int calcularDistanciaLevenshtein(String s1, String s2) {
        int[] custos = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int ultimoValor = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    custos[j] = j;
                } else {
                    if (j > 0) {
                        int novoValor = custos[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            novoValor = Math.min(Math.min(novoValor, ultimoValor), custos[j]) + 1;
                        }
                        custos[j - 1] = ultimoValor;
                        ultimoValor = novoValor;
                    }
                }
            }
            if (i > 0) custos[s2.length()] = ultimoValor;
        }
        return custos[s2.length()];
    }

    private void exibirAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}