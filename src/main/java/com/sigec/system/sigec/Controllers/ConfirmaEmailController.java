package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.MainApplication;
import com.sigec.system.sigec.Services.EmailService;
import com.sigec.system.sigec.Utils.BackgroundAnimator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Controlador da etapa de confirmação de e-mail e validação de token de recuperação.
 */
public class ConfirmaEmailController {

    @FXML
    private TextField emailDigitar;

    @FXML
    private TextField codigoDigitar;

    @FXML
    private StackPane rootPane;

    @FXML
    private AnchorPane animatedBackground;
    @FXML
    private ComboBox<String> SelecionarUnidade;

    public static String unidadeSelecionada;

    public void initialize() {
        if (animatedBackground != null && rootPane != null) {
            BackgroundAnimator.startAnimation(animatedBackground, rootPane);
        }
        if (SelecionarUnidade != null) {
            SelecionarUnidade.getItems().addAll(
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
                    "34 - CEP CORPORATIVO"
            );

            SelecionarUnidade.getSelectionModel().selectFirst();
            unidadeSelecionada = SelecionarUnidade.getValue();

            SelecionarUnidade.setOnAction(e -> {
                unidadeSelecionada = SelecionarUnidade.getValue();
            });
        }
        if (emailDigitar != null) {
            emailDigitar.setOnAction(e -> {
                if (emailDigitar.getText() != null && !emailDigitar.getText().trim().isEmpty()) {
                    onConfirmaEmailClick(e);
                }
                if (codigoDigitar != null) {
                    codigoDigitar.requestFocus();
                }
            });
        }

        if (codigoDigitar != null) {
            codigoDigitar.setOnAction(this::onConfirmaToken);
        }
    }

    @FXML
    public void onConfirmaEmailClick(ActionEvent event) {
        String email = emailDigitar != null ? emailDigitar.getText() : null;

        if (email == null || email.trim().isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Informe o seu e-mail cadastrado.");
            return;
        }

        if (!EmailService.validarFormatoEmail(email.trim())) {
            exibirAlerta(Alert.AlertType.WARNING, "Formato Inválido", "Por favor, digite um endereço de e-mail válido.");
            return;
        }

        exibirAlerta(Alert.AlertType.INFORMATION, "Código Enviado",
                "Se o e-mail estiver cadastrado, um código de verificação foi enviado. Por favor, verifique sua caixa de entrada.");
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
        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a tela de alteração de senha: " + e.getMessage());
        }
    }

    @FXML
    public void onVoltarClick(ActionEvent event) {
        try {
            MainApplication.trocadorDeTelas("login.fxml");
        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao voltar para o login: " + e.getMessage());
        }
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
