package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.User;
import com.sigec.system.sigec.DAOS.RelatorioInsumosDAO;
import com.sigec.system.sigec.DAOS.RelatorioInsumosDAO.ItemCompra;
import com.sigec.system.sigec.DAOS.RelatorioInsumosDAO.Opcao;
import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Services.SessaoService;
import com.sigec.system.sigec.Utils.BackgroundAnimator;
import com.sigec.system.sigec.Utils.PdfSigec;
import com.sigec.system.sigec.Utils.RelatorioComprasPdf;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class HistoricoController {

    private static final String MSG_INICIAL =
            "Selecione a unidade e o período e clique em \"Gerar Lista de Compras\".";

    private final RelatorioInsumosDAO relatorioDAO = new RelatorioInsumosDAO();
    private final DecimalFormat formato = new DecimalFormat("#,##0.###",
            DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR")));

    private String nomeUsuario = "Usuário";

    /** Cada geração ou mudança de filtro incrementa; resultado de número antigo é descartado. */
    private long geracaoAtual = 0;

    /** Texto original do botão Gerar (restaurado ao fim do carregamento). */
    private String textoBotaoGerar;

    /** Filtros da última geração: o PDF sai exatamente com o que está na tabela. */
    private PdfSigec.Filtros ultimosFiltros;

    @FXML private AnchorPane topBarPane;
    @FXML private Label dataLabel;
    @FXML private Label horaLabel;
    @FXML private Label usuarioLabel;

    @FXML private VBox submenuEstoque;
    @FXML private Label setaEstoque;
    @FXML private Button btnNavEstoque;

    @FXML private Button btnNavCadastro;
    @FXML private VBox submenuCadastro;
    @FXML private Label setaCadastro;

    @FXML private ChoiceBox<Opcao> cbUnidade;

    @FXML private DatePicker dataInicio;
    @FXML private DatePicker dataFim;

    @FXML private Button btnGerarLista;
    @FXML private Button btnSalvarPdf;
    @FXML private Label lblResumo;

    @FXML private TableView<ItemCompra> tabelaCompras;
    @FXML private TableColumn<ItemCompra, String> colCodigo;
    @FXML private TableColumn<ItemCompra, String> colCategoria;
    @FXML private TableColumn<ItemCompra, String> colInsumo;
    @FXML private TableColumn<ItemCompra, Double> colNecessario;
    @FXML private TableColumn<ItemCompra, Double> colEstoque;
    @FXML private TableColumn<ItemCompra, Double> colComprar;
    @FXML private TableColumn<ItemCompra, String> colUnidade;

    @FXML
    public void initialize() {
        if (topBarPane != null) {
            BackgroundAnimator.startTopBarAnimation(topBarPane);
        }
        iniciarRelogio();
        carregarUsuarioLogado();
        configurarTabela();
        configurarFiltros();
    }

    // ==================================================================
    // Inicialização
    // ==================================================================
    private void iniciarRelogio() {
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (dataLabel != null) {
            dataLabel.setText(LocalDate.now().format(dataFormatter));
        }
        if (horaLabel != null) {
            DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            Timeline clock = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> horaLabel.setText(LocalTime.now().format(horaFormatter))),
                    new KeyFrame(Duration.seconds(1)));
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();
        }
    }

    private void carregarUsuarioLogado() {
        User usuario = SessaoService.getUsuarioLogado();

        if (usuario != null && usuario.getNome() != null && !usuario.getNome().isBlank()) {
            nomeUsuario = usuario.getNome().trim();
        } else if (SessaoService.getIdUsuarioLogado() > 0) {
            try {
                String nome = relatorioDAO.buscarNomeUsuario(SessaoService.getIdUsuarioLogado());
                nomeUsuario = (nome != null && !nome.isBlank()) ? nome : SessaoService.getNomeExibicao();
            } catch (SQLException e) {
                System.err.println("Não foi possível carregar o nome do usuário: " + e.getMessage());
                nomeUsuario = SessaoService.getNomeExibicao();
            }
        } else {
            nomeUsuario = SessaoService.getNomeExibicao(); // sem sessão: "Administrador"
        }

        if (usuarioLabel != null) {
            usuarioLabel.setText(nomeUsuario);
        }
    }

    private void configurarTabela() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().codigoMxm() == null ? "—" : c.getValue().codigoMxm()));
        colCategoria.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().categoria()));
        colInsumo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nomeProduto()));
        colNecessario.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().necessario()));
        colEstoque.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().emEstoque()));
        colComprar.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().aComprar()));
        colUnidade.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().unidade()));

        // Com CONSTRAINED_RESIZE_POLICY, o maxWidth funciona como "peso" de cada coluna.
        colCodigo.setMaxWidth(1f * Integer.MAX_VALUE * 12);
        colCategoria.setMaxWidth(1f * Integer.MAX_VALUE * 14);
        colInsumo.setMaxWidth(1f * Integer.MAX_VALUE * 28);
        colNecessario.setMaxWidth(1f * Integer.MAX_VALUE * 12);
        colEstoque.setMaxWidth(1f * Integer.MAX_VALUE * 12);
        colComprar.setMaxWidth(1f * Integer.MAX_VALUE * 12);
        colUnidade.setMaxWidth(1f * Integer.MAX_VALUE * 10);

        colNecessario.setCellFactory(col -> celulaNumero(false));
        colEstoque.setCellFactory(col -> celulaNumero(false));
        colComprar.setCellFactory(col -> celulaNumero(true));

        tabelaCompras.setPlaceholder(new Label(MSG_INICIAL));

        // O botão de PDF só fica ativo quando há itens na tabela
        // (a lista é sempre a mesma: setAll/clear, então o vínculo continua valendo)
        btnSalvarPdf.disableProperty().bind(Bindings.isEmpty(tabelaCompras.getItems()));
    }

    /**
     * Número no padrão brasileiro (1.200 / 0,5). Na coluna "A comprar":
     * valor > 0 em destaque (classe valor-comprar) e zero como "—" (classe valor-ok).
     */
    private TableCell<ItemCompra, Double> celulaNumero(boolean colunaComprar) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean vazio) {
                super.updateItem(valor, vazio);
                getStyleClass().removeAll("valor-comprar", "valor-ok");
                if (vazio || valor == null) {
                    setText(null);
                    return;
                }
                if (colunaComprar && valor <= 0.0005) {
                    setText("—");
                    getStyleClass().add("valor-ok");
                } else {
                    setText(formato.format(valor));
                    if (colunaComprar) {
                        getStyleClass().add("valor-comprar");
                    }
                }
            }
        };
    }

    private void configurarFiltros() {
        // Mostra o nome completo ao passar o mouse (nomes longos de unidade)
        configurarTooltip(cbUnidade);

        carregarUnidades();

        // Qualquer mudança de filtro invalida o resultado que está na tela
        cbUnidade.getSelectionModel().selectedItemProperty().addListener((obs, a, n) -> limparTabela());
        dataInicio.valueProperty().addListener((obs, a, n) -> limparTabela());
        dataFim.valueProperty().addListener((obs, a, n) -> limparTabela());
    }

    private void configurarTooltip(ChoiceBox<Opcao> choiceBox) {
        choiceBox.valueProperty().addListener((obs, antigo, novo) ->
                choiceBox.setTooltip(novo == null ? null : new Tooltip(novo.nome())));
    }

    // ==================================================================
    // Filtro de unidade
    // ==================================================================
    private void carregarUnidades() {
        try {
            cbUnidade.setItems(FXCollections.observableArrayList(relatorioDAO.listarUnidades()));
            // Se o usuário só tem uma unidade, já deixa selecionada
            if (cbUnidade.getItems().size() == 1) {
                cbUnidade.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            mostrarErro("Não foi possível carregar as unidades.", e);
        }
    }

    // ==================================================================
    // Lista de compras
    // ==================================================================
    @FXML
    public void onGerarRelatorio(ActionEvent event) {
        Opcao unidade = cbUnidade.getValue();
        LocalDate inicio = dataInicio.getValue();
        LocalDate fim = dataFim.getValue();

        if (unidade == null) {
            mostrarAviso("Selecione a unidade.");
            return;
        }
        if (inicio == null || fim == null) {
            mostrarAviso("Selecione a data inicial e a data final.");
            return;
        }
        if (fim.isBefore(inicio)) {
            mostrarAviso("A data final não pode ser anterior à data inicial.");
            return;
        }

        int idUnidade = unidade.id();

        // A consulta roda em outra thread; a tela continua respondendo
        Task<List<ItemCompra>> tarefa = new Task<>() {
            @Override
            protected List<ItemCompra> call() throws Exception {
                // Só banco aqui: nada de tela fora da thread do JavaFX
                // Lista da unidade inteira: sem filtro de curso/turma
                return relatorioDAO.buscarListaDeCompras(inicio, fim, idUnidade, null, null);
            }
        };

        long minhaGeracao = ++geracaoAtual;
        mostrarCarregando(true);

        tarefa.setOnSucceeded(e -> {
            if (minhaGeracao != geracaoAtual) {
                return; // filtros mudaram durante a consulta: resultado descartado
            }
            mostrarCarregando(false);
            List<ItemCompra> itens = tarefa.getValue();
            tabelaCompras.getItems().setAll(itens);
            // Curso/turma não se aplicam a este relatório (o PDF não mostra esses campos)
            ultimosFiltros = new PdfSigec.Filtros(unidade.nome(), null, null, inicio, fim, nomeUsuario);
            atualizarResumo(itens);
            if (itens.isEmpty()) {
                tabelaCompras.setPlaceholder(new Label("Nenhuma aula agendada nesta unidade no período."));
            }
        });

        tarefa.setOnFailed(e -> {
            if (minhaGeracao != geracaoAtual) {
                return;
            }
            limparTabela();
            Throwable erro = tarefa.getException();
            mostrarErro("Não foi possível gerar a lista de compras.",
                    erro instanceof Exception ex ? ex : new Exception(erro));
        });

        Thread thread = new Thread(tarefa, "lista-compras");
        thread.setDaemon(true); // não segura o fechamento do sistema
        thread.start();
    }

    private void atualizarResumo(List<ItemCompra> itens) {
        if (itens.isEmpty()) {
            lblResumo.setText("");
            return;
        }
        long comprar = itens.stream().filter(ItemCompra::precisaComprar).count();
        lblResumo.setText(comprar == 0
                ? "Estoque suficiente para todas as aulas"
                : comprar + " de " + itens.size() + " itens precisam ser comprados");
    }

    /** Liga/desliga o estado de carregamento: botão travado + spinner laranja na tabela. */
    private void mostrarCarregando(boolean carregando) {
        if (textoBotaoGerar == null) {
            textoBotaoGerar = btnGerarLista.getText();
        }
        btnGerarLista.setDisable(carregando);
        btnGerarLista.setText(carregando ? "Gerando lista..." : textoBotaoGerar);

        if (carregando) {
            tabelaCompras.getItems().clear();
            lblResumo.setText("");
            tabelaCompras.setPlaceholder(criarIndicadorCarregando());
        }
    }

    private VBox criarIndicadorCarregando() {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.getStyleClass().add("loader-laranja");
        spinner.setPrefSize(40, 40);

        Label texto = new Label("Comparando com o estoque...");
        texto.getStyleClass().add("loader-texto");

        VBox caixa = new VBox(10, spinner, texto);
        caixa.setAlignment(Pos.CENTER);
        return caixa;
    }

    // ==================================================================
    // Exportação em PDF (só os itens que precisam ser comprados)
    // ==================================================================
    @FXML
    public void onSalvarPdf(ActionEvent event) {
        if (tabelaCompras.getItems().isEmpty() || ultimosFiltros == null) {
            mostrarAviso("Gere a lista de compras antes de salvar o PDF.");
            return;
        }
        if (tabelaCompras.getItems().stream().noneMatch(ItemCompra::precisaComprar)) {
            mostrarInfo("O estoque da unidade é suficiente para todas as aulas do período.\n"
                    + "Não há itens para comprar.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar lista de compras em PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo PDF (*.pdf)", "*.pdf"));
        chooser.setInitialDirectory(PdfSigec.pastaRelatorios()); // Documentos\SIGEC\Relatórios
        chooser.setInitialFileName(PdfSigec.nomeArquivo("lista-compras", ultimosFiltros));

        File destino = chooser.showSaveDialog(tabelaCompras.getScene().getWindow());
        if (destino == null) {
            return; // usuário cancelou
        }
        if (!destino.getName().toLowerCase().endsWith(".pdf")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".pdf");
        }

        try {
            RelatorioComprasPdf.gerar(destino, ultimosFiltros, List.copyOf(tabelaCompras.getItems()));
        } catch (Exception e) {
            mostrarErro("Não foi possível salvar o PDF. Verifique se o arquivo não está aberto em outro programa.", e);
            return;
        }

        Alert ok = new Alert(Alert.AlertType.CONFIRMATION,
                "PDF salvo em:\n" + destino.getAbsolutePath() + "\n\nDeseja abrir o arquivo agora?",
                ButtonType.YES, ButtonType.NO);
        ok.setHeaderText(null);
        ok.setTitle("Lista de compras salva");
        Optional<ButtonType> resposta = ok.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.YES) {
            abrirArquivo(destino);
        }
    }

    /** Abre o PDF no leitor padrão do Windows, fora da thread da interface. */
    private void abrirArquivo(File arquivo) {
        Thread t = new Thread(() -> {
            try {
                java.awt.Desktop.getDesktop().open(arquivo);
            } catch (Exception e) {
                System.err.println("Não foi possível abrir o PDF: " + e.getMessage());
            }
        }, "abrir-pdf");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void onLimparFiltros(ActionEvent event) {
        cbUnidade.getSelectionModel().clearSelection();
        dataInicio.setValue(null);
        dataFim.setValue(null);
        limparTabela();
    }

    private void limparTabela() {
        geracaoAtual++;            // invalida uma consulta que ainda esteja rodando
        mostrarCarregando(false);
        ultimosFiltros = null;
        tabelaCompras.getItems().clear();
        lblResumo.setText("");
        tabelaCompras.setPlaceholder(new Label(MSG_INICIAL));
    }

    private void mostrarAviso(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensagem, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.setTitle("Atenção");
        alerta.showAndWait();
    }

    private void mostrarInfo(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensagem, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.setTitle("Lista de compras");
        alerta.showAndWait();
    }

    private void mostrarErro(String mensagem, Exception e) {
        e.printStackTrace();
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensagem + "\n\nDetalhe: " + e.getMessage(), ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.setTitle("Erro");
        alerta.showAndWait();
    }

    // ==================================================================
    // Navegação (mantida como estava nesta tela)
    // ==================================================================
    @FXML public void onClickHome(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("home.fxml"); } catch (IOException e) { mostrarErro("Erro ao navegar.", e); }
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
    @FXML public void onClickCadastroUsuario(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("cadastro.fxml"); } catch (IOException e) { mostrarErro("Erro ao navegar.", e); }
    }
    @FXML public void onClickCadastroTurma(ActionEvent event) {
        try { MainApplication.trocadorDeTelas("cadastro-turma.fxml"); } catch (IOException e) { mostrarErro("Erro ao navegar.", e); }
    }
    @FXML public void botaoSairAction(ActionEvent event) {
        com.sigec.system.sigec.Utils.SidebarNavigationManager.executarLogout();
    }
}