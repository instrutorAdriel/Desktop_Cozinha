package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.DAOS.UserDAO;
import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Services.SessaoService;
import com.sigec.system.sigec.Utils.*;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import java.util.function.UnaryOperator;

public class CadastroController implements Initializable {

    @FXML
    private AnchorPane topBarPane;
    @FXML
    private Label dataLabel;
    @FXML
    private Label usuarioLabel;
    @FXML
    private Label horaLabel;
    @FXML
    private Button btnNavCadastro;
    @FXML
    private VBox submenuCadastro;
    @FXML
    private Label setaCadastro;
    @FXML
    private Button btnSubNavUsuario;
    @FXML
    private Button btnSubNavTurma;
    @FXML
    private Button botaoSair;

    @FXML
    private VBox submenuEstoque;
    @FXML
    private Button btnNavEstoque;
    @FXML
    private Label setaEstoque;

    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtAcesso;
    @FXML
    private ChoiceBox<String> acessoSelect;
    @FXML
    private ChoiceBox<String> unidadeSelect;

    @FXML
    private PasswordField txtSenha;

    // --- CAMPOS DO OLHO (SENHA) ---
    @FXML
    private TextField txtSenhaRevelada;
    @FXML
    private ToggleButton btnMostrarSenha;

    @FXML
    private PasswordField txtConfirmarSenha;

    // --- CAMPOS DO OLHO (CONFIRMAR SENHA) ---
    @FXML
    private TextField txtConfirmarSenhaRevelada;
    @FXML
    private ToggleButton btnMostrarConfirmarSenha;

    @FXML
    private Button btnCadastrar;
    @FXML
    private Button btnLimpar;

    @FXML
    private VBox loadingOverlay;

    // --- MENSAGENS DE ERRO DENTRO DOS CAMPOS ---
    // Os estilos ficam no arquivo CSS (classes "campo-erro" e "mensagem-erro-campo")
    private static final String CLASSE_CAMPO_ERRO = "campo-erro";
    private static final String CLASSE_MENSAGEM_ERRO = "mensagem-erro-campo";

    private Label erroNome;
    private Label erroEmail;

    // Caracteres permitidos em um e-mail segundo a RFC 5322 (parte local + @ + domínio)
    private static final Pattern EMAIL_CARACTERES_VALIDOS =
            Pattern.compile("[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~@-]*");

    // Formato conforme RFC 5322 (dot-atom): não aceita ponto no início/fim
    // nem dois pontos seguidos na parte local (ex.: ".jean", "jean.", "je..an")
    private static final Pattern EMAIL_RFC_5322 = Pattern.compile(
            "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@" +
                    "[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?" +
                    "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)*$");

    private static final String DOMINIO_INSTITUCIONAL = "@df.senac.br";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (topBarPane != null) {
            BackgroundAnimator.startTopBarAnimation(topBarPane);
        }

        configurarDataHora();
        configurarUsuario();
        configurarMenuInicial();

