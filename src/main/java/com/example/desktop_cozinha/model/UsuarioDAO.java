package com.example.desktop_cozinha.model;
import java.sql.*;

import com.example.desktop_cozinha.config.DatabaseConfig;


public class UsuarioDAO {



    public static void salvarToken(String email, String token) throws SQLException {
        String sql = "UPDATE usuarios SET token = ? WHERE email = ?";

        try(Connection connection = DatabaseConfig.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);){
            stmt.setString(1, token);
            stmt.setString(2, email);
            stmt.executeUpdate();
        }catch (SQLException e){
            System.out.println("Erro ao salvar token: " + e.getMessage());
        }

    }

    public static void recuperaSenha(String novaSenha, String email) throws SQLException {
        String sql = "UPDATE usuarios SET senha = ? WHERE email = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, novaSenha);
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar senha");

        }


    }
    public static void deletaToken(String email) throws SQLException {
        String sql = "UPDATE usuarios SET token = NULL WHERE email = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar senha");
        }
    }










}