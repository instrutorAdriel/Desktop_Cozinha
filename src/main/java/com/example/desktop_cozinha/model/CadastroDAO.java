package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class CadastroDAO {
    // CREATE

    public boolean cadastrarUsuario(String nome, String email, String senhaPura) {
        String sql = "INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)";
        String senhaCriptografada = BCrypt.hashpw(senhaPura, BCrypt.gensalt());
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, senhaCriptografada);
            stmt.executeUpdate();
            return true; // Retorna true indicando que cadastrou com sucesso

        } catch (SQLIntegrityConstraintViolationException e) {
            // Captura EXATAMENTE o erro de e-mail já existente (Duplicate entry)
            return false; // Retorna false para o Controller saber que deu erro de duplicação

        } catch (SQLException e) {
            // Se for um erro de conexão ou outro problema no banco, estoura a exceção
            throw new RuntimeException("Erro interno no banco de dados ao criar usuário", e);
        }
    }
}