        // Sincroniza os textos entre os campos ocultos e revelados
        if (txtSenhaRevelada != null && txtSenha != null) {
            txtSenhaRevelada.textProperty().bindBidirectional(txtSenha.textProperty());
        }
        if (txtConfirmarSenhaRevelada != null && txtConfirmarSenha != null) {
            txtConfirmarSenhaRevelada.textProperty().bindBidirectional(txtConfirmarSenha.textProperty());
        }
        configurarRestricaoSenha(txtSenha);
        configurarRestricaoSenha(txtSenhaRevelada);
        configurarRestricaoSenha(txtConfirmarSenha);
        configurarRestricaoSenha(txtConfirmarSenhaRevelada);
        configurarNavegacaoCampos();
        configurarSeletorAcesso();
        configurarSeletorUnidade();
        configurarValidacaoVisualNome();
        configurarValidacaoVisualEmail();
    }

    // =====================================================================
    // MENSAGENS DE ERRO DENTRO DA CAIXA DE TEXTO
    // =====================================================================

    /**
     * Coloca o campo dentro de um StackPane (no mesmo lugar do layout) e adiciona
     * um Label por cima do campo, alinhado à direita. Assim a mensagem aparece
     * DENTRO da caixinha sem precisar alterar o FXML.
     * A aparência do label e da borda de erro vem do CSS.
     */
    private Label instalarMensagemNoCampo(TextField campo) {
        if (campo == null || !(campo.getParent() instanceof Pane pai)) {
            return null;
        }

        Label erro = new Label();
        erro.getStyleClass().add(CLASSE_MENSAGEM_ERRO);
        erro.setVisible(false);
        erro.setMouseTransparent(true); // o clique continua indo para o campo

        StackPane wrapper = new StackPane();
        // Copia as restrições de layout do pai (margens, anchors, grow, linha/coluna...)
        wrapper.getProperties().putAll(campo.getProperties());
        wrapper.setLayoutX(campo.getLayoutX());
        wrapper.setLayoutY(campo.getLayoutY());
        wrapper.setMinWidth(campo.getMinWidth());
        wrapper.setPrefWidth(campo.getPrefWidth());
        wrapper.setMaxWidth(campo.getMaxWidth());
        wrapper.setMinHeight(campo.getMinHeight());
        wrapper.setPrefHeight(campo.getPrefHeight());
        wrapper.setMaxHeight(campo.getMaxHeight());

        int indice = pai.getChildren().indexOf(campo);
        pai.getChildren().set(indice, wrapper);

        campo.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(erro, Pos.CENTER_RIGHT);
        StackPane.setMargin(erro, new Insets(0, 12, 0, 0));
        erro.maxWidthProperty().bind(wrapper.widthProperty().multiply(0.55));

        wrapper.getChildren().addAll(campo, erro);
        return erro;
    }

    private void mostrarErro(TextField campo, Label erro, String mensagem) {
        if (campo == null) {
            return;
        }
        if (!campo.getStyleClass().contains(CLASSE_CAMPO_ERRO)) {
            campo.getStyleClass().add(CLASSE_CAMPO_ERRO);
        }

        if (erro != null) {
            erro.setText(mensagem);
            erro.setVisible(true);
        } else {
            // Plano B: se não foi possível instalar o label, usa tooltip
            campo.setTooltip(new Tooltip(mensagem));
        }
    }

    private void ocultarErro(TextField campo, Label erro) {
        if (campo == null) {
            return;
        }
        campo.getStyleClass().remove(CLASSE_CAMPO_ERRO);

        if (erro != null) {
            erro.setVisible(false);
            erro.setText("");
        } else {
            campo.setTooltip(null);
        }
    }

    // ---------------- NOME ----------------

    private void configurarValidacaoVisualNome() {
        if (txtNome == null) {
            return;
        }
        erroNome = instalarMensagemNoCampo(txtNome);

        // Enquanto digita: avisa na hora se tiver número ou símbolo
        txtNome.textProperty().addListener((obs, antigo, novo) -> {
            if (novo == null || novo.isEmpty()) {
                ocultarErro(txtNome, erroNome);
            } else if (!novo.matches("[\\p{L} '\\-]*")) {
                mostrarErro(txtNome, erroNome, "Use apenas letras (sem números ou símbolos)");
            } else {
                ocultarErro(txtNome, erroNome);
            }
        });

        // Ao sair do campo: confere se tem nome e sobrenome
        txtNome.focusedProperty().addListener((obs, tinhaFoco, temFoco) -> {
            if (!temFoco && !txtNome.getText().isBlank()) {
                aplicarValidacaoNome();
            }
        });
    }

    /** Retorna a mensagem de erro do nome, ou null se estiver válido. */
    private String mensagemErroNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return "Preencha o nome completo";
        }
        if (!nome.matches("[\\p{L} '\\-]*")) {
            return "Use apenas letras (sem números ou símbolos)";
        }
        if (!validarNomeCompleto(nome.trim())) {
            return "Informe nome e sobrenome";
        }
        return null;
    }

    private boolean aplicarValidacaoNome() {
        String msg = mensagemErroNome(txtNome.getText());
        if (msg != null) {
            mostrarErro(txtNome, erroNome, msg);
            return false;
        }
        ocultarErro(txtNome, erroNome);
        return true;
    }

    // ---------------- E-MAIL ----------------

    private void configurarValidacaoVisualEmail() {
        if (txtEmail == null) {
            return;
        }
        erroEmail = instalarMensagemNoCampo(txtEmail);

        // Enquanto digita: só avisa de caracteres que a RFC 5322 não permite (ex.: espaço, acento)
        // (o resto é conferido ao sair do campo, senão daria erro antes de terminar de digitar)
        txtEmail.textProperty().addListener((obs, antigo, novo) -> {
            if (novo == null || novo.isEmpty()) {
                ocultarErro(txtEmail, erroEmail);
            } else if (!EMAIL_CARACTERES_VALIDOS.matcher(novo).matches()) {
                mostrarErro(txtEmail, erroEmail, "Caractere inválido (padrão RFC 5322)");
            } else {
                ocultarErro(txtEmail, erroEmail);
            }
        });

        // Ao sair do campo: valida o e-mail completo
        txtEmail.focusedProperty().addListener((obs, tinhaFoco, temFoco) -> {
            if (!temFoco && !txtEmail.getText().isBlank()) {
                aplicarValidacaoEmail();
            }
        });
    }

    /** Retorna a mensagem de erro do e-mail, ou null se estiver válido. */
    private String mensagemErroEmail(String email) {
        if (email == null || email.isBlank()) {
            return "Preencha o e-mail institucional";
        }
        String e = email.trim().toLowerCase();
        if (!EMAIL_CARACTERES_VALIDOS.matcher(e).matches()) {
            return "Caractere inválido (padrão RFC 5322)";
        }
        if (!e.contains("@")) {
            return "Falta o @ no e-mail";
        }
        if (!EMAIL_RFC_5322.matcher(e).matches()) {
            return "E-mail fora do padrão RFC 5322";
        }
        if (!e.endsWith(DOMINIO_INSTITUCIONAL)) {
            return "Use o domínio " + DOMINIO_INSTITUCIONAL;
        }
        return null;
    }

    private boolean aplicarValidacaoEmail() {
        String msg = mensagemErroEmail(txtEmail.getText());
        if (msg != null) {
            mostrarErro(txtEmail, erroEmail, msg);
            return false;
        }
        ocultarErro(txtEmail, erroEmail);
        return true;
    }

    // =====================================================================

    private void configurarRestricaoSenha(TextInputControl campo) {
        if (campo == null) return;

        UnaryOperator<TextFormatter.Change> filtro = change -> {
            String novoTexto = change.getControlNewText();

            if (novoTexto.matches(".*\\s.*") || novoTexto.length() > 25) {
                return null;
            }
            return change;
        };
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }

    private void configurarSeletorUnidade() {
        if (unidadeSelect != null) {

            unidadeSelect.getItems().clear();

            unidadeSelect.getItems().addAll(
                    "07 - CEP ENNIUS MUNIZ",
                    "08 - CEP JÓ - TAGUATINGA",
                    "09 - CEP TALAL CEILÂNDIA",
                    "10 - CEP JOAQUIM - GAMA",
                    "11 - CEP SOBRADINHO",
                    "12 - CEP MS GASTRONOMIA",
                    "24 - CEP AÇÕES MÓVEIS",
                    "25 - POLO SÃO SEBASTIÃO",
                    "26 - POLO PÁTIO BRASIL",
                    "27 - POLO BRAZLÂNDIA",
                    "28 - POLO SANTA MARIA",
                    "29 - POLO RECANTO",
                    "32 - POLO SENAC BELEZA",
                    "34 - CEP CORPORATIVO");

            unidadeSelect.setValue("34 - CEP CORPORATIVO");
        }
    }

    // --- MÉTODO DO OLHO (SENHA) ---
    @FXML
    public void toggleMostrarSenha() {
        if (btnMostrarSenha.isSelected()) {
            txtSenhaRevelada.setVisible(true);
            txtSenhaRevelada.setManaged(true);
            txtSenha.setVisible(false);
            txtSenha.setManaged(false);
        } else {
            txtSenha.setVisible(true);
            txtSenha.setManaged(true);
            txtSenhaRevelada.setVisible(false);
            txtSenhaRevelada.setManaged(false);
        }
    }

    // --- MÉTODO DO OLHO (CONFIRMAR SENHA) ---
    @FXML
    public void toggleMostrarConfirmarSenha() {
        if (btnMostrarConfirmarSenha.isSelected()) {
            txtConfirmarSenhaRevelada.setVisible(true);
            txtConfirmarSenhaRevelada.setManaged(true);
            txtConfirmarSenha.setVisible(false);
            txtConfirmarSenha.setManaged(false);
        } else {
            txtConfirmarSenha.setVisible(true);
            txtConfirmarSenha.setManaged(true);
            txtConfirmarSenhaRevelada.setVisible(false);
            txtConfirmarSenhaRevelada.setManaged(false);
        }
    }

    private void configurarUsuario() {
        if (usuarioLabel != null) {
            usuarioLabel.setText(SessaoService.getNomeUsuarioLogado());
        }
    }

    private void configurarMenuInicial() {
        if (submenuCadastro != null) {
            submenuCadastro.setVisible(true);
            submenuCadastro.setManaged(true);
        }
        if (setaCadastro != null) {
            setaCadastro.setText("▶");
        }
    }

    private void configurarNavegacaoCampos() {
        if (txtAcesso != null) {
            FormNavigationUtil.encadearCampos(btnCadastrar, txtNome, txtEmail, txtAcesso, txtSenha, txtConfirmarSenha);
        } else {
            FormNavigationUtil.encadearCampos(btnCadastrar, txtNome, txtEmail, txtSenha, txtConfirmarSenha);
        }
    }

    private void configurarSeletorAcesso() {
        if (acessoSelect != null) {
            acessoSelect.getItems().clear();
            acessoSelect.getItems().addAll("Instrutor", "Gestor");
            acessoSelect.setValue("Instrutor");
            acessoSelect.showingProperty().addListener((obs, wasShowing, isShowing) -> {
                if (isShowing) {
                    Platform.runLater(() -> {
                        double buttonWidth = acessoSelect.getWidth();
                        if (buttonWidth <= 0) {
                            buttonWidth = acessoSelect.getPrefWidth();
                        }
                        if (buttonWidth > 0) {
                            for (Window window : Window.getWindows()) {
                                if (window instanceof PopupWindow popupWindow) {
                                    if (popupWindow.getScene() != null && popupWindow.getScene().getRoot() != null) {
                                        Node root = popupWindow.getScene().getRoot();
                                        if (root.getStyleClass().contains("context-menu")
                                                && root instanceof Region region) {
                                            region.setMinWidth(buttonWidth);
                                            region.setPrefWidth(buttonWidth);
                                            region.setMaxWidth(buttonWidth);
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    private void configurarDataHora() {
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        if (dataLabel != null) {
            dataLabel.setText(LocalDate.now().format(dataFormatter));
        }

        if (horaLabel != null) {
            horaLabel.setText(LocalTime.now().format(horaFormatter));
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1),
                    event -> horaLabel.setText(LocalTime.now().format(horaFormatter))));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
    }

    @FXML
    public void onClickHome(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("home.fxml");
        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao navegar para a Página Inicial: " + e.getMessage());
        }
    }

    @FXML
    public void onClickRelatorio(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("historico.fxml");
        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao navegar para o Relatório: " + e.getMessage());
        }
    }

    @FXML
    protected void botaoCalendarioAction(javafx.event.ActionEvent event) {
        try {
            com.sigec.system.sigec.MainApplication.trocadorDeTelas("calendario.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void toggleDropdownCadastro(ActionEvent event) {
        if (submenuCadastro != null) {
            boolean expandido = !submenuCadastro.isVisible();
            submenuCadastro.setVisible(expandido);
            submenuCadastro.setManaged(expandido);
            if (setaCadastro != null) {
                setaCadastro.setText(expandido ? "▶" : "▼");
            }
            if (expandido && btnNavCadastro != null) {
                ButtonBorderLapAnimator.animarDropdown(btnNavCadastro, submenuCadastro);
            } else if (!expandido && btnNavCadastro != null) {
                ButtonBorderLapAnimator.cancelarAnimacaoAtiva();
                btnNavCadastro.getStyleClass().remove("btn-dropdown-ativo");
                btnNavCadastro.setStyle(null);
                submenuCadastro.getStyleClass().remove("submenu-lateral-ativo");
                submenuCadastro.setStyle(null);
            }
        }
    }

    @FXML
    public void toggleDropdownEstoque(ActionEvent event) {
        if (submenuEstoque != null && btnNavEstoque != null) {
            boolean expandido = !submenuEstoque.isVisible();

            if (setaEstoque != null) {
                setaEstoque.setText(expandido ? "▲" : "▼");
            }

            if (expandido) {
                submenuEstoque.setVisible(true);
                submenuEstoque.setManaged(true);

                DropdownBorderLapAnimator.animarDropdown(btnNavEstoque, submenuEstoque);
            } else {

                ButtonBorderLapAnimator.cancelarAnimacaoAtiva();

                btnNavEstoque.getStyleClass().remove("btn-dropdown-ativo");
                btnNavEstoque.setStyle(null);
                submenuEstoque.getStyleClass().remove("submenu-lateral-ativo");
                submenuEstoque.setStyle(null);

                submenuEstoque.setVisible(false);
                submenuEstoque.setManaged(false);
            }
        }
    }

    @FXML
    public void onClickInsumos(javafx.event.ActionEvent event) {
        try {
            com.sigec.system.sigec.MainApplication.trocadorDeTelas("lista-estoque.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao carregar a tela de Insumos: " + e.getMessage());
        }
    }

    @FXML
    public void onClickUtensilios(javafx.event.ActionEvent event) {
        try {
            com.sigec.system.sigec.MainApplication.trocadorDeTelas("gestao-utensilios.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao carregar a tela de Utensílios: " + e.getMessage());
        }
    }

    @FXML
    public void onClickCadastroUsuario(ActionEvent event) {
        if (txtNome != null) {
            txtNome.requestFocus();
        }
    }

    @FXML
    public void onClickCadastroTurma(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("cadastro-turma.fxml");
        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro",
                    "Não foi possível carregar a tela de Cadastro de Turma: " + e.getMessage());
        }
    }

    @FXML
    public void botaoSairAction(ActionEvent event) {
        com.sigec.system.sigec.Utils.SidebarNavigationManager.executarLogout();
    }

    @FXML
    public void onClickSolicitacoes(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("solicitacoes-extras.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exibirLoading(boolean mostrar) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(mostrar);
            loadingOverlay.setManaged(mostrar);
        }
        if (btnCadastrar != null) {
            btnCadastrar.setDisable(mostrar);
        }
        if (btnLimpar != null) {
            btnLimpar.setDisable(mostrar);
        }
    }

    @FXML
    public void onCadastrar(ActionEvent event) {
        // Nome e e-mail são validados DENTRO dos próprios campos (sem Alert).
        // Os dois são conferidos juntos, para o usuário ver todos os erros de uma vez.
        boolean nomeOk = aplicarValidacaoNome();
        boolean emailOk = aplicarValidacaoEmail();

        if (!nomeOk) {
            if (txtNome != null) {
                txtNome.requestFocus();
            }
            return;
        }
        if (!emailOk) {
            if (txtEmail != null) {
                txtEmail.requestFocus();
            }
            return;
        }

        String nomeLimpo = txtNome.getText().trim();
        String emailLimpo = txtEmail.getText().trim();
        String senha = txtSenha != null ? txtSenha.getText() : null;
        String confirmarSenha = txtConfirmarSenha != null ? txtConfirmarSenha.getText() : null;

        if (senha == null || senha.isEmpty() ||
                confirmarSenha == null || confirmarSenha.isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Campos Obrigatórios",
                    "Por favor, preencha todos os campos obrigatórios para continuar.");
            return;
        }

        if (!validarSenha(senha)) {
            exibirAlerta(Alert.AlertType.WARNING, "Senha Fora do Padrão",
                    "A senha deve conter no mínimo 8 caracteres, incluindo obrigatoriamente:\n"
                            + "• Pelo menos 1 letra maiúscula\n"
                            + "• Pelo menos 1 número\n"
                            + "• Pelo menos 1 caractere especial (ex: !@#$%&*)");
            if (txtSenha != null) {
                txtSenha.requestFocus();
            }
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            exibirAlerta(Alert.AlertType.ERROR, "Senhas não conferem",
                    "A senha e a confirmação de senha devem ser idênticas.");
            return;
        }

        String nivelAcesso = (acessoSelect != null && acessoSelect.getValue() != null
                && !acessoSelect.getValue().trim().isEmpty())
                ? acessoSelect.getValue().trim()
                : ((txtAcesso != null && !txtAcesso.getText().trim().isEmpty()) ? txtAcesso.getText().trim()
                : "Instrutor");

        // Exibe a tela de loading enquanto realiza o cadastro no banco de dados em segundo plano
        exibirLoading(true);

        Thread bgCadastro = new Thread(() -> {
            try {
                boolean cadastrado = UserDAO.cadastrar(nomeLimpo, emailLimpo, senha, nivelAcesso);
                Platform.runLater(() -> {
                    exibirLoading(false);
                    if (cadastrado) {
                        limparCampos();
                        exibirAlerta(Alert.AlertType.INFORMATION, "Cadastro Realizado",
                                "Usuário \"" + nomeLimpo + "\" cadastrado com sucesso no sistema!");
                    } else {
                        exibirAlerta(Alert.AlertType.ERROR, "Erro no Cadastro",
                                "Não foi possível cadastrar. O e-mail informado já pode estar em uso.");
                    }
                });
            } catch (SQLException | RuntimeException e) {
                Platform.runLater(() -> {
                    exibirLoading(false);
                    exibirAlerta(Alert.AlertType.ERROR, "Erro de Banco de Dados",
                            "Erro ao salvar cadastro: " + e.getMessage());
                });
            }
        });
        bgCadastro.setDaemon(true);
        bgCadastro.setName("SIGEC-CadastroUsuarioWorker");
        bgCadastro.start();
    }

    @FXML
    public void onLimparCampos(ActionEvent event) {
        limparCampos();
    }

    public void limparCampos() {
        if (txtNome != null)
            txtNome.clear();
        if (txtEmail != null)
            txtEmail.clear();
        if (txtSenha != null)
            txtSenha.clear();
        if (txtConfirmarSenha != null)
            txtConfirmarSenha.clear();
        if (txtAcesso != null)
            txtAcesso.clear();
        if (acessoSelect != null)
            acessoSelect.setValue("Instrutor");

        // Garante que as mensagens de erro somem ao limpar
        ocultarErro(txtNome, erroNome);
        ocultarErro(txtEmail, erroEmail);

        if (txtNome != null) {
            txtNome.requestFocus();
        }
    }

    // Exige nome e sobrenome, somente letras (com acento), espaço, apóstrofo e hífen
    private boolean validarNomeCompleto(String nome) {
        if (nome == null || nome.isBlank()) {
            return false;
        }
        return nome.trim().replaceAll("\\s+", " ")
                .matches("^\\p{L}{2,}(?:[ '\\-]\\p{L}+)+$");
    }

    private boolean validarSenha(String senha) {
        if (senha == null || senha.length() < 8 || senha.length() > 25) {
            return false;
        }
        boolean temNumero = false;
        boolean temMaiuscula = false;
        boolean temEspecial = false;

        for (char c : senha.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return false;
            } else if (Character.isDigit(c)) {
                temNumero = true;
            } else if (Character.isUpperCase(c)) {
                temMaiuscula = true;
            } else if (!Character.isLetterOrDigit(c)) {
                temEspecial = true;
            }
        }

        return temNumero && temMaiuscula && temEspecial;
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

}