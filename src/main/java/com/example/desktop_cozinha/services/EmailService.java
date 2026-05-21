package com.example.desktop_cozinha.services;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

private static final String email = "sigecsenac@gmail.com";
private static final String senha = "cnmvnbgshzrxdllo";

    public static Session session() {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, senha);
            }
        });
        return session;
    }
    public static void estruturaEmail(String destinario, String assunto, String mensagem) throws MessagingException {

        try {Session sessao = session();

            MimeMessage message = new MimeMessage(sessao);
            message.addRecipients(Message.RecipientType.TO, destinario);
            message.setSubject(assunto);
            message.setText(mensagem);
            Transport.send(message);

    }catch (MessagingException e) {
            System.out.println("Erro ao enviar o email: " + e.getMessage());
        }




}
}
