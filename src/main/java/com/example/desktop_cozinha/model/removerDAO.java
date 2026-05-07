package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class removerDAO {
    public void deletarProduto(Integer idProdutoEmRemover) throws SQLException, ClassNotFoundException {

        String sql = "DELETE FROM estoque_geral WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProdutoEmRemover);

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                IO.println("Produto removido com sucesso!");
            } else {
                IO.println("Nenhum produto encontrado com esse ID.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover produto!", e);
        }
    }
}
