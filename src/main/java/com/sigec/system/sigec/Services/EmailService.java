
package com.sigec.system.sigec.Services;

import java.util.Properties;
import java.util.regex.Pattern;

import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public final class EmailService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private EmailService() {
        // Construtor privado: classe utilitária
    }

    // ==========================================
    // VALIDAR E-MAIL
    // ==========================================

    public static boolean isEmailValido(String email) {

        if (email == null || email.isBlank()) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean validarFormatoEmail(String email) {
        return isEmailValido(email);
    }

    // ==========================================
    // ENVIAR TOKEN DE RECUPERAÇÃO
    // ==========================================

    public static boolean enviarTokenRecuperacao(
            String destinatario,
            int token
    ) {

        if (!isEmailValido(destinatario)) {
            return false;
        }

        try {

            String host = ConfigDataBase.getEnvVar("MAIL_HOST", "");
            String port = ConfigDataBase.getEnvVar("MAIL_PORT", "");
            String username = ConfigDataBase.getEnvVar("MAIL_NAME", "");
            String password = ConfigDataBase.getEnvVar("MAIL_PASS", "");

            // Verifica se as configurações foram carregadas
            if (host.isBlank()
                    || port.isBlank()
                    || username.isBlank()
                    || password.isBlank()) {

                System.err.println(
                        "Configuração de e-mail incompleta. "
                                + "Verifique MAIL_HOST, MAIL_PORT, MAIL_NAME e MAIL_PASS."
                );

                return false;
            }

            Properties props = new Properties();

            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            // Evita que a aplicação fique aguardando indefinidamente
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");

            Session session = Session.getInstance(
                    props,
                    new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    }
            );

            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(username));

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(destinatario.trim())
            );

            message.setSubject("Recuperação de Senha - SIGEC");

            message.setText(
                    "Olá!\n\n"
                            + "Você solicitou a recuperação de senha do sistema SIGEC.\n\n"
                            + "Seu código de recuperação é: " + token + "\n\n"
                            + "Caso não tenha solicitado a recuperação de senha, "
                            + "ignore este e-mail.\n\n"
                            + "Atenciosamente,\n"
                            + "Equipe SIGEC"
            );

            Transport.send(message);

            System.out.println(
                    "Token de recuperação enviado para: " + destinatario
            );

            return true;

        } catch (Exception e) {

            System.err.println("Erro ao enviar e-mail de recuperação:");
            e.printStackTrace();

            return false;
        }
    }
}
