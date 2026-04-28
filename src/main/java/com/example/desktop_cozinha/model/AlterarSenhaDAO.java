package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AlterarSenhaDAO {

    public void alterar(String Senha,String email) {
        String sql = "UPDATE usuarios SET senha = ? WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, Senha);
            stmt.setString(2, email);
            stmt.executeUpdate();
            System.out.println("Senha atualizada!");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar", e);
        }
    }
}
