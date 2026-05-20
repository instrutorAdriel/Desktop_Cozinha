package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ListaEstoqueDAO {

    public List<ProdutoListaEstoque> filtrarProdutos(String nomeBusca, boolean naoPereciveis, boolean pereciveis, boolean utensilios) {

        String sql = "SELECT id, nome, tipo, quantidade_atual, unidade_medida, estoque_minimo, data_validade FROM estoque_geral WHERE nome LIKE ?";

        List<String> filtros = new ArrayList<>();

        if (naoPereciveis) filtros.add("tipo = 'NAO_PERECIVEL'");
        if (pereciveis)    filtros.add("tipo = 'PERECIVEL'");
        if (utensilios)    filtros.add("tipo = 'UTENSILIO'");

        if (!filtros.isEmpty()) {
            sql += " AND (" + String.join(" OR ", filtros) + ")";
        }

        List<ProdutoListaEstoque> itens = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nomeBusca + "%");

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    java.sql.Date dataBanco = rs.getDate("data_validade");
                    LocalDate dataConvertida = null;
                    if (dataBanco != null) {
                        dataConvertida = dataBanco.toLocalDate();
                    }

                    ProdutoListaEstoque p = new ProdutoListaEstoque(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("tipo"),
                            rs.getString("quantidade_atual"),
                            rs.getString("unidade_medida"),
                            rs.getString("estoque_minimo"),
                            dataConvertida
                    );
                    itens.add(p);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao filtrar produtos: " + e.getMessage());
        }

        return itens;
    }
}
