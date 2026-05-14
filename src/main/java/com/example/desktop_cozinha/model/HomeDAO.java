package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeDAO {
    public String bucarNome(String email){
        String sql = "SELECT nome FROM usuarios WHERE email = ?";
        try (Connection conect = DatabaseConfig.getConnection();
             PreparedStatement cmd = conect.prepareStatement(sql)) {
            cmd.setString(1, email);
            try (ResultSet rs = cmd.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nome");
                }
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    };
}
