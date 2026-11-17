
package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Services.EmailService;
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

        // Gera um código de seis dígitos
        int novoToken = 100000 + random.nextInt(900000);

        // Tenta enviar o código
        boolean enviado = EmailService.enviarTokenRecuperacao(
                email,
                novoToken
        );

        // Se houver falha, não inicia a contagem
        if (!enviado) {

            exibirAlerta(
                    Alert.AlertType.ERROR,
                    "Falha no envio",
                    "Não foi possível enviar o código. Tente novamente."
            );

            return;
        }

        // Guarda o código e o e-mail
        tokenGerado = novoToken;
        emailDoToken = email;

        // Altera o texto do botão
        btnEnviarCodigo.setText("REENVIAR CÓDIGO");

        // Inicia os 90 segundos após o envio bem-sucedido
        iniciarContagem();

        exibirAlerta(
                Alert.AlertType.INFORMATION,
                "Código enviado",
                "Se o e-mail estiver cadastrado, um código de verificação foi enviado. Verifique sua caixa de entrada."
        );
    }

    // ==========================================
    // INICIAR CONTAGEM
    // ==========================================

    private void iniciarContagem() {

        // Define o horário exato de liberação
        horarioLiberacaoReenvio =
                System.currentTimeMillis() + (TEMPO_REENVIO * 1000L);

        segundosRestantes = TEMPO_REENVIO;

        if (lblContador != null) {
            lblContador.setVisible(true);
            lblContador.setManaged(true);
        }

        // Inicia a atualização visual
        iniciarTimelineRestante();
    }

    // ==========================================
    // RESTAURAR CONTAGEM AO VOLTAR PARA A TELA
    // ==========================================

    private void restaurarContagem() {

        if (horarioLiberacaoReenvio == 0) {
            segundosRestantes = 0;
            return;
        }

        long restante =
                horarioLiberacaoReenvio - System.currentTimeMillis();

        if (restante > 0) {

            segundosRestantes = (int) Math.ceil(
                    restante / 1000.0
            );

            if (lblContador != null) {
                lblContador.setVisible(true);
                lblContador.setManaged(true);
            }

            iniciarTimelineRestante();

        } else {

            segundosRestantes = 0;

            if (lblContador != null) {

                lblContador.setVisible(true);
                lblContador.setManaged(true);

                lblContador.setText(
                        "Você já pode solicitar um novo código."
                );
            }
        }
    }

    // ==========================================
    // ATUALIZAÇÃO DO TIMELINE
    // ==========================================

    private void iniciarTimelineRestante() {

        // Interrompe eventual contador anterior
        if (contador != null) {
            contador.stop();
        }

        atualizarTempoRestante();

        contador = new Timeline(
                new KeyFrame(Duration.seconds(1), evento -> {

                    atualizarTempoRestante();

                    if (segundosRestantes <= 0) {

                        contador.stop();

                        segundosRestantes = 0;

                        lblContador.setText(
                                "Você já pode solicitar um novo código."
                        );

                        atualizarEstadoBotao();
                    }
                })
        );

        contador.setCycleCount(Timeline.INDEFINITE);
        contador.play();
    }

    // ==========================================
    // CALCULAR TEMPO REAL RESTANTE
    // ==========================================

    private void atualizarTempoRestante() {

        long restante =
                horarioLiberacaoReenvio - System.currentTimeMillis();

        segundosRestantes = (int) Math.max(
                0,
                Math.ceil(restante / 1000.0)
        );

        if (segundosRestantes > 0) {
            atualizarTextoContador();
        }

        atualizarEstadoBotao();
    }

    // ==========================================
    // TEXTO DO CONTADOR
    // ==========================================

    private void atualizarTextoContador() {

        int minutos = segundosRestantes / 60;
        int segundos = segundosRestantes % 60;

        String tempo = String.format(
                "%02d:%02d",
                minutos,
                segundos
        );

        lblContador.setText(
                "Você poderá reenviar em " + tempo
        );
    }

    // ==========================================
    // CONTROLE DO BOTÃO
    // ==========================================

    private void atualizarEstadoBotao() {

        if (btnEnviarCodigo == null || emailDigitar == null) {
            return;
        }

        boolean emailVazio =
                emailDigitar.getText() == null
                        || emailDigitar.getText().trim().isEmpty();

        boolean aguardando =
                System.currentTimeMillis() < horarioLiberacaoReenvio;

        // Bloqueia se não houver e-mail ou se estiver aguardando
        btnEnviarCodigo.setDisable(emailVazio || aguardando);
    }

    // ==========================================
    // VERIFICAÇÃO DO CÓDIGO
    // ==========================================

    @FXML
    public void onConfirmaToken(ActionEvent event) {

        String codigo = codigoDigitar.getText() != null
                ? codigoDigitar.getText().trim()
                : "";

        String emailAtual = emailDigitar.getText() != null
                ? emailDigitar.getText().trim()
                : "";

        // Verifica se o código foi digitado
        if (codigo.isEmpty()) {

            exibirAlerta(
                    Alert.AlertType.WARNING,
                    "Atenção",
                    "Digite o código recebido no seu e-mail."
            );

            return;
        }

        // Verifica se já foi solicitado um código
        if (tokenGerado == null || emailDoToken == null) {

            exibirAlerta(
                    Alert.AlertType.WARNING,
                    "Código não solicitado",
                    "Solicite um código antes de realizar a verificação."
            );

            return;
        }

        // Verifica se o e-mail continua sendo o mesmo
        if (!emailAtual.equalsIgnoreCase(emailDoToken)) {

            exibirAlerta(
                    Alert.AlertType.WARNING,
                    "E-mail alterado",
                    "O código foi enviado para outro endereço. Solicite um novo código para este e-mail."
            );

            return;
        }

        // Verifica se possui seis números
        if (!codigo.matches("\\d{6}")) {

            exibirAlerta(
                    Alert.AlertType.WARNING,
                    "Código inválido",
                    "O código deve conter exatamente 6 números."
            );

            return;
        }

        // Compara o código digitado com o código gerado
        if (!codigo.equals(String.valueOf(tokenGerado))) {

            exibirAlerta(
                    Alert.AlertType.ERROR,
                    "Código incorreto",
                    "O código informado não corresponde ao código enviado."
            );

            return;
        }

        // Interrompe o contador
        if (contador != null) {
            contador.stop();
        }

        exibirAlerta(
                Alert.AlertType.INFORMATION,
                "Código confirmado",
                "Código validado com sucesso! Você será redirecionado para a alteração de senha."
        );

        try {

            MainApplication.trocadorDeTelas("alterar-senha.fxml");

            // Limpa os dados após a validação
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
    // VOLTAR PARA O LOGIN
    // ==========================================

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
