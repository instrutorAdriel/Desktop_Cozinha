package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListaEstoqueDAO {

    public List<ProdutoListaEstoque> filtrarProdutos(String nomeBusca, boolean naoPereciveis,boolean pereciveis, boolean utensilios) {
        String sql = "SELECT nome, tipo, quantidade_atual, unidade_medida FROM estoque_geral WHERE nome LIKE ?";

        List<String> filtros = new ArrayList<>();

        if (naoPereciveis) filtros.add("tipo = 'Não-Perecível'");
        if (pereciveis)    filtros.add("tipo = 'Perecível'");
        if (utensilios)    filtros.add("tipo = 'Utensílio'");

        if (!filtros.isEmpty()) {
            sql += " AND (" + String.join(" OR ", filtros) + ")";
        }


        List<ProdutoListaEstoque> itens = new ArrayList<>();


        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nomeBusca + "%");

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    ProdutoListaEstoque p = new ProdutoListaEstoque(
                            rs.getString("nome"),
                            rs.getString("tipo"),
                            rs.getString("quantidade_atual"),
                            rs.getString("unidade_medida")
                    );
                    itens.add(p);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro");
        }

        return itens;
    }
}