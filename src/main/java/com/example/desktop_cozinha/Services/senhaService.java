package com.example.desktop_cozinha.Services;

import com.example.desktop_cozinha.model.Usuario;
import com.example.desktop_cozinha.model.UsuarioDAO;
import jakarta.mail.MessagingException;

import java.sql.*;
import java.security.SecureRandom;


public class senhaService {

    public void enviarEmail(String email) throws SQLException, MessagingException {
        Usuario usuario = UsuarioDAO.buscaEmail(email);
        if(usuario == null){
            System.out.println("Usuario nao encontrado");
        }


        String token = String.format("%06d", new SecureRandom().nextInt(999999));
        String codigoHash = encryptService.encrypt(token);

        UsuarioDAO.salvarToken(codigoHash, email);

        String corpo = """ 
                Voce solicitou a recuperacao de senha, seu codigo de recuperacao e: 
                """.formatted(token);



        emailService.estruturaEmail(email, "Recupercao de senha", corpo);


    }

    public static void resetaSenha(String novaSenha, String confirmaSenha, String codigoHash)throws SQLException {

        String email = SessaoService.getEmailAtual();

        if(email == null){
            System.out.println("Sessao expirada");
        }

        String novaSenhaHash = encryptService.encrypt(novaSenha);
        UsuarioDAO.recuperaSenha(novaSenhaHash, email);
        UsuarioDAO.deletaToken(email);
        SessaoService.setEmailAtual(null);

    }


}





















