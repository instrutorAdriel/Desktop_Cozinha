package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListaEstoqueDAO {

    public List<ProdutoListaEstoque> lerTodos() {
        String sql = "SELECT nome, tipo, quantidade_atual, unidade_medida FROM estoque_geral";
        List<ProdutoListaEstoque> itens = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                ProdutoListaEstoque p = new ProdutoListaEstoque(
                        rs.getString("nome"),
                        rs.getString("tipo"),
                        rs.getString("quantidade_atual"),
                        rs.getString("unidade_medida")
                );

                itens.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao executar SELECT em estoque_geral");
            e.printStackTrace();
        }

        return itens;
    }
}