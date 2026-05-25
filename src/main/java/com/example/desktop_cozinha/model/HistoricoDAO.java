package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoricoDAO {

    private static Historico mapearHistorico(ResultSet rs) throws SQLException {
        return new Historico(
                rs.getString("nome_produto"),
                rs.getString("nome_usuario"),
                rs.getString("tipo_movimentacao"),
                rs.getInt("quantidade_movimentada"),
                rs.getString("tipo"),
                rs.getString("data_hora")
        );
    }

    /**
     * Filtro mestre dinâmico que une produto, tipo de movimentação e intervalo de datas.
     */
    public static List<Historico> filtrar(String nomeProduto, String tipoMovimentacao, String dataDe, String dataAte) {
        // Base da Query
        StringBuilder sql = new StringBuilder("""
                SELECT e.nome AS nome_produto, u.nome AS nome_usuario,\s
                                            h.tipo_movimentacao, h.quantidade_movimentada,\s
                                            e.tipo, h.data_hora
                                     FROM historico_movimentacao h
                                     JOIN estoque_geral e ON h.produto_id = e.id
                                     JOIN usuarios u ON h.usuario_id = u.id
                                     WHERE 1=1
            """);


        // Cláusula "WHERE 1=1" serve para podermos encadear os "AND" dinamicamente sem quebrar o SQL

        // 1. Filtro por Nome do Produto
        if (nomeProduto != null && !nomeProduto.trim().isEmpty()) {
            sql.append(" AND e.nome LIKE ?");
        }

        // 2. Filtro por Tipo de Movimentação
        if (tipoMovimentacao != null && !tipoMovimentacao.equals("Todos")) {
            sql.append(" AND h.tipo_movimentacao = ?");

        }
        else {
            sql.append(" AND h.tipo_movimentacao IN ('Entrada', 'Saida', 'Descarte') ");

        }

        // 3. Filtro por Intervalo de Datas
        if (dataDe != null && !dataDe.trim().isEmpty() && dataAte != null && !dataAte.trim().isEmpty()) {
            // Se o seu banco for TIMESTAMP/DATETIME, usamos strings adaptadas para o dia completo
            sql.append(" AND h.data_hora BETWEEN ? AND ?");
        }

        // Ordenação padrão descrescente
        sql.append(" ORDER BY h.data_hora DESC;");

        List<Historico> itens = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            // Define o parâmetro do Produto se ele existir
            if (nomeProduto != null && !nomeProduto.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + nomeProduto + "%");
            }

            // Define o parâmetro do Tipo se ele existir
            if (tipoMovimentacao != null && !tipoMovimentacao.equals("Todos")) {
                stmt.setString(paramIndex++, tipoMovimentacao);
            }

            // Define os parâmetros de data se existirem
            if (dataDe != null && !dataDe.trim().isEmpty() && dataAte != null && !dataAte.trim().isEmpty()) {
                // Adiciona os horários na String para garantir que busque o dia inteiro
                stmt.setString(paramIndex++, dataDe + " 00:00:00");
                stmt.setString(paramIndex++, dataAte + " 23:59:59");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(mapearHistorico(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro na consulta dinâmica: " + e.getMessage());
        }
        return itens;
    }
}