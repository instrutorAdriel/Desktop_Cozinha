package com.example.desktop_cozinha.services;

import com.example.desktop_cozinha.model.Usuario;
import com.example.desktop_cozinha.model.UsuarioDAO;
import jakarta.mail.MessagingException;

import java.sql.*;
import java.security.SecureRandom;


public class SenhaService {

    public static void enviarEmail(String email) throws SQLException, MessagingException {
        Usuario usuario = UsuarioDAO.buscaEmail(email);
        if(usuario == null){
            System.out.println("Usuario nao encontrado");
        }


        String token = String.format("%06d", new SecureRandom().nextInt(999999));
        String codigoHash = EncryptService.encrypt(token);

        UsuarioDAO.salvarToken(email, codigoHash);

        String corpo = """ 
                Voce solicitou a recuperacao de senha, seu codigo de recuperacao e: 
                """+ token;



        EmailService.estruturaEmail(email, "Recupercao de senha", corpo);


    }

    public static void resetaSenha(String novaSenha, String codigoHash)throws SQLException {

        String email = SessaoService.getEmailAtual();

        if(email == null){
            System.out.println("Sessao expirada");
        }

        String novaSenhaHash = EncryptService.encrypt(novaSenha);
        UsuarioDAO.recuperaSenha(novaSenhaHash, email);
        UsuarioDAO.deletaToken(email);
        SessaoService.setEmailAtual(null);

    }


}





















