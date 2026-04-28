package com.example.desktop_cozinha.model;

//import com.mysql.cj.exceptions.ClosedOnExpiredPasswordException;
import com.example.desktop_cozinha.config.DatabaseConfig;

//import java.awt.*;
import java.sql.*;

public class LoginDAO {
    public boolean autenticar(String email, String senha) {
        String sql = "SELECT senha FROM usuarios WHERE email = ?";


        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String senhaDoBanco = rs.getString("senha");
                    if (senha.equals(senhaDoBanco))
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

    public static Connection conectar() {
        try {
            // Exemplo para MySQL
            String url = "jdbc:mysql://localhost:3306/nome_do_seu_banco?useTimezone=true&serverTimezone=UTC";
            String usuario = "root";
            String senha = "sua_senha";

            return DriverManager.getConnection(url, usuario, senha);
        } catch (SQLException e) {
            System.err.println("Erro de Conexão: " + e.getMessage());
            return null;
        }
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