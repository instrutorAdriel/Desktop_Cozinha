package com.example.desktop_cozinha.model;

//import com.mysql.cj.exceptions.ClosedOnExpiredPasswordException;
import com.example.desktop_cozinha.config.DatabaseConfig;

//import java.awt.*;
import java.sql.*;

public class LoginDAO {
    public boolean autenticar(String emailD, String senhaD) {
        String sql = "SELECT senha FROM usuarios WHERE email = ?";


        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emailD);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String senhaDoBanco = rs.getString("senha");
                    if (senhaD.equals(senhaDoBanco))
                        return true; //senha correta
                    else {
                        return false;//senha incorreta
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar usuário", e);
        }

        return false; // usuário não encontrado
    }


    public String obterSenhaHash(String usuario) {
        String sql = "SELECT senha FROM usuarios WHERE nome = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("senha"); // Retorna o hash (ex: $2a$10$...)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Usuário não encontrado
    }



}