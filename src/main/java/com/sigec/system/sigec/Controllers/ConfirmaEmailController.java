
package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Services.EmailService;
import com.sigec.system.sigec.Services.SenhaService;
import com.sigec.system.sigec.Utils.BackgroundAnimator;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;

public class ConfirmaEmailController {

    @FXML
    private TextField emailDigitar;

    @FXML
    private TextField codigoDigitar;

    @FXML
    private Button btnEnviarCodigo;

    @FXML
    private Label lblContador;

    @FXML
    private StackPane rootPane;

    @FXML
    private AnchorPane animatedBackground;

    private Timeline contador;

    // Dados mantidos enquanto a aplicação estiver aberta
    private static Integer tokenGerado = null;
    private static String emailDoToken = null;
    private static long horarioLiberacaoReenvio = 0;

    private static final int TEMPO_REENVIO = 90;

    // ==========================================
    // INICIALIZAÇÃO
    // ==========================================

    @FXML
    public void initialize() {

        if (animatedBackground != null && rootPane != null) {
            BackgroundAnimator.startAnimation(
                    animatedBackground,
                    rootPane
            );
        }

        if (emailDoToken != null && emailDigitar != null) {
            emailDigitar.setText(emailDoToken);
        }

        if (emailDigitar != null) {
            emailDigitar.textProperty().addListener(
                    (observable, antigo, novo) -> atualizarEstadoBotao()
            );
        }

        if (codigoDigitar != null) {
            codigoDigitar.setOnAction(this::onConfirmaToken);
        }

        if (tokenGerado != null && btnEnviarCodigo != null) {
            btnEnviarCodigo.setText("REENVIAR CÓDIGO");
        }

        restaurarContagem();
        atualizarEstadoBotao();
    }

    // ==========================================
    // ENVIAR / REENVIAR CÓDIGO
    // ==========================================

    @FXML
    public void onConfirmaEmailClick(ActionEvent event) {

        if (emailDigitar == null) {
            return;
        }

        String email = emailDigitar.getText() == null
                ? ""
                : emailDigitar.getText().trim();

        if (email.isEmpty()) {
            exibirAlerta(
                    Alert.AlertType.WARNING,
                    "Atenção",
                    "Informe seu e-mail antes de solicitar o código."
            );
            atualizarEstadoBotao();
            return;
        }

        if (!EmailService.validarFormatoEmail(email)) {
            exibirAlerta(
                    Alert.AlertType.WARNING,
                    "E-mail inválido",
                    "Digite um endereço de e-mail válido."
            );
            return;
        }

        // Não permite novo envio antes de terminar os 90 segundos
        if (System.currentTimeMillis() < horarioLiberacaoReenvio) {
            atualizarEstadoBotao();
            return;
        }

        // Gera o token pelo serviço já existente no projeto
        int novoToken = SenhaService.gerarTokenRecuperacao();

        // Envia o token pelo serviço SMTP
        boolean enviado = EmailService.enviarTokenRecuperacao(
                email,
                novoToken
        );

        if (!enviado) {
            exibirAlerta(
                    Alert.AlertType.ERROR,
                    "Falha no envio",
                    "Não foi possível enviar o código para o e-mail informado. "
                            + "Verifique a configuração do serviço de e-mail e tente novamente."
            );

            atualizarEstadoBotao();
            return;
        }

        // Só salva o token depois que o envio retornar sucesso
        tokenGerado = novoToken;
        emailDoToken = email;

        // Inicia os 90 segundos somente após o envio bem-sucedido
        iniciarContagem();

        if (btnEnviarCodigo != null) {
            btnEnviarCodigo.setText("REENVIAR CÓDIGO");
        }

        exibirAlerta(
                Alert.AlertType.INFORMATION,
                "Código enviado",
                "O código de recuperação foi enviado para:\n" + email
        );

        atualizarEstadoBotao();
    }

    // ==========================================
    // CONTADOR DE 90 SEGUNDOS
    // ==========================================

    private void iniciarContagem() {

        horarioLiberacaoReenvio =
                System.currentTimeMillis() + (TEMPO_REENVIO * 1000L);

        if (lblContador != null) {
            lblContador.setVisible(true);
            lblContador.setManaged(true);
        }

        iniciarTimelineRestante();
    }

