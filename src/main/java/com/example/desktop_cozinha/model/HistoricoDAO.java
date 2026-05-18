package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoricoDAO {

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

    public static List<Historico> imprimirHistoricoporentrada() {
        String sql = """
            SELECT e.nome AS nome_produto, u.nome AS nome_usuario, 
                   h.tipo_movimentacao, h.quantidade_movimentada, 
                   h.observacao, h.data_hora
            FROM historico_movimentacoes h
            JOIN estoque_geral e ON h.produto_id = e.id
            JOIN usuarios u ON h.usuario_id = u.id
            WHERE h.tipo_movimentacao LIKE '%entrada%'
            ORDER BY h.data_hora DESC
            """;
        return buscar(sql, null);
    }

    public static List<Historico> imprimirHistoricoporsaida() {
        String sql = """
            SELECT e.nome AS nome_produto, u.nome AS nome_usuario, 
                   h.tipo_movimentacao, h.quantidade_movimentada, 
                   h.observacao, h.data_hora
            FROM historico_movimentacoes h
            JOIN estoque_geral e ON h.produto_id = e.id
            JOIN usuarios u ON h.usuario_id = u.id
            WHERE h.tipo_movimentacao LIKE '%saida%'
            ORDER BY h.data_hora DESC
            """;
        return buscar(sql, null);
    }

    // NOVO MÉTODO: Busca específica por nome de produto
    public static List<Historico> buscarPorProduto(String nomeProduto) {
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
        return buscar(sql, "%" + nomeProduto + "%");
    }

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

    public static  List<Historico> buscarPorData (String dataDE,String dataATE){
        String sql = """
        SELECT e.nome AS nome_produto, u.nome AS nome_usuario,
        h.tipo_movimentacao, h.quantidade_movimentada,
        h.observacao, h.data_hora
        FROM historico_movimentacoes h
        JOIN estoque_geral e ON h.produto_id = e.id
        JOIN usuarios u ON h.usuario_id = u.id WHERE data_hora BETWEEN ? AND ?
        """;

        List<Historico> itens = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dataDE);
            stmt.setString(2, dataATE);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(mapearHistorico(rs));
                }
            }
        }
        catch (SQLException e) {
            System.err.println("Erro na consulta SQL: " + e.getMessage());
        }
        return itens;

    }
}

