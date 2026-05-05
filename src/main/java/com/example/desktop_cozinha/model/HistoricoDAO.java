package com.example.desktop_cozinha.model;
import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoricoDAO {

    public void onEstoqueMudou() {
        // Reage automaticamente ao aviso
        System.out.println("\n--- [Histórico Notificado] Atualizando e imprimindo historico... ---\n");
        imprimirHistoricoCompleto();
    }

    // Método JDBC normal para ler e imprimir toda a tabela
    public List<Historico> imprimirHistoricoCompleto() {
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
                "ORDER BY h.data_hora DESC";



                // vamos escrever os dados de cada livro
                List<Historico> itens = new ArrayList<>();


                try (Connection conn = DatabaseConfig.getConnection()) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {


                        try (ResultSet rs = stmt.executeQuery()) {

                            while (rs.next()) {

                                Historico H = new Historico(
                                        rs.getString("nome_produto"),
                                        rs.getString("nome_usuario"),
                                        rs.getString("tipo"),
                                        rs.getInt("quantidade_movimentada"),
                                        rs.getString("observacao")
                                );
                                itens.add(H);
                            }
                        }

                    }
                } catch (SQLException e) {
                    System.err.println("Erro");
                }

                return itens;


            }

    }

