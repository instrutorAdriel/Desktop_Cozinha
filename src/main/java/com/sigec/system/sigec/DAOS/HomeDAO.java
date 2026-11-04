package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Constructors.AlertaEstoqueDTO;
import com.sigec.system.sigec.Constructors.UnidadeDTO;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HomeDAO {

    // 1. Produtos Vencidos
    public List<AlertaEstoqueDTO> listarProdutosVencidos(int idUnidade) {
        List<AlertaEstoqueDTO> lista = new ArrayList<>();
        String sql = "SELECT p.nome_produto, i.data_validade, c.nome_categoria " +
                "FROM insumo i " +
                "JOIN produto p ON i.id_produto = p.id_produto " +
                "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria " +
                "WHERE i.data_validade < CURRENT_DATE " +
                "AND i.quantidade > 0 AND i.cancelado = 'N' AND i.id_unidade = ?";


        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUnidade);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AlertaEstoqueDTO alerta = new AlertaEstoqueDTO();
                    alerta.setNomeProduto(rs.getString("nome_produto"));
                    alerta.setNomeCategoria(rs.getString("nome_categoria"));

                    // Convertendo java.sql.Date para java.time.LocalDate
                    if (rs.getDate("data_validade") != null) {
                        alerta.setDataValidade(rs.getDate("data_validade").toLocalDate());
                    }

                    lista.add(alerta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar produtos vencidos: " + e.getMessage());
        }

        return lista;
    }

    // 2. Próximos ao Vencimento
    public List<AlertaEstoqueDTO> listarProximosVencimento(int idUnidade) {
        List<AlertaEstoqueDTO> lista = new ArrayList<>();
        String sql = "SELECT p.nome_produto, i.data_validade, i.lote, i.quantidade " +
                "FROM insumo i " +
                "JOIN produto p ON i.id_produto = p.id_produto " +
                "WHERE i.data_validade BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY) " +
                "AND i.quantidade > 0 AND i.cancelado = 'N' AND i.id_unidade = ?";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUnidade);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AlertaEstoqueDTO alerta = new AlertaEstoqueDTO();
                    alerta.setNomeProduto(rs.getString("nome_produto"));
                    alerta.setLote(rs.getString("lote"));
                    alerta.setQtdAtual(rs.getInt("quantidade"));

                    // Convertendo java.sql.Date para java.time.LocalDate
                    if (rs.getDate("data_validade") != null) {
                        alerta.setDataValidade(rs.getDate("data_validade").toLocalDate());
                    }

                    lista.add(alerta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar produtos próximos ao vencimento: " + e.getMessage());
        }

        return lista;
    }

    // 3. Estoque Baixo
    public List<AlertaEstoqueDTO> listarEstoqueBaixo(int idUnidade) {
        List<AlertaEstoqueDTO> lista = new ArrayList<>();
        String sql = "SELECT p.nome_produto, i.quantidade, i.quantidade_mininma " +
                "FROM insumo i " +
                "JOIN produto p ON i.id_produto = p.id_produto " +
                "WHERE i.quantidade > 0 AND i.quantidade <= i.quantidade_mininma " +
                "AND i.cancelado = 'N' AND i.id_unidade = ?";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUnidade);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AlertaEstoqueDTO alerta = new AlertaEstoqueDTO();
                    alerta.setNomeProduto(rs.getString("nome_produto"));
                    alerta.setQtdAtual(rs.getInt("quantidade"));
                    alerta.setQtdMinima(rs.getInt("quantidade_mininma"));

                    lista.add(alerta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar estoque baixo: " + e.getMessage());
        }

        return lista;
    }

    // 4. Estoque Zerado
    public List<AlertaEstoqueDTO> listarEstoqueZerado(int idUnidade) {
        List<AlertaEstoqueDTO> lista = new ArrayList<>();
        String sql = "SELECT p.nome_produto, c.nome_categoria " +
                "FROM insumo i " +
                "JOIN produto p ON i.id_produto = p.id_produto " +
                "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria " +
                "WHERE i.quantidade <= 0 AND i.cancelado = 'N' AND i.id_unidade = ?";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUnidade);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AlertaEstoqueDTO alerta = new AlertaEstoqueDTO();
                    alerta.setNomeProduto(rs.getString("nome_produto"));
                    alerta.setNomeCategoria(rs.getString("nome_categoria"));

                    lista.add(alerta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar estoque zerado: " + e.getMessage());
        }

        return lista;
    }

    public List<UnidadeDTO> listarUnidades() {
        List<UnidadeDTO> unidades = new ArrayList<>();
        String sql = "SELECT id_unidade, nome_unidade FROM unidade WHERE situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                unidades.add(new UnidadeDTO(rs.getInt("id_unidade"), rs.getString("nome_unidade")));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar unidades: " + e.getMessage());
        }
        return unidades;
    }
}