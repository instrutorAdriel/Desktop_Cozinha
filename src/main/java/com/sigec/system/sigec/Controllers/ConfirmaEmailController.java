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
import java.security.SecureRandom;

/**
 * Controlador da etapa de confirmação de e-mail e validação de token de recuperação.
 */
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

    // Contador da tela atual
    private Timeline contador;

    private int segundosRestantes = 0;

    // ==========================================
    // DADOS PERSISTENTES DURANTE A EXECUÇÃO
    // ==========================================

    private static Integer tokenGerado = null;

    private static String emailDoToken = null;

    // Horário exato em que o usuário poderá reenviar
    private static long horarioLiberacaoReenvio = 0;

    // Tempo de espera: 90 segundos
    private static final int TEMPO_REENVIO = 90;

    private final SecureRandom random = new SecureRandom();

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

        // Recupera o e-mail utilizado anteriormente
        if (emailDoToken != null && emailDigitar != null) {
            emailDigitar.setText(emailDoToken);
        }

        // Atualiza o botão quando o e-mail for alterado
        if (emailDigitar != null) {

            emailDigitar.textProperty().addListener(
                    (observable, antigo, novo) -> atualizarEstadoBotao()
            );
        }

        // Permite confirmar o código pressionando Enter
        if (codigoDigitar != null) {
            codigoDigitar.setOnAction(this::onConfirmaToken);
        }

        // Se já houve envio anteriormente, altera o texto
        if (btnEnviarCodigo != null && tokenGerado != null) {
            btnEnviarCodigo.setText("REENVIAR CÓDIGO");
        }

        // Recupera a contagem sem reiniciar os 90 segundos
        restaurarContagem();

        atualizarEstadoBotao();
    }

    // ==========================================
    // ENVIO E REENVIO DO CÓDIGO
    // ==========================================

    @FXML
    public void onConfirmaEmailClick(ActionEvent event) {

        String email = emailDigitar.getText() != null
                ? emailDigitar.getText().trim()
                : "";

        // Verifica se o campo está vazio
        if (email.isEmpty()) {

            exibirAlerta(
                    Alert.AlertType.WARNING,
                    "Atenção",
                    "Informe seu e-mail antes de solicitar o código."
            );

            atualizarEstadoBotao();
            return;
        }

        // Valida o formato do e-mail
        if (!EmailService.validarFormatoEmail(email)) {

            exibirAlerta(
                    Alert.AlertType.WARNING,
                    "Formato inválido",
                    "Digite um endereço de e-mail válido."
            );

            return;
        }

        // Impede o envio durante a contagem
        if (System.currentTimeMillis() < horarioLiberacaoReenvio) {

            atualizarEstadoBotao();
            return;
        }
        int token = SenhaService.gerarTokenRecuperacao();

        boolean enviado = EmailService.enviarTokenRecuperacao(
                email.trim(),
                token
        );

        if (enviado) {
            exibirAlerta(Alert.AlertType.INFORMATION,
                    "Código Enviado",
                    "Código enviado para: " + email);
        } else {
            exibirAlerta(Alert.AlertType.ERROR,
                    "Erro",
                    "Falha ao enviar o código.");
        }

    }

    @FXML
    public void onConfirmaToken(ActionEvent event) {
        String codigo = codigoDigitar != null ? codigoDigitar.getText() : null;

        if (codigo == null || codigo.trim().isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Digite o código recebido no seu e-mail.");
            return;
        }

        exibirAlerta(Alert.AlertType.INFORMATION, "Código Confirmado",
                "Código validado com sucesso! Você será redirecionado para a alteração de senha.");

        try {
            MainApplication.trocadorDeTelas("alterar-senha.fxml");

            // Limpa os dados após a validação
            tokenGerado = null;
            emailDoToken = null;
            horarioLiberacaoReenvio = 0;

        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a tela de alteração de senha: " + e.getMessage());
        }
    }

    @FXML
    public void onVoltarClick(ActionEvent event) {

        // Para somente o Timeline desta tela.
        // O horário de liberação permanece armazenado.
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
