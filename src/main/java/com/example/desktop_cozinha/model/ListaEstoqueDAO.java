package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListaEstoqueDAO {// READ (Busca todos os usuários e retorna uma lista)

    public List<String> lerTodos() {
        String sql = "SELECT * FROM estoque_geral";
        List<String> itens = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String info = rs.getString("nome") + " - " + rs.getString("tipo") + " (" + rs.getString("quantidade_atual") + " (" + rs.getString("unidade_medida");
                itens.add(info);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao ler", e);
        }
        return itens;
    }

}
