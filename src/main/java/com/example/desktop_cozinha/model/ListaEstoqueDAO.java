package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável por consultas de listagem e filtro no estoque geral.
 */
public class ListaEstoqueDAO {

    public List<ProdutoListaEstoque> filtrarProdutos(
            String nomeBusca,
            boolean naoPereciveis,
            boolean pereciveis,
            boolean utensilios) {

        StringBuilder sql = new StringBuilder(
                "SELECT id, nome, tipo, quantidade_atual, unidade_medida, estoque_minimo, data_validade " +
                        "FROM estoque_geral WHERE nome LIKE ?");

        List<String> filtros = new ArrayList<>();
        if (naoPereciveis) filtros.add("tipo = 'Não_Perecível'");
        if (pereciveis)    filtros.add("tipo = 'Perecível'");
        if (utensilios)    filtros.add("tipo = 'Utensílio'");

        if (!filtros.isEmpty()) {
            sql.append(" AND (").append(String.join(" OR ", filtros)).append(")");
        }

        List<ProdutoListaEstoque> itens = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setString(1, "%" + nomeBusca + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    java.sql.Date dataBanco = rs.getDate("data_validade");
                    LocalDate dataConvertida = (dataBanco != null) ? dataBanco.toLocalDate() : null;

                    itens.add(new ProdutoListaEstoque(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("tipo"),
                            rs.getString("quantidade_atual"),
                            rs.getString("unidade_medida"),
                            rs.getString("estoque_minimo"),
                            dataConvertida
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao filtrar produtos: " + e.getMessage());
        }

        return itens;
    }
}
