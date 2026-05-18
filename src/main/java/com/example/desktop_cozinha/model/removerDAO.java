package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO responsável por operações de remoção na tabela estoque_geral.
 */
public class removerDAO {

    /**
     * Deleta um produto pelo seu ID.
     *
     * @param id ID do produto a ser removido.
     * @return true se o produto foi removido com sucesso, false caso contrário.
     */
    public boolean deletarProduto(int id) {
        String sql = "DELETE FROM estoque_geral WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao deletar produto com id=" + id + ": " + e.getMessage());
            return false;
        }
    }
}