    private void restaurarContagem() {

        if (horarioLiberacaoReenvio == 0) {
            if (lblContador != null) {
                lblContador.setVisible(false);
                lblContador.setManaged(false);
            }
            return;
        }

        long restante =
                horarioLiberacaoReenvio - System.currentTimeMillis();

        if (restante > 0) {
            if (lblContador != null) {
                lblContador.setVisible(true);
                lblContador.setManaged(true);
            }

            iniciarTimelineRestante();

        } else {
            if (lblContador != null) {
                lblContador.setText(
                        "Você já pode solicitar um novo código."
                );
                lblContador.setVisible(true);
                lblContador.setManaged(true);
            }

            atualizarEstadoBotao();
        }
    }

    private void iniciarTimelineRestante() {

        if (contador != null) {
            contador.stop();
        }

        atualizarTempoRestante();

        contador = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        event -> atualizarTempoRestante()
                )
        );

        contador.setCycleCount(Timeline.INDEFINITE);
        contador.play();
    }

    private void atualizarTempoRestante() {

        long restante =
                horarioLiberacaoReenvio - System.currentTimeMillis();

        if (restante <= 0) {

            if (contador != null) {
                contador.stop();
            }

            if (lblContador != null) {
                lblContador.setText(
                        "Você já pode solicitar um novo código."
                );
            }

            atualizarEstadoBotao();
            return;
        }

        int segundosRestantes =
                (int) Math.ceil(restante / 1000.0);

        int minutos = segundosRestantes / 60;
        int segundos = segundosRestantes % 60;

        if (lblContador != null) {
            lblContador.setText(
                    String.format(
                            "Você poderá reenviar o código em %02d:%02d",
                            minutos,
                            segundos
                    )
            );
        }

        atualizarEstadoBotao();
    }

    // ==========================================
    // ESTADO DO BOTÃO
    // ==========================================

    private void atualizarEstadoBotao() {

        if (btnEnviarCodigo == null) {
            return;
        }

        boolean emailVazio =
                emailDigitar == null
                        || emailDigitar.getText() == null
                        || emailDigitar.getText().trim().isEmpty();

        boolean aguardando =
                System.currentTimeMillis() < horarioLiberacaoReenvio;

        btnEnviarCodigo.setDisable(emailVazio || aguardando);
    }

    // ==========================================
    // VALIDAR TOKEN
    // ==========================================

    @FXML
    public void onConfirmaToken(ActionEvent event) {

        if (codigoDigitar == null || emailDigitar == null) {
            return;
        }

        String codigo = codigoDigitar.getText() == null
                ? ""
                : codigoDigitar.getText().trim();

        String emailAtual = emailDigitar.getText() == null
                ? ""
                : emailDigitar.getText().trim();

        if (codigo.isEmpty()) {
            exibirAlerta(
                    Alert.AlertType.WARNING,
                    "Atenção",
                    "Digite o código recebido no seu e-mail."
            );
            return;
        }

        if (tokenGerado == null || emailDoToken == null) {
            exibirAlerta(
                    Alert.AlertType.WARNING,
                    "Código não solicitado",
                    "Solicite um código de recuperação antes de continuar."
            );
            return;
        }

        if (!emailDoToken.equalsIgnoreCase(emailAtual)) {
            exibirAlerta(
                    Alert.AlertType.WARNING,
                    "E-mail diferente",
                    "O código informado foi enviado para outro endereço de e-mail."
            );
            return;
        }

        if (!codigo.matches("\\d{6}")) {
            exibirAlerta(
                    Alert.AlertType.WARNING,
                    "Código inválido",
                    "O código deve conter exatamente 6 números."
            );
            return;
        }

        if (!codigo.equals(String.valueOf(tokenGerado))) {
            exibirAlerta(
                    Alert.AlertType.ERROR,
                    "Código incorreto",
                    "O código informado está incorreto. Verifique seu e-mail."
            );
            return;
        }

        try {

            // Só limpa o token depois que a navegação for realizada
            MainApplication.trocadorDeTelas("alterar-senha.fxml");

            if (contador != null) {
                contador.stop();
            }

            tokenGerado = null;
            emailDoToken = null;
            horarioLiberacaoReenvio = 0;

        } catch (IOException e) {

            exibirAlerta(
                    Alert.AlertType.ERROR,
                    "Erro",
                    "Não foi possível carregar a tela de alteração de senha: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================
    // VOLTAR PARA LOGIN
    // ==========================================

    @FXML
    public void onVoltarClick(ActionEvent event) {

        if (contador != null) {
            contador.stop();
        }

        try {

            MainApplication.trocadorDeTelas("login.fxml");

        } catch (IOException e) {

            exibirAlerta(
                    Alert.AlertType.ERROR,
                    "Erro",
                    "Erro ao voltar para o login: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // ALERTAS
    // ==========================================

    private void exibirAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensagem
    ) {

        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
