package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoricoDAO {

    // Helper para evitar repetição de código na criação do objeto Historico
    private static Historico mapearHistorico(ResultSet rs) throws SQLException {
        return new Historico(
                rs.getString("nome_produto"),
                rs.getString("nome_usuario"),
                rs.getString("tipo_movimentacao"),
                rs.getInt("quantidade_movimentada"),
                rs.getString("observacao"),
                rs.getString("data_hora")
        );
    }

    public static List<Historico> imprimirHistoricoCompleto() {
        String sql = """
            SELECT e.nome AS nome_produto, u.nome AS nome_usuario, 
                   h.tipo_movimentacao, h.quantidade_movimentada, 
                   h.observacao, h.data_hora
            FROM historico_movimentacoes h
            JOIN estoque_geral e ON h.produto_id = e.id
            JOIN usuarios u ON h.usuario_id = u.id
            ORDER BY h.data_hora DESC
            """;
        return buscar(sql, null);
    }

    public static List<Historico> imprimirHistoricoporproduto(String txtpesquisa) {
        String sql = """
            SELECT e.nome AS nome_produto, u.nome AS nome_usuario, 
                   h.tipo_movimentacao, h.quantidade_movimentada, 
                   h.observacao, h.data_hora
            FROM historico_movimentacoes h
            JOIN estoque_geral e ON h.produto_id = e.id
            JOIN usuarios u ON h.usuario_id = u.id
            WHERE e.nome LIKE ?
            ORDER BY h.data_hora DESC
            """;
        return buscar(sql, "%" + txtpesquisa + "%");
    }

    public static List<Historico> imprimirHistoricoporusuario(String txtpesquisa) {
        String sql = """
            SELECT e.nome AS nome_produto, u.nome AS nome_usuario, 
                   h.tipo_movimentacao, h.quantidade_movimentada, 
                   h.observacao, h.data_hora
            FROM historico_movimentacoes h
            JOIN estoque_geral e ON h.produto_id = e.id
            JOIN usuarios u ON h.usuario_id = u.id
            WHERE u.nome LIKE ?
            ORDER BY h.data_hora DESC
            """;
        return buscar(sql, "%" + txtpesquisa + "%");
    }

    public static List<Historico> imprimirHistoricoportipo(String txtpesquisa) {
        String sql = """
            SELECT e.nome AS nome_produto, u.nome AS nome_usuario, 
                   h.tipo_movimentacao, h.quantidade_movimentada, 
                   h.observacao, h.data_hora
            FROM historico_movimentacoes h
            JOIN estoque_geral e ON h.produto_id = e.id
            JOIN usuarios u ON h.usuario_id = u.id
            WHERE h.tipo_movimentacao LIKE ?
            ORDER BY h.data_hora DESC
            """;
        return buscar(sql, "%" + txtpesquisa + "%");
    }
    public static List<Historico> imprimirHistoricopordata(String txtpesquisa) {
        String sql = """
            SELECT e.nome AS nome_produto, u.nome AS nome_usuario, 
                   h.tipo_movimentacao, h.quantidade_movimentada, 
                   h.observacao, h.data_hora
            FROM historico_movimentacoes h
            JOIN estoque_geral e ON h.produto_id = e.id
            JOIN usuarios u ON h.usuario_id = u.id
            WHERE h.data_hora LIKE ?
            ORDER BY h.data_hora DESC
            """;
        return buscar(sql, "%" + txtpesquisa + "%");
    }

    // Método genérico para reduzir a repetição de código (Boilerplate)
    private static List<Historico> buscar(String sql, String parametro) {
        List<Historico> itens = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (parametro != null) {
                stmt.setString(1, parametro);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(mapearHistorico(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro na consulta SQL: " + e.getMessage());
        }
        return itens;
    }
}

