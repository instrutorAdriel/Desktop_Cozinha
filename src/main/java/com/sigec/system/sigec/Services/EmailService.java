package com.sigec.system.sigec.Services;

import java.util.regex.Pattern;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Serviço de regras de validação e comunicação de e-mail do sistema SIGEC.
 */
public final class EmailService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailService() {
        // Construtor privado para utilitário de serviço
    }

    /**
     * Valida sintaticamente se a string é um endereço de e-mail válido.
     *
     * @param email E-mail a ser verificado
     * @return true se o formato estiver correto, false caso contrário
     */
    public static boolean isEmailValido(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Alias para isEmailValido para conveniência semântica.
     *
     * @param email E-mail a ser verificado
     * @return true se o formato estiver correto, false caso contrário
     */
    public static boolean validarFormatoEmail(String email) {
        return isEmailValido(email);
    }

    /**
     * Envia o token de recuperação de senha por e-mail para o usuário.
     *
     * @param destinatario E-mail do usuário
     * @param token        Token gerado de 6 dígitos
     * @return true se enviado com sucesso
     */
    public static boolean enviarTokenRecuperacao(String destinatario, int token) {
        if (!isEmailValido(destinatario)) {
            return false;
        }
        try {
            String host = ConfigDataBase.getEnvVar("MAIL_HOST", "");
            String port = ConfigDataBase.getEnvVar("MAIL_PORT", "");
            String username = ConfigDataBase.getEnvVar("MAIL_NAME", "");
            String password = ConfigDataBase.getEnvVar("MAIL_PASS", "");

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props,
                    new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(destinatario)
            );

            message.setSubject("Recuperação de Senha - SIGEC");

            message.setText(
                    "Seu código de recuperação é: " + token +
                            "\n\nCaso não tenha solicitado a recuperação de senha, ignore este e-mail."
            );

            Transport.send(message);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
