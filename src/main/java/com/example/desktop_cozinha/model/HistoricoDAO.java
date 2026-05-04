package com.example.desktop_cozinha.model;
import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class HistoricoDAO {

    public void onEstoqueMudou() {
        // Reage automaticamente ao aviso
        System.out.println("\n--- [Histórico Notificado] Atualizando e imprimindo historico... ---\n");
        imprimirHistoricoCompleto();
    }

    // Método JDBC normal para ler e imprimir toda a tabela
    public void imprimirHistoricoCompleto() {
        String sql = "SELECT \n" +
                "    e.nome AS nome_produto, \n" +
                "    u.nome AS nome_usuario, \n" +
                "    h.tipo_movimentacao, \n" +
                "    h.quantidade_movimentada, \n" +
                "    h.observacao, \n" +
                "    h.data_hora\n" +
                "FROM historico_movimentacoes h\n" +
                "JOIN estoque_geral e ON h.produto_id = e.id\n" +
                "JOIN usuarios u ON h.usuario_id = u.id\n" +
                "ORDER BY h.data_hora DESC;";


        try (Connection conn =  DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {


                        rs.getInt("id"),
                        rs.getLong("produto_id"),
                        rs.getLong("usuario_id"),
                        rs.getString("tipo_movimentacao"),
                        rs.getInt("quantidade_movimentada"),
                        rs.getTimestamp("data_hora"),
                        rs.getString("observacao");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
