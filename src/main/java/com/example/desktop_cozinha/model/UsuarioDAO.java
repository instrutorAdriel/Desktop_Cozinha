package com.example.desktop_cozinha.model;
import java.sql.*;
import java.util.List;

import com.example.desktop_cozinha.config.DatabaseConfig;


public class UsuarioDAO {

    public static Usuario buscaEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ?";

        try(Connection connection = DatabaseConfig.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql);){

           stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                Usuario usuario = new Usuario();
                usuario.setID(rs.getInt("ID"));
                usuario.setNome(rs.getString("nome"));
                usuario.setSenha(rs.getString("senha"));
                usuario.setEmail(rs.getString("email"));
                usuario.setCargo(rs.getString("cargo"));
                usuario.setToken(rs.getString("token"));
                return usuario;
            }



        }catch (SQLException e){
            System.out.println("Erro ao buscar email: " + e.getMessage());
        }return null;

    }

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
